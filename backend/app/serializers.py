"""Conversión explícita de filas de BD a JSON.

Antes cada endpoint devolvía ``SELECT *`` y convertía tipos de forma distinta
(algunas fechas salían en ISO, otras en formato RFC 822 de Flask y los
importes ``Decimal`` como cadenas). Además ``SELECT *`` exponía columnas
internas. Aquí se define una única representación por recurso, con solo los
campos que la app necesita.
"""
from __future__ import annotations

from datetime import date, datetime, time
from decimal import Decimal

from .timeutils import fmt_date, fmt_datetime, fmt_time


def money(value) -> float:
    if value is None:
        return 0.0
    return float(Decimal(value).quantize(Decimal("0.01")))


def _iso_dt(value: datetime | None) -> str | None:
    return value.isoformat() if value else None


def client(row: dict) -> dict:
    return {
        "clienteid": row["clienteid"],
        "usuarioid": row["usuarioid"],
        "nombre": row["nombre"],
        "telefono": row["telefono"],
        "fecharegistro": fmt_date(row["fecharegistro"]),
        "ultimacita": fmt_datetime(row.get("ultimacita")),
        "email": row["email"],
        "nombreusuario": row["nombreusuario"],
    }


def barber_profile(row: dict) -> dict:
    return {
        "peluqueroid": row["peluqueroid"],
        "usuarioid": row["usuarioid"],
        "nombre": row["nombre"],
        "telefono": row["telefono"],
        "especialidad": row["especialidad"],
        "fechacontratacion": fmt_date(row["fechacontratacion"]),
        "localid": row["localid"],
        "email": row["email"],
        "nombreusuario": row["nombreusuario"],
        "rol": row["rol"],
    }


def barber_public(row: dict, *, include_contact: bool = False) -> dict:
    data = {
        "peluqueroid": row["peluqueroid"],
        "usuarioid": row["usuarioid"],
        "nombre": row["nombre"],
        "telefono": row["telefono"] if include_contact else None,
        "especialidad": row["especialidad"],
        "fechacontratacion": fmt_date(row["fechacontratacion"]),
        "localid": row["localid"],
        "rol": row["rol"],
    }
    if "activo" in row:
        data["activo"] = bool(row["activo"])
    return data


def barbershop(row: dict) -> dict:
    return {
        "localid": row["localid"],
        "nombre": row["nombre"],
        "direccion": row["direccion"],
        "telefono": row["telefono"],
        "horarioapertura": fmt_time(row["horarioapertura"]),
        "horariocierre": fmt_time(row["horariocierre"]),
        "descripcion": row["descripcion"],
        "localidad": row["localidad"],
        "rating": float(row["rating"]) if row.get("rating") is not None else None,
        "cantidad_resenas": row.get("cantidad_resenas") or 0,
        "es_favorito": bool(row.get("es_favorito")),
        "imagen_url": row.get("imagen_url"),
    }


def service(row: dict) -> dict:
    return {
        "servicioid": row["servicioid"],
        "nombre": row["nombre"],
        "descripcion": row["descripcion"],
        "duracion": row["duracion"],
        "precio": money(row["precio"]),
        "localid": row["localid"],
    }


def review(row: dict) -> dict:
    return {
        "resenaid": row["resenaid"],
        "clienteid": row["clienteid"],
        "peluqueroid": row["peluqueroid"],
        "calificacion": row["calificacion"],
        "comentario": row["comentario"],
        "fecharesena": _iso_dt(row["fecharesena"]),
        "localid": row["localid"],
        "cliente_nombre": row.get("cliente_nombre"),
    }


def schedule_entry(row: dict) -> dict:
    return {
        "diasemana": row["diasemana"],
        "horainicio": fmt_time(row["horainicio"]),
        "horafin": fmt_time(row["horafin"]),
    }


def blocked_hours(row: dict) -> dict:
    return {
        "fecha": fmt_date(row["fecha"]),
        "horainicio": fmt_time(row["horainicio"]),
        "horafin": fmt_time(row["horafin"]),
        "motivo": row["motivo"],
    }


def client_appointment(row: dict, now: datetime) -> dict:
    return {
        "citaid": row["citaid"],
        "clienteid": row["clienteid"],
        "peluqueroid": row["peluqueroid"],
        "servicioid": row["servicioid"],
        "fechainicio": fmt_datetime(row["fechainicio"]),
        "fechafin": fmt_datetime(row["fechafin"]),
        "estado": row["estado"],
        "localid": row["localid"],
        "servicio_nombre": row["servicio_nombre"],
        "servicio_precio": money(row["servicio_precio"]),
        "servicio_duracion": row["servicio_duracion"],
        "barber_nombre": row["barber_nombre"],
        "local_nombre": row["local_nombre"],
        "local_direccion": row["local_direccion"],
        "finalizada": row["fechafin"] < now,
    }


def barber_appointment(row: dict, now: datetime) -> dict:
    return {
        "citaid": row["citaid"],
        "clienteid": row["clienteid"],
        "peluqueroid": row["peluqueroid"],
        "servicioid": row["servicioid"],
        "fechainicio": fmt_datetime(row["fechainicio"]),
        "fechafin": fmt_datetime(row["fechafin"]),
        "estado": row["estado"],
        "localid": row["localid"],
        "servicio_nombre": row["servicio_nombre"],
        "duracion": row["duracion"],
        "precio": money(row["precio"]),
        "cliente_nombre": row["cliente_nombre"],
        "cliente_telefono": row["cliente_telefono"],
        "finalizada": row["fechafin"] < now,
    }


def activity(row: dict) -> dict:
    return {
        "actividadid": row["actividadid"],
        "tipo": row["tipo"],
        "fecha": fmt_datetime(row["fecha"]),
        "cliente_nombre": row["cliente_nombre"],
        "fechainicio": fmt_datetime(row["fechainicio"]),
        "fechafin": fmt_datetime(row["fechafin"]),
        "estado": row["estado"],
        "servicio_nombre": row["servicio_nombre"],
        "local_nombre": row["local_nombre"],
    }


def jsonable(value):
    """Conversión genérica de último recurso para el proveedor JSON."""
    if isinstance(value, Decimal):
        return float(value)
    if isinstance(value, datetime):
        return fmt_datetime(value)
    if isinstance(value, date):
        return value.isoformat()
    if isinstance(value, time):
        return fmt_time(value)
    raise TypeError(f"Tipo no serializable: {type(value).__name__}")
