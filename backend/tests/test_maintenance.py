"""Cambio de contraseña y tareas programadas de mantenimiento."""
from datetime import timedelta

from app.cli import run_maintenance
from app.db import fetch_one, transaction

from .conftest import PASSWORD, auth, local_now, login

API = "/api/v1"


def test_change_password_revokes_other_sessions(client, world):
    other_device = login(client, world["client"]["username"])
    headers = auth(client, world["client"])
    bad = client.post(f"{API}/auth/change-password", headers=headers,
                      json={"current_password": "incorrecta1", "new_password": "NuevaClave1"})
    assert bad.status_code == 401
    ok = client.post(f"{API}/auth/change-password", headers=headers,
                     json={"current_password": PASSWORD, "new_password": "NuevaClave1"})
    assert ok.status_code == 200
    # La sesión que hace el cambio recibe tokens nuevos; las demás quedan revocadas.
    new_headers = {"Authorization": f"Bearer {ok.get_json()['access_token']}"}
    assert client.get(f"{API}/me/client", headers=new_headers).status_code == 200
    assert client.get(f"{API}/me/client", headers=headers).status_code == 401
    assert client.post(f"{API}/auth/refresh",
                       json={"refresh_token": other_device["refresh_token"]}).status_code == 401


def test_maintenance_completes_past_and_purges(app, make, world):
    past = make.appointment(world["client"], world["barber"], world["servicioid"],
                            local_now() - timedelta(days=2))
    with app.app_context():
        with transaction() as cur:
            cur.execute(
                "INSERT INTO actividad_peluquero (peluqueroid, tipo, citaid, clienteid, fecha) VALUES (%s, 'Reserva', %s, %s, %s)",
                (world["barber"]["peluqueroid"], past, world["client"]["clienteid"], local_now() - timedelta(days=30)),
            )
            cur.execute(
                "INSERT INTO codigos_verificacion (email, tipo, codigo_hash, expiracion) VALUES ('a@b.es', 'password_reset', NULL, %s)",
                (local_now() - timedelta(hours=1),),
            )
        result = run_maintenance()
        assert result["citas_completadas"] == 1
        assert result["actividad_purgada"] == 1
        assert result["codigos_purgados"] == 1
        assert fetch_one("SELECT estado FROM citas WHERE citaid = %s", (past,))["estado"] == "Completada"
