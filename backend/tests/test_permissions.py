"""Autorización: cada rol solo puede actuar sobre lo suyo (antes no había ningún control)."""
from datetime import timedelta

from .conftest import auth, future_at

API = "/api/v1"


def test_client_profile_is_bound_to_token(client, world):
    headers = auth(client, world["client"])
    resp = client.patch(f"{API}/me/client", json={"nombre": "Nombre Nuevo", "telefono": "600111222"},
                        headers=headers)
    assert resp.status_code == 200
    other = auth(client, world["client2"])
    assert client.get(f"{API}/me/client", headers=other).get_json()["nombre"] != "Nombre Nuevo"


def test_profile_rejects_unknown_fields_and_invalid_data(client, world):
    headers = auth(client, world["client"])
    assert client.patch(f"{API}/me/client", json={"rol": "admin"}, headers=headers).status_code == 422
    assert client.patch(f"{API}/me/client", json={"telefono": "12ab"}, headers=headers).status_code == 422
    assert client.patch(f"{API}/me/client", json={"email": "no-es-email"}, headers=headers).status_code == 422


def test_only_admin_of_local_can_edit_barbershop(client, world):
    url = f"{API}/barbershops/{world['localid']}"
    body = {"descripcion": "Nueva descripción"}
    assert client.patch(url, json=body, headers=auth(client, world["client"])).status_code == 403
    assert client.patch(url, json=body, headers=auth(client, world["barber"])).status_code == 403
    assert client.patch(url, json=body, headers=auth(client, world["semi"])).status_code == 403
    assert client.patch(url, json=body, headers=auth(client, world["other_admin"])).status_code == 403
    assert client.patch(url, json=body, headers=auth(client, world["admin"])).status_code == 200
    bad = client.patch(url, json={"horarioapertura": "22:00:00"}, headers=auth(client, world["admin"]))
    assert bad.status_code == 400  # apertura posterior al cierre


def test_service_management_permissions_and_soft_delete(client, make, world):
    url = f"{API}/barbershops/{world['localid']}/services"
    body = {"nombre": "Tinte", "descripcion": None, "duracion": 60, "precio": 30}
    assert client.post(url, json=body, headers=auth(client, world["barber"])).status_code == 403
    assert client.post(url, json=body, headers=auth(client, world["other_admin"])).status_code == 403
    created = client.post(url, json=body, headers=auth(client, world["semi"]))
    assert created.status_code == 201
    sid = created.get_json()["servicioid"]
    assert client.post(url, json=body, headers=auth(client, world["admin"])).status_code == 409

    # Validación de rangos
    bad = dict(body, nombre="Gratis", precio=-1)
    assert client.post(url, json=bad, headers=auth(client, world["admin"])).status_code == 422

    # Borrar un servicio con citas ya no borra las citas.
    citaid = make.appointment(world["client"], world["barber"], sid, future_at(10))
    assert client.delete(f"{API}/services/{sid}", headers=auth(client, world["other_admin"])).status_code == 403
    assert client.delete(f"{API}/services/{sid}", headers=auth(client, world["admin"])).status_code == 200
    services = client.get(url, headers=auth(client, world["client"])).get_json()
    assert sid not in [s["servicioid"] for s in services]
    dates = client.get(f"{API}/me/appointments", headers=auth(client, world["client"])).get_json()
    assert citaid in [d["citaid"] for d in dates["appointments"]]


def test_reviews_permissions(client, world):
    url = f"{API}/barbershops/{world['localid']}/reviews"
    c1 = auth(client, world["client"])
    resp = client.post(url, json={"calificacion": 4.0, "comentario": "<script>x</script>"}, headers=c1)
    assert resp.status_code == 201
    rid = resp.get_json()["resenaid"]
    # Ni otro cliente ni el personal de otro local pueden borrarla.
    assert client.delete(f"{API}/reviews/{rid}", headers=auth(client, world["client2"])).status_code == 404
    assert client.delete(f"{API}/reviews/{rid}", headers=auth(client, world["other_admin"])).status_code == 404
    assert client.delete(f"{API}/reviews/{rid}", headers=auth(client, world["barber"])).status_code == 404
    assert client.delete(f"{API}/reviews/{rid}", headers=auth(client, world["semi"])).status_code == 200

    for _ in range(3):
        assert client.post(url, json={"calificacion": 5}, headers=c1).status_code == 201
    assert client.post(url, json={"calificacion": 5}, headers=c1).status_code == 409
    assert client.post(url, json={"calificacion": 9}, headers=c1).status_code == 422
    assert client.post(url, json={"calificacion": 5}, headers=auth(client, world["barber"])).status_code == 403


