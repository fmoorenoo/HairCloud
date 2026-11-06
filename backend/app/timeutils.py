"""Utilidades de fecha/hora.

Las columnas de la base de datos son ``timestamp without time zone`` y guardan
la hora local del negocio (``APP_TIMEZONE``). Para no mezclar relojes (el
código original comparaba ``datetime.now()`` del servidor con ``NOW()`` de la
base de datos), toda la lógica obtiene "ahora" desde aquí.
"""
from __future__ import annotations

from datetime import date, datetime, time
from zoneinfo import ZoneInfo

from flask import current_app

WEEKDAYS_ES = ("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
MONTHS_ES = ("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
             "agosto", "septiembre", "octubre", "noviembre", "diciembre")

DATETIME_FMT = "%Y-%m-%d %H:%M"


def business_tz() -> ZoneInfo:
    return ZoneInfo(current_app.config["APP_TIMEZONE"])


def now_local() -> datetime:
    """Hora actual del negocio, sin tzinfo y truncada a segundos."""
    return datetime.now(business_tz()).replace(tzinfo=None, microsecond=0)


def today_local() -> date:
    return now_local().date()


def weekday_es(d: date) -> str:
    # weekday() no depende del locale del sistema (strftime("%A") sí, y en un
    # servidor con locale español el código original lanzaba KeyError).
    return WEEKDAYS_ES[d.weekday()]


def fmt_datetime(value: datetime | None) -> str | None:
    return value.strftime(DATETIME_FMT) if value else None


def fmt_date(value: date | None) -> str | None:
    return value.isoformat() if value else None


def fmt_time(value: time | None) -> str | None:
    return value.strftime("%H:%M:%S") if value else None


def long_date_es(d: date) -> str:
    return f"{d.day} de {MONTHS_ES[d.month - 1]} de {d.year}"


def weekday_long_es(d: date) -> str:
    return f"{weekday_es(d)} {d.day} de {MONTHS_ES[d.month - 1]}"
