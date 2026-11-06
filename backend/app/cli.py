"""Comandos de administración (``flask --app wsgi <comando>``).

* ``db-upgrade``      Aplica las migraciones SQL pendientes de ``migrations/``.
* ``db-status``       Muestra qué migraciones están aplicadas.
* ``maintenance``     Tareas periódicas: completa citas pasadas y purga datos
                      caducados (códigos, sesiones, actividad, auditoría).
                      Pensado para ejecutarse desde cron / Programador de tareas.
* ``create-admin``    Crea un local y su usuario administrador.
* ``seed-demo``       Carga datos ficticios para desarrollo.
"""
from __future__ import annotations

import getpass
import secrets
from datetime import date, time, timedelta
from pathlib import Path

import click
from flask import Flask, current_app

from .db import connection, transaction
from .schemas import Password, Username
from .security.passwords import hash_password
from .services.appointments import complete_past_appointments
from .timeutils import WEEKDAYS_ES, now_local

MIGRATIONS_DIR = Path(__file__).resolve().parent.parent / "migrations"


def _migration_files() -> list[Path]:
    return sorted(p for p in MIGRATIONS_DIR.glob("*.sql") if p.name[:4].isdigit())


def run_migrations(echo=print) -> list[str]:
    applied_now: list[str] = []
    with connection() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT pg_advisory_lock(727274)")  # evita migraciones concurrentes
            try:
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version varchar(100) PRIMARY KEY,
                        applied_at timestamp without time zone NOT NULL DEFAULT now()
                    )
                """)
                cur.execute("SELECT version FROM schema_migrations")
                applied = {r["version"] for r in cur.fetchall()}
                cur.execute("SELECT to_regclass('public.usuarios') IS NOT NULL AS legacy")
                legacy = cur.fetchone()["legacy"]
                conn.commit()

                for path in _migration_files():
                    version = path.stem
                    if version in applied:
                        continue
                    if version.startswith("0001") and legacy:
                        echo(f"  {version}: esquema existente detectado, se marca como aplicada")
                    else:
                        echo(f"  {version}: aplicando...")
                        cur.execute(path.read_text(encoding="utf-8"))
                    cur.execute("INSERT INTO schema_migrations (version) VALUES (%s)", (version,))
                    conn.commit()
                    applied_now.append(version)
            except Exception:
                conn.rollback()
                raise
            finally:
                cur.execute("SELECT pg_advisory_unlock(727274)")
                conn.commit()
    return applied_now


def run_maintenance() -> dict:
    cfg = current_app.config
    now = now_local()
    result = {"citas_completadas": complete_past_appointments()}
    with transaction() as cur:
        cur.execute("DELETE FROM codigos_verificacion WHERE expiracion < %s AND (token_expira IS NULL OR token_expira < %s)",
                    (now, now))
        result["codigos_purgados"] = cur.rowcount
        cur.execute("DELETE FROM refresh_tokens WHERE expires_at < %s OR revoked_at < %s",
                    (now, now - timedelta(days=7)))
        result["sesiones_purgadas"] = cur.rowcount
        cur.execute("DELETE FROM actividad_peluquero WHERE fecha < %s",
                    (now - timedelta(days=cfg["ACTIVITY_RETENTION_DAYS"]),))
        result["actividad_purgada"] = cur.rowcount
        cur.execute("DELETE FROM audit_log WHERE fecha < %s", (now - timedelta(days=365),))
        result["auditoria_purgada"] = cur.rowcount
    return result


def register_cli(app: Flask) -> None:
    @app.cli.command("db-upgrade")
    def db_upgrade():
        """Aplica las migraciones pendientes."""
        done = run_migrations(click.echo)
        click.echo("Base de datos al día." if not done else f"Aplicadas: {', '.join(done)}")

    @app.cli.command("db-status")
    def db_status():
        """Lista las migraciones y su estado."""
        with transaction() as cur:
            cur.execute("SELECT to_regclass('public.schema_migrations') IS NOT NULL AS ok")
            applied = set()
            if cur.fetchone()["ok"]:
                cur.execute("SELECT version FROM schema_migrations")
                applied = {r["version"] for r in cur.fetchall()}
        for path in _migration_files():
            click.echo(f"  [{'x' if path.stem in applied else ' '}] {path.stem}")

    @app.cli.command("maintenance")
    def maintenance():
        """Completa citas pasadas y purga datos caducados."""
        for key, value in run_maintenance().items():
            click.echo(f"  {key}: {value}")

    @app.cli.command("create-admin")
    @click.option("--local-nombre", prompt="Nombre del local")
    @click.option("--direccion", prompt="Dirección")
    @click.option("--localidad", prompt="Localidad")
    @click.option("--telefono-local", prompt="Teléfono del local")
    @click.option("--usuario", prompt="Usuario administrador")
    @click.option("--email", prompt="Email del administrador")
    @click.option("--nombre", prompt="Nombre completo del administrador")
    def create_admin(local_nombre, direccion, localidad, telefono_local, usuario, email, nombre):
        """Crea un local con su administrador (que también es peluquero)."""
        from pydantic import TypeAdapter
        TypeAdapter(Username).validate_python(usuario)
        password = getpass.getpass("Contraseña: ")
        TypeAdapter(Password).validate_python(password)
        now = now_local()
        with transaction() as cur:
            cur.execute(
                """INSERT INTO local (nombre, direccion, telefono, horarioapertura, horariocierre, localidad)
                   VALUES (%s, %s, %s, '09:00', '20:00', %s) RETURNING localid""",
                (local_nombre, direccion, telefono_local, localidad),
            )
            localid = cur.fetchone()["localid"]
            cur.execute(
                """INSERT INTO usuarios (nombreusuario, password_hash, rol, localid, email, created_at)
                   VALUES (%s, %s, 'admin', %s, %s, %s) RETURNING usuarioid""",
                (usuario, hash_password(password), localid, email.strip().lower(), now),
            )
            usuarioid = cur.fetchone()["usuarioid"]
            cur.execute(
                """INSERT INTO peluqueros (usuarioid, nombre, fechacontratacion, localid, activo)
                   VALUES (%s, %s, %s, %s, TRUE)""",
                (usuarioid, nombre, now.date(), localid),
            )
        click.echo(f"Local {localid} y administrador '{usuario}' creados.")

    @app.cli.command("seed-demo")
    @click.option("--password", default=None, help="Contraseña común para las cuentas demo")
    def seed_demo(password):
        """Carga datos ficticios (solo desarrollo)."""
        if current_app.config["ENV_NAME"] == "production":
            raise click.ClickException("seed-demo no se puede usar en producción")
        password = password or ("Demo" + secrets.token_hex(4) + "1")
        info = seed_demo_data(password)
        click.echo("Datos demo cargados. Cuentas (misma contraseña para todas):")
        for user in info:
            click.echo(f"  {user}")
        click.echo(f"  contraseña: {password}")


def seed_demo_data(password: str) -> list[str]:
    pw = hash_password(password)
    now = now_local()
    with transaction() as cur:
        cur.execute("SELECT 1 FROM usuarios WHERE nombreusuario = 'admin_demo'")
        if cur.fetchone():
            raise click.ClickException("Los datos demo ya existen")
        cur.execute(
            """INSERT INTO local (nombre, direccion, telefono, horarioapertura, horariocierre, descripcion, localidad)
               VALUES ('Barbería Demo', 'Calle Mayor 1', '928000001', '09:00', '20:00',
                       'Local de demostración', 'Arrecife') RETURNING localid"""
        )
        localid = cur.fetchone()["localid"]

        def user(username, rol, email, local=None):
            cur.execute(
                """INSERT INTO usuarios (nombreusuario, password_hash, rol, localid, email, created_at)
                   VALUES (%s, %s, %s, %s, %s, %s) RETURNING usuarioid""",
                (username, pw, rol, local, email, now),
            )
            return cur.fetchone()["usuarioid"]

        def barber(usuarioid, nombre):
            cur.execute(
                """INSERT INTO peluqueros (usuarioid, nombre, especialidad, fechacontratacion, localid, activo)
                   VALUES (%s, %s, 'Corte y barba', %s, %s, TRUE) RETURNING peluqueroid""",
                (usuarioid, nombre, date(2024, 1, 15), localid),
            )
            pid = cur.fetchone()["peluqueroid"]
            for dia in WEEKDAYS_ES[:5]:
                cur.execute(
                    """INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin)
                       VALUES (%s, %s, %s, %s), (%s, %s, %s, %s)""",
                    (pid, dia, time(9), time(13), pid, dia, time(16), time(20)),
                )
            return pid

        barber(user("admin_demo", "admin", "admin@demo.haircloud.example", localid), "Ana Admin")
        barber(user("semi_demo", "semiadmin", "semi@demo.haircloud.example", localid), "Sergio Semiadmin")
        barber(user("pelu_demo", "peluquero", "pelu@demo.haircloud.example", localid), "Paula Peluquera")
        for nombre, desc, dur, precio in (("Corte clásico", "Corte con tijera y máquina", 30, 15),
                                          ("Arreglo de barba", "Perfilado y toalla caliente", 20, 10),
                                          ("Corte + barba", "Servicio completo", 50, 22)):
            cur.execute(
                "INSERT INTO servicios (nombre, descripcion, duracion, precio, localid) VALUES (%s, %s, %s, %s, %s)",
                (nombre, desc, dur, precio, localid),
            )
        cid = user("cliente_demo", "cliente", "cliente@demo.haircloud.example")
        cur.execute("INSERT INTO clientes (usuarioid, nombre, fecharegistro) VALUES (%s, 'Carlos Cliente', %s)",
                    (cid, now.date()))
    return ["admin_demo (admin)", "semi_demo (semiadmin)", "pelu_demo (peluquero)", "cliente_demo (cliente)"]
