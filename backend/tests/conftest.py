"""Fixtures de test contra una base de datos PostgreSQL real.

Requiere ``TEST_DATABASE_URL`` apuntando a una base de datos desechable: el
esquema ``public`` se borra y se recrea con las migraciones al empezar.
"""
from __future__ import annotations

import itertools
import os
from datetime import date, datetime, time, timedelta

import psycopg
import pytest

os.environ.setdefault("APP_ENV", "testing")

from app import create_app  # noqa: E402
from app.cli import run_migrations  # noqa: E402
from app.db import transaction  # noqa: E402
from app.security.passwords import hash_password  # noqa: E402
from app.services.mailer import outbox  # noqa: E402
from app.timeutils import WEEKDAYS_ES, now_local  # noqa: E402

PASSWORD = "Secreta123"
_counter = itertools.count(1)

TABLES = ("audit_log", "refresh_tokens", "actividad_peluquero", "favoritos_clientes", "resenas", "citas",
          "bloqueoshorarios", "horarios_peluqueros", "servicios", "codigos_verificacion", "clientes",
          "peluqueros", "usuarios", "local")


@pytest.fixture(scope="session")
def app():
    url = os.environ.get("TEST_DATABASE_URL")
    if not url:
        pytest.skip("TEST_DATABASE_URL no definida")
    with psycopg.connect(url, autocommit=True) as conn:
        conn.execute("DROP SCHEMA IF EXISTS public CASCADE")
        conn.execute("CREATE SCHEMA public")
    application = create_app("testing")
    with application.app_context():
        run_migrations(lambda *_: None)
    yield application


@pytest.fixture(autouse=True)
def _clean(request):
    if not {"client", "make", "ctx", "world"} & set(request.fixturenames):
        yield  # test unitario puro: no necesita BD
        return
    app = request.getfixturevalue("app")
    with app.app_context():
        with transaction() as cur:
            cur.execute(f"TRUNCATE {', '.join(TABLES)} RESTART IDENTITY CASCADE")
        outbox().clear()
    from app.extensions import limiter
    if app.config["RATELIMIT_ENABLED"]:
        limiter.reset()
    yield


@pytest.fixture
def client(app):
    return app.test_client()


@pytest.fixture
def ctx(app):
    with app.app_context():
        yield


# --------------------------------------------------------------------------
# Factorías
# --------------------------------------------------------------------------
class Factory:
    def __init__(self, app):
        self.app = app
        self._pw = None

    def _hash(self):
        if self._pw is None:
            with self.app.app_context():
                self._pw = hash_password(PASSWORD)
        return self._pw

    def local(self, nombre="Barbería Test") -> int:
        n = next(_counter)
        with self.app.app_context(), transaction() as cur:
            cur.execute(
                """INSERT INTO local (nombre, direccion, telefono, horarioapertura, horariocierre, localidad)
                   VALUES (%s, 'Calle 1', %s, '08:00', '21:00', 'Arrecife') RETURNING localid""",
                (f"{nombre} {n}", f"9280{n:05d}"),
            )
            return cur.fetchone()["localid"]

    def user(self, rol="cliente", localid=None, username=None) -> dict:
        n = next(_counter)
        username = username or f"user{n:04d}"
        email = f"{username}@test.haircloud"
        with self.app.app_context(), transaction() as cur:
            cur.execute(
                """INSERT INTO usuarios (nombreusuario, password_hash, rol, localid, email)
                   VALUES (%s, %s, %s, %s, %s) RETURNING usuarioid""",
                (username, self._hash(), rol, localid, email),
            )
            uid = cur.fetchone()["usuarioid"]
            out = {"usuarioid": uid, "username": username, "email": email, "rol": rol, "localid": localid}
            if rol == "cliente":
                cur.execute("INSERT INTO clientes (usuarioid, nombre) VALUES (%s, %s) RETURNING clienteid",
                            (uid, f"Cliente {n}"))
                out["clienteid"] = cur.fetchone()["clienteid"]
            else:
                cur.execute(
                    """INSERT INTO peluqueros (usuarioid, nombre, fechacontratacion, localid, activo)
                       VALUES (%s, %s, CURRENT_DATE, %s, TRUE) RETURNING peluqueroid""",
                    (uid, f"Peluquero {n}", localid),
                )
                out["peluqueroid"] = cur.fetchone()["peluqueroid"]
                for dia in WEEKDAYS_ES:
                    cur.execute(
                        "INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin) VALUES (%s, %s, '09:00', '20:00')",
                        (out["peluqueroid"], dia),
                    )
            return out

    def service(self, localid, duracion=30, precio=15, nombre=None) -> int:
        with self.app.app_context(), transaction() as cur:
            cur.execute(
                "INSERT INTO servicios (nombre, duracion, precio, localid) VALUES (%s, %s, %s, %s) RETURNING servicioid",
                (nombre or f"Servicio {next(_counter)}", duracion, precio, localid),
            )
            return cur.fetchone()["servicioid"]

    def appointment(self, cliente, barber, servicioid, start: datetime, minutes=30, estado="Pendiente") -> int:
        with self.app.app_context(), transaction() as cur:
            cur.execute(
                """INSERT INTO citas (clienteid, peluqueroid, servicioid, fechainicio, fechafin, estado, localid)
                   VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING citaid""",
                (cliente["clienteid"], barber["peluqueroid"], servicioid, start,
                 start + timedelta(minutes=minutes), estado, barber["localid"]),
            )
            return cur.fetchone()["citaid"]


@pytest.fixture
def make(app):
    return Factory(app)


def login(client, username, password=PASSWORD) -> dict:
    resp = client.post("/api/v1/auth/login", json={"nombreusuario": username, "password": password})
    assert resp.status_code == 200, resp.get_json()
    return resp.get_json()


def auth(client, user) -> dict:
    tokens = login(client, user["username"])
    return {"Authorization": f"Bearer {tokens['access_token']}"}


@pytest.fixture
def world(make):
    """Un local con admin, semiadmin, peluquero, servicio y dos clientes; y un segundo local."""
    localid = make.local()
    other_local = make.local("Otro local")
    return {
        "localid": localid,
        "other_localid": other_local,
        "admin": make.user("admin", localid),
        "semi": make.user("semiadmin", localid),
        "barber": make.user("peluquero", localid),
        "other_admin": make.user("admin", other_local),
        "client": make.user("cliente"),
        "client2": make.user("cliente"),
        "servicioid": make.service(localid, duracion=30, precio=15),
        "other_servicioid": make.service(other_local, duracion=30, precio=20),
    }


def local_now() -> datetime:
    from zoneinfo import ZoneInfo
    return datetime.now(ZoneInfo(os.getenv("APP_TIMEZONE", "Europe/Madrid"))).replace(tzinfo=None, microsecond=0)


def future_at(hour: int, minute: int = 0, days_ahead: int = 2) -> datetime:
    """Fecha futura; en los tests todos los peluqueros trabajan de 09:00 a 20:00 todos los días."""
    day = (local_now() + timedelta(days=days_ahead)).date()
    return datetime.combine(day, time(hour, minute))