def test_staff_management_only_by_admin_of_same_local(client, world):
    pid = world["barber"]["peluqueroid"]
    assert client.patch(f"{API}/barbers/{pid}/role", json={"rol": "semiadmin"},
                        headers=auth(client, world["semi"])).status_code == 403
    assert client.patch(f"{API}/barbers/{pid}/role", json={"rol": "semiadmin"},
                        headers=auth(client, world["other_admin"])).status_code == 403
    assert client.patch(f"{API}/barbers/{pid}/role", json={"rol": "admin"},
                        headers=auth(client, world["admin"])).status_code == 422
    assert client.patch(f"{API}/barbers/{pid}/role", json={"rol": "semiadmin"},
                        headers=auth(client, world["admin"])).status_code == 200
    # El admin no puede degradarse ni desactivarse a sí mismo.
    me = world["admin"]["peluqueroid"]
    assert client.patch(f"{API}/barbers/{me}/active", json={"activo": False},
                        headers=auth(client, world["admin"])).status_code == 403

    inactive = client.get(f"{API}/barbershops/{world['other_localid']}/barbers/inactive",
                          headers=auth(client, world["admin"]))
    assert inactive.status_code == 403  # antes listaba los inactivos de TODOS los locales


def test_create_barber_is_scoped_to_admin_local(client, world):
    body = {"nombreusuario": "nuevo_pelu", "contrasena": "Clave1234", "email": "np@test.haircloud",
            "nombre": "Nuevo Pelu", "especialidad": None,
            "horario": [{"dia": "Lunes", "inicio": "09:00", "fin": "14:00"}]}
    assert client.post(f"{API}/barbershops/{world['other_localid']}/barbers", json=body,
                       headers=auth(client, world["admin"])).status_code == 403
    resp = client.post(f"{API}/barbershops/{world['localid']}/barbers", json=body,
                       headers=auth(client, world["admin"]))
    assert resp.status_code == 201
    overlap = dict(body, nombreusuario="otro_pelu", email="op@test.haircloud",
                   horario=[{"dia": "Lunes", "inicio": "09:00", "fin": "14:00"},
                            {"dia": "Lunes", "inicio": "13:00", "fin": "15:00"}])
    assert client.post(f"{API}/barbershops/{world['localid']}/barbers", json=overlap,
                       headers=auth(client, world["admin"])).status_code == 400


def test_barber_agenda_and_stats_are_private(client, make, world):
    pid = world["barber"]["peluqueroid"]
    day = future_at(10).date().isoformat()
    for who, expected in (("client", 403), ("other_admin", 403), ("barber", 200), ("semi", 200),
                          ("admin", 200)):
        resp = client.get(f"{API}/barbers/{pid}/appointments?date={day}", headers=auth(client, world[who]))
        assert resp.status_code == expected, who
    stats_url = f"{API}/barbers/{pid}/stats?start={day}&end={day}"
    assert client.get(stats_url, headers=auth(client, world["semi"])).status_code == 403
    assert client.get(stats_url, headers=auth(client, world["admin"])).status_code == 200
    # El email de estadísticas solo a uno mismo, con datos calculados en el servidor.
    assert client.post(f"{API}/barbers/{pid}/stats/email", json={"start_date": day, "end_date": day},
                       headers=auth(client, world["admin"])).status_code == 403


def test_barber_phone_hidden_from_clients(client, make, world):
    with client.application.app_context():
        from app.db import transaction
        with transaction() as cur:
            cur.execute("UPDATE peluqueros SET telefono = '600000001' WHERE peluqueroid = %s",
                        (world["barber"]["peluqueroid"],))
    url = f"{API}/barbershops/{world['localid']}/barbers"
    as_client = client.get(url, headers=auth(client, world["client"])).get_json()
    as_staff = client.get(url, headers=auth(client, world["semi"])).get_json()
    assert all(b["telefono"] is None for b in as_client)
    assert "600000001" in [b["telefono"] for b in as_staff]


def test_schedule_update_permissions(client, world):
    pid = world["barber"]["peluqueroid"]
    body = [{"dia": "Martes", "inicio": "10:00", "fin": "14:00"}]
    assert client.put(f"{API}/barbers/{pid}/schedule", json=body,
                      headers=auth(client, world["semi"])).status_code == 403
    assert client.put(f"{API}/barbers/{pid}/schedule", json=body,
                      headers=auth(client, world["barber"])).status_code == 200
    assert client.put(f"{API}/barbers/{pid}/schedule", json=[{"dia": "Martes", "inicio": "15:00", "fin": "10:00"}],
                      headers=auth(client, world["admin"])).status_code == 422
    sched = client.get(f"{API}/barbers/{pid}/schedule", headers=auth(client, world["client"])).get_json()
    assert sched == [{"diasemana": "Martes", "horainicio": "10:00:00", "horafin": "14:00:00"}]
