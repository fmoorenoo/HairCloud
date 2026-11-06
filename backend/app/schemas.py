"""Esquemas de validación de entrada (Pydantic v2).

El servidor no confía en ninguna validación del cliente: todas las reglas
(longitudes, formatos, política de contraseñas, rangos) se comprueban aquí.
Los campos desconocidos se rechazan para evitar asignaciones masivas.
"""
from __future__ import annotations

import re
from datetime import date, datetime, time
from decimal import Decimal
from typing import Annotated, Literal

from flask import request
from pydantic import (AfterValidator, BaseModel, BeforeValidator, ConfigDict,
                      EmailStr, Field, StringConstraints, field_validator, model_validator)

from .errors import BadRequest
from .timeutils import WEEKDAYS_ES

USERNAME_RE = re.compile(r"^[\w.]{6,20}$")
PHONE_RE = re.compile(r"^\d{9,15}$")
CODE_RE = re.compile(r"^\d{6}$")

APPOINTMENT_STATES = ("Pendiente", "Completada", "Cancelada", "No completada")


def _strip(value):
    return value.strip() if isinstance(value, str) else value


def _empty_to_none(value):
    if isinstance(value, str):
        value = value.strip()
        # "nulo" era el centinela que usaba la app antigua para borrar campos.
        if value == "" or value.lower() == "nulo":
            return None
    return value


def _check_password(value: str) -> str:
    if len(value) < 8:
        raise ValueError("La contraseña debe tener al menos 8 caracteres")
    if len(value.encode("utf-8")) > 72:
        raise ValueError("La contraseña es demasiado larga (máximo 72 bytes)")
    if not any(c.isalpha() for c in value) or not any(c.isdigit() for c in value):
        raise ValueError("La contraseña debe contener letras y números")
    if any(c.isspace() for c in value):
        raise ValueError("La contraseña no puede contener espacios")
    return value


def _check_username(value: str) -> str:
    if not USERNAME_RE.match(value):
        raise ValueError(
            "El nombre de usuario debe tener 6-20 caracteres: letras, números, '.' o '_'")
    return value


def _check_phone(value: str | None) -> str | None:
    if value is not None and not PHONE_RE.match(value):
        raise ValueError("El teléfono debe tener entre 9 y 15 dígitos")
    return value


def _lower(value: str) -> str:
    return value.lower()


Str = Annotated[str, BeforeValidator(_strip)]
Password = Annotated[str, AfterValidator(_check_password)]
Username = Annotated[str, BeforeValidator(_strip), AfterValidator(_check_username)]
Email = Annotated[EmailStr, BeforeValidator(_strip), AfterValidator(_lower), Field(max_length=100)]
OptionalPhone = Annotated[str | None, BeforeValidator(_empty_to_none), AfterValidator(_check_phone)]
OptionalText = Annotated[str | None, BeforeValidator(_empty_to_none)]


def opt_text(max_length: int):
    """Texto opcional: cadena vacía -> None; longitud máxima solo si hay valor."""
    return Annotated[Annotated[str, StringConstraints(max_length=max_length)] | None,
                     BeforeValidator(_empty_to_none)]
PersonName = Annotated[str, BeforeValidator(_strip), Field(min_length=2, max_length=100)]


class Schema(BaseModel):
    model_config = ConfigDict(extra="forbid", str_max_length=2000)


def parse(schema: type[Schema], data=None) -> Schema:
    """Valida el cuerpo JSON de la petición contra ``schema``."""
    if data is None:
        data = request.get_json(silent=True)
        if data is None:
            raise BadRequest("El cuerpo de la petición debe ser JSON válido", code="invalid_json")
    if not isinstance(data, dict):
        raise BadRequest("Se esperaba un objeto JSON", code="invalid_json")
    return schema.model_validate(data)


# --------------------------------------------------------------------------
# Autenticación
# --------------------------------------------------------------------------
class LoginIn(Schema):
    nombreusuario: Annotated[str, BeforeValidator(_strip), Field(min_length=1, max_length=100)]
    password: Annotated[str, Field(min_length=1, max_length=200)]


class RefreshIn(Schema):
    refresh_token: Annotated[str, Field(min_length=20, max_length=200)]


class RegisterIn(Schema):
    nombre_completo: PersonName
    email: Email
    nombreusuario: Username
    password: Password
    verification_token: Annotated[str, Field(min_length=20, max_length=2000)]


class SendCodeIn(Schema):
    email: Email
    purpose: Literal["email_verification", "password_reset"]


class VerifyCodeIn(Schema):
    email: Email
    codigo: Annotated[str, BeforeValidator(_strip)]
    purpose: Literal["email_verification", "password_reset"]

    @field_validator("codigo")
    @classmethod
    def _code(cls, v: str) -> str:
        if not CODE_RE.match(v):
            raise ValueError("El código debe tener 6 dígitos")
        return v


class ResetPasswordIn(Schema):
    reset_token: Annotated[str, Field(min_length=20, max_length=2000)]
    password: Password


class ChangePasswordIn(Schema):
    current_password: Annotated[str, Field(min_length=1, max_length=200)]
    new_password: Password


# --------------------------------------------------------------------------
# Perfiles
# --------------------------------------------------------------------------
class ClientUpdateIn(Schema):
    email: Email | None = None
    nombreusuario: Username | None = None
    nombre: PersonName | None = None
    telefono: OptionalPhone = None


