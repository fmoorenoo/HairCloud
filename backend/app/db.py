"""Acceso a PostgreSQL con un pool de conexiones y transacciones explícitas.

Uso típico::

    with transaction() as cur:
        cur.execute("UPDATE ...", (...,))

    row = fetch_one("SELECT ... WHERE id = %s", (id_,))

Cualquier excepción dentro de ``transaction()`` provoca ROLLBACK; la conexión
vuelve siempre al pool, aunque se produzca un error (el código anterior
dejaba conexiones abiertas cada vez que una consulta fallaba).
"""
from __future__ import annotations

from contextlib import contextmanager
from typing import Any, Iterator, Sequence

from flask import Flask, current_app
from psycopg import Connection, Cursor
from psycopg.conninfo import make_conninfo
from psycopg.rows import dict_row
from psycopg_pool import ConnectionPool

_EXTENSION_KEY = "haircloud_db_pool"


def build_conninfo(config: dict) -> str:
    if config.get("DATABASE_URL"):
        return config["DATABASE_URL"]
    return make_conninfo(
        host=config["DB_HOST"],
        port=config["DB_PORT"],
        dbname=config["DB_NAME"],
        user=config["DB_USER"],
        password=config["DB_PASSWORD"] or "",
    )


def init_db(app: Flask) -> None:
    timezone = app.config["APP_TIMEZONE"]

    def configure(conn: Connection) -> None:
        # Todas las columnas de fecha son "timestamp without time zone" y
        # representan la hora local del negocio: fijamos la zona horaria de
        # la sesión para que now()/CURRENT_DATE coincidan con la aplicación.
        conn.execute("SELECT set_config('TimeZone', %s, false)", (timezone,))
        conn.execute("SET search_path TO public")
        conn.commit()

    pool = ConnectionPool(
        conninfo=build_conninfo(app.config),
        min_size=app.config["DB_POOL_MIN"],
        max_size=app.config["DB_POOL_MAX"],
        kwargs={"row_factory": dict_row, "autocommit": False},
        configure=configure,
        open=False,
        name="haircloud",
    )
    pool.open(wait=False)
    app.extensions[_EXTENSION_KEY] = pool


def close_db(app: Flask) -> None:
    pool: ConnectionPool | None = app.extensions.pop(_EXTENSION_KEY, None)
    if pool is not None:
        pool.close()


def _pool() -> ConnectionPool:
    return current_app.extensions[_EXTENSION_KEY]


@contextmanager
def connection() -> Iterator[Connection]:
    with _pool().connection() as conn:
        yield conn


@contextmanager
def transaction() -> Iterator[Cursor]:
    """Abre una transacción; COMMIT al salir sin error, ROLLBACK si hay excepción."""
    with _pool().connection() as conn:
        try:
            with conn.cursor() as cur:
                yield cur
            conn.commit()
        except BaseException:
            conn.rollback()
            raise


def fetch_one(sql: str, params: Sequence[Any] | dict | None = None) -> dict | None:
    with transaction() as cur:
        cur.execute(sql, params)
        return cur.fetchone()


def fetch_all(sql: str, params: Sequence[Any] | dict | None = None) -> list[dict]:
    with transaction() as cur:
        cur.execute(sql, params)
        return cur.fetchall()


def fetch_value(sql: str, params: Sequence[Any] | dict | None = None) -> Any:
    row = fetch_one(sql, params)
    if row is None:
        return None
    return next(iter(row.values()))
