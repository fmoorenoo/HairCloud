"""Reservas, disponibilidad y máquina de estados de las citas."""
from concurrent.futures import ThreadPoolExecutor
from datetime import timedelta

from app.services.mailer import outbox

from .conftest import auth, future_at, local_now

API = "/api/v1"


def _book(client, headers, world, start, servicioid=None, barber="barber"):
    return client.post(f"{API}/appointments", headers=headers, json={
        "peluqueroid": world[barber]["peluqueroid"],
        "servicioid": servicioid or world["servicioid"],
        "fechainicio": start.strftime("%Y-%m-%d %H:%M"),
    })


def test_booking_derives_client_local_and_end_from_server(app, client, world):
    headers = auth(client, world["client"])
    start = future_at(10)
    resp = _book(client, headers, world, start)
    assert resp.status_code == 201, resp.get_json()
    body = resp.get_json()
    assert body["fechafin"] == (start + timedelta(minutes=30)).strftime("%Y-%m-%d %H:%M")
    assert body["localid"] == world["localid"]

    dates = client.get(f"{API}/me/appointments", headers=headers).get_json()["appointments"]
    assert [d["citaid"] for d in dates] == [body["citaid"]]
    # El servidor envía la confirmación (antes la pedía la app con otra llamada).
    with app.app_context():
        assert outbox()[-1].to == world["client"]["email"]

    # Campos que antes controlaba el cliente ahora se rechazan.
    bad = client.post(f"{API}/appointments", headers=headers, json={
        "peluqueroid": world["barber"]["peluqueroid"], "servicioid": world["servicioid"],
        "fechainicio": future_at(12).strftime("%Y-%m-%d %H:%M"), "clienteid": world["client2"]["clienteid"]})
    assert bad.status_code == 422


def test_double_booking_is_rejected(client, world):
    start = future_at(11)
    assert _book(client, auth(client, world["client"]), world, start).status_code == 201
    resp = _book(client, auth(client, world["client2"]), world, start + timedelta(minutes=15))
    assert resp.status_code == 409


def test_concurrent_bookings_only_one_wins(app, client, world):
    start = future_at(12)
    h1, h2 = auth(client, world["client"]), auth(client, world["client2"])

    def attempt(headers):
        with app.test_client() as c:
            return _book(c, headers, world, start).status_code

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = sorted(pool.map(attempt, [h1, h2]))
    assert results == [201, 409]


def test_booking_validations(client, world):
    headers = auth(client, world["client"])
    assert _book(client, headers, world, future_at(21)).status_code == 409          # fuera de horario
    assert _book(client, headers, world, local_now() - timedelta(days=1)).status_code == 400  # pasado
    assert _book(client, headers, world, future_at(10, days_ahead=200)).status_code == 400  # demasiado lejos
    # Servicio de otro local
    assert _book(client, headers, world, future_at(10), servicioid=world["other_servicioid"]).status_code == 400
    # El personal no reserva como cliente
    assert _book(client, auth(client, world["barber"]), world, future_at(10)).status_code == 403


def test_availability_uses_service_duration_and_hides_busy(client, make, world):
    start = future_at(9)
    make.appointment(world["client2"], world["barber"], world["servicioid"], start)
    day = start.date().isoformat()
    resp = client.get(f"{API}/barbers/{world['barber']['peluqueroid']}/availability"
                      f"?fecha={day}&servicioid={world['servicioid']}",
                      headers=auth(client, world["client"]))
    assert resp.status_code == 200
    slots = resp.get_json()
    assert slots[0] == {"desde": "09:30", "hasta": "10:00"}
    assert all(s["desde"] != "09:00" for s in slots)


def test_client_cancellation_rules(client, make, world):
    headers = auth(client, world["client"])
    far = make.appointment(world["client"], world["barber"], world["servicioid"], future_at(10))
    soon_start = local_now() + timedelta(minutes=20)
    soon = make.appointment(world["client"], world["barber"], world["servicioid"], soon_start)
    others = make.appointment(world["client2"], world["barber"], world["servicioid"], future_at(15))

    url = f"{API}/appointments/{{}}/status"
    assert client.patch(url.format(soon), json={"estado": "Cancelada"}, headers=headers).status_code == 403
    assert client.patch(url.format(others), json={"estado": "Cancelada"}, headers=headers).status_code == 404
    assert client.patch(url.format(far), json={"estado": "Completada"}, headers=headers).status_code == 403
    assert client.patch(url.format(far), json={"estado": "Cancelada"}, headers=headers).status_code == 200
    assert client.patch(url.format(far), json={"estado": "Cancelada"}, headers=headers).status_code == 409

    activity = client.get(f"{API}/barbers/{world['barber']['peluqueroid']}/activity",
                          headers=auth(client, world["barber"])).get_json()
    assert any(a["tipo"] == "Cancelada" for a in activity)


def test_staff_transitions_and_cancellation_email(app, client, make, world):
    barber = auth(client, world["barber"])
    future = make.appointment(world["client"], world["barber"], world["servicioid"], future_at(10))
    past = make.appointment(world["client"], world["barber"], world["servicioid"],
                            local_now() - timedelta(hours=3))
    url = f"{API}/appointments/{{}}/status"

    assert client.patch(url.format(future), json={"estado": "No completada"}, headers=barber).status_code == 409
    assert client.patch(url.format(past), json={"estado": "Pendiente"}, headers=barber).status_code == 409
    assert client.patch(url.format(past), json={"estado": "No completada"}, headers=barber).status_code == 200
    assert client.patch(url.format(past), json={"estado": "Cancelada"}, headers=barber).status_code == 409

    # Otro local no puede tocar la cita
    assert client.patch(url.format(future), json={"estado": "Cancelada"},
                        headers=auth(client, world["other_admin"])).status_code == 404

    resp = client.patch(url.format(future), json={"estado": "Cancelada", "motivo": "<b>Enfermedad</b>"},
                        headers=barber)
    assert resp.status_code == 200
    with app.app_context():
        mail = outbox()[-1]
    assert mail.to == world["client"]["email"]
    assert "&lt;b&gt;Enfermedad&lt;/b&gt;" in mail.html  # el motivo se escapa en el HTML


def test_past_pending_is_reported_completed_and_counted(client, make, world):
    make.appointment(world["client"], world["barber"], world["servicioid"], local_now() - timedelta(days=1))
    day = (local_now() - timedelta(days=1)).date().isoformat()
    pid = world["barber"]["peluqueroid"]
    dates = client.get(f"{API}/barbers/{pid}/appointments?date={day}",
                       headers=auth(client, world["barber"])).get_json()["dates"]
    assert dates[0]["estado"] == "Completada" and dates[0]["finalizada"] is True
    stats = client.get(f"{API}/barbers/{pid}/stats?start={day}&end={day}",
                       headers=auth(client, world["barber"])).get_json()
    assert stats["total_citas"] == 1 and stats["ingresos_totales"] == 15.0
    client_stats = client.get(f"{API}/me/stats", headers=auth(client, world["client"])).get_json()
    assert client_stats["total_citas_finalizadas"] == 1


def test_favorites_are_idempotent(client, world):
    headers = auth(client, world["client"])
    url = f"{API}/me/favorites/{world['localid']}"
    assert client.put(url, headers=headers).status_code == 200
    assert client.put(url, headers=headers).status_code == 200
    favs = client.get(f"{API}/me/favorites", headers=headers).get_json()
    assert len(favs) == 1 and favs[0]["es_favorito"] is True
    assert client.delete(url, headers=headers).status_code == 200
    assert client.get(f"{API}/me/favorites", headers=headers).get_json() == []