class BarberUpdateIn(Schema):
    email: Email | None = None
    nombreusuario: Username | None = None
    nombre: PersonName | None = None
    telefono: OptionalPhone = None
    especialidad: opt_text(100) = None


# --------------------------------------------------------------------------
# Horarios
# --------------------------------------------------------------------------
class ScheduleItem(Schema):
    dia: Literal[WEEKDAYS_ES]  # type: ignore[valid-type]
    inicio: time
    fin: time

    @model_validator(mode="after")
    def _order(self):
        if self.inicio >= self.fin:
            raise ValueError(f"{self.dia}: la hora de inicio debe ser anterior a la de fin")
        return self


def validate_schedule(items: list[ScheduleItem]) -> list[ScheduleItem]:
    by_day: dict[str, list[ScheduleItem]] = {}
    for item in items:
        by_day.setdefault(item.dia, []).append(item)
    for dia, ranges in by_day.items():
        ranges.sort(key=lambda r: r.inicio)
        for prev, cur in zip(ranges, ranges[1:]):
            if cur.inicio < prev.fin:
                raise BadRequest(f"{dia}: los tramos horarios se solapan", code="schedule_overlap")
    return items


class ScheduleIn(Schema):
    horario: Annotated[list[ScheduleItem], Field(max_length=28)]


# --------------------------------------------------------------------------
# Personal
# --------------------------------------------------------------------------
class BarberCreateIn(Schema):
    nombreusuario: Username
    contrasena: Password
    email: Email
    nombre: PersonName
    especialidad: opt_text(100) = None
    horario: Annotated[list[ScheduleItem], Field(min_length=1, max_length=28)]


class BarberActiveIn(Schema):
    activo: bool


class BarberRoleIn(Schema):
    rol: Literal["peluquero", "semiadmin"]


# --------------------------------------------------------------------------
# Locales, servicios, reseñas
# --------------------------------------------------------------------------
class BarbershopUpdateIn(Schema):
    nombre: Annotated[Str, Field(min_length=2, max_length=100)] | None = None
    direccion: Annotated[Str, Field(min_length=3, max_length=255)] | None = None
    telefono: Annotated[str, BeforeValidator(_strip), AfterValidator(_check_phone)] | None = None
    horarioapertura: time | None = None
    horariocierre: time | None = None
    descripcion: opt_text(2000) = None


class ServiceIn(Schema):
    nombre: Annotated[Str, Field(min_length=2, max_length=100)]
    descripcion: opt_text(1000) = None
    duracion: Annotated[int, Field(ge=5, le=480)]
    precio: Annotated[Decimal, Field(ge=0, le=10000, max_digits=7, decimal_places=2)]


class ReviewIn(Schema):
    calificacion: Annotated[int, Field(ge=1, le=5)]
    comentario: opt_text(1000) = None

    @field_validator("calificacion", mode="before")
    @classmethod
    def _rating(cls, v):
        # La app enviaba la nota como Double (p.ej. 4.0).
        if isinstance(v, float) and v.is_integer():
            return int(v)
        return v


# --------------------------------------------------------------------------
# Citas
# --------------------------------------------------------------------------
class AppointmentCreateIn(Schema):
    peluqueroid: Annotated[int, Field(gt=0)]
    servicioid: Annotated[int, Field(gt=0)]
    fechainicio: datetime

    @field_validator("fechainicio", mode="before")
    @classmethod
    def _parse(cls, v):
        if isinstance(v, str):
            try:
                return datetime.strptime(v.strip(), "%Y-%m-%d %H:%M")
            except ValueError:
                pass
        return v

    @field_validator("fechainicio")
    @classmethod
    def _naive(cls, v: datetime) -> datetime:
        return v.replace(tzinfo=None, second=0, microsecond=0)


class AppointmentStatusIn(Schema):
    estado: Literal[APPOINTMENT_STATES]  # type: ignore[valid-type]
    motivo: opt_text(500) = None


class DateRangeQuery(Schema):
    start: date
    end: date

    @model_validator(mode="after")
    def _range(self):
        if self.end < self.start:
            raise ValueError("La fecha final no puede ser anterior a la inicial")
        if (self.end - self.start).days > 366:
            raise ValueError("El rango máximo es de un año")
        return self


class StatsEmailIn(Schema):
    start_date: date
    end_date: date


def parse_date_param(name: str, required: bool = True) -> date | None:
    raw = request.args.get(name)
    if raw in (None, ""):
        if required:
            raise BadRequest(f"Falta el parámetro '{name}'", code="missing_param")
        return None
    try:
        return date.fromisoformat(raw)
    except ValueError:
        raise BadRequest(f"'{name}' debe tener formato YYYY-MM-DD", code="invalid_param")


def parse_int_param(name: str, *, required: bool = True, minimum: int = 1,
                    maximum: int = 10**9) -> int | None:
    raw = request.args.get(name)
    if raw in (None, ""):
        if required:
            raise BadRequest(f"Falta el parámetro '{name}'", code="missing_param")
        return None
    try:
        value = int(raw)
    except ValueError:
        raise BadRequest(f"'{name}' debe ser un número entero", code="invalid_param")
    if not minimum <= value <= maximum:
        raise BadRequest(f"'{name}' está fuera de rango", code="invalid_param")
    return value
