"""Errores de dominio y manejadores HTTP homogéneos.

Todas las respuestas de error tienen la forma::

    {"error": "Mensaje legible para el usuario", "code": "codigo_maquina"}

y opcionalmente ``details`` con errores de validación por campo. Nunca se
devuelven trazas ni mensajes internos de la base de datos al cliente.
"""
from __future__ import annotations

import logging
import uuid

from flask import Flask, g, jsonify, request
from psycopg import errors as pg_errors
from pydantic import ValidationError
from werkzeug.exceptions import HTTPException

log = logging.getLogger(__name__)


class ApiError(Exception):
    status = 400
    code = "bad_request"

    def __init__(self, message: str, *, code: str | None = None, status: int | None = None,
                 details: dict | list | None = None, headers: dict | None = None):
        super().__init__(message)
        self.message = message
        if code:
            self.code = code
        if status:
            self.status = status
        self.details = details
        self.headers = headers or {}


class BadRequest(ApiError):
    status, code = 400, "bad_request"


class Unauthorized(ApiError):
    status, code = 401, "unauthorized"


class Forbidden(ApiError):
    status, code = 403, "forbidden"


class NotFound(ApiError):
    status, code = 404, "not_found"


class Conflict(ApiError):
    status, code = 409, "conflict"


class TooManyRequests(ApiError):
    status, code = 429, "too_many_requests"


def _payload(message: str, code: str, details=None):
    body = {"error": message, "code": code}
    if details:
        body["details"] = details
    return body


# Mensajes en español para los errores genéricos de Pydantic (los de las
# reglas propias ya vienen en español desde schemas.py).
_MESSAGES = {
    "missing": "Campo obligatorio",
    "extra_forbidden": "Campo no permitido",
    "string_too_short": "Texto demasiado corto",
    "string_too_long": "Texto demasiado largo",
    "string_type": "Debe ser un texto",
    "int_parsing": "Debe ser un número entero",
    "int_type": "Debe ser un número entero",
    "int_from_float": "Debe ser un número entero",
    "decimal_parsing": "Debe ser un número",
    "decimal_max_places": "Máximo 2 decimales",
    "greater_than": "Valor demasiado pequeño",
    "greater_than_equal": "Valor demasiado pequeño",
    "less_than_equal": "Valor demasiado grande",
    "literal_error": "Valor no permitido",
    "time_parsing": "Hora no válida (HH:MM)",
    "time_type": "Hora no válida (HH:MM)",
    "date_from_datetime_parsing": "Fecha no válida (YYYY-MM-DD)",
    "date_parsing": "Fecha no válida (YYYY-MM-DD)",
    "datetime_parsing": "Fecha y hora no válidas (YYYY-MM-DD HH:MM)",
    "datetime_from_date_parsing": "Fecha y hora no válidas (YYYY-MM-DD HH:MM)",
    "bool_parsing": "Debe ser verdadero o falso",
    "list_type": "Se esperaba una lista",
    "too_long": "Demasiados elementos",
    "too_short": "Faltan elementos",
}


def _format_validation(exc: ValidationError) -> list[dict]:
    out = []
    for err in exc.errors(include_url=False, include_input=False):
        loc = ".".join(str(p) for p in err.get("loc", ()) if p != "__root__")
        msg = err.get("msg", "Valor no válido")
        if msg.startswith("Value error, "):
            msg = msg[len("Value error, "):]
            if "email address" in msg:
                msg = "El email no es válido"
        else:
            msg = _MESSAGES.get(err.get("type", ""), "Valor no válido")
        out.append({"field": loc, "message": msg})
    return out


def register_error_handlers(app: Flask) -> None:
    @app.errorhandler(ApiError)
    def handle_api_error(exc: ApiError):
        resp = jsonify(_payload(exc.message, exc.code, exc.details))
        resp.status_code = exc.status
        for key, value in exc.headers.items():
            resp.headers[key] = value
        return resp

    @app.errorhandler(ValidationError)
    def handle_validation(exc: ValidationError):
        details = _format_validation(exc)
        message = details[0]["message"] if len(details) == 1 else "Datos no válidos"
        return jsonify(_payload(message, "validation_error", details)), 422

    @app.errorhandler(pg_errors.ExclusionViolation)
    def handle_overlap(exc):
        return jsonify(_payload("Ese horario acaba de ocuparse. Elige otra hora.",
                                "slot_taken")), 409

    @app.errorhandler(pg_errors.UniqueViolation)
    def handle_unique(exc):
        log.info("Violación de unicidad: %s", getattr(exc.diag, "constraint_name", "?"))
        return jsonify(_payload("El recurso ya existe", "conflict")), 409

    @app.errorhandler(pg_errors.ForeignKeyViolation)
    def handle_fk(exc):
        return jsonify(_payload("Referencia a un recurso inexistente o en uso",
                                "invalid_reference")), 409

    @app.errorhandler(pg_errors.CheckViolation)
    def handle_check(exc):
        return jsonify(_payload("Los datos no cumplen las reglas del sistema",
                                "constraint_violation")), 422

    @app.errorhandler(429)
    def handle_rate_limit(exc):
        return jsonify(_payload("Demasiadas peticiones. Inténtalo más tarde.",
                                "rate_limited")), 429

    @app.errorhandler(HTTPException)
    def handle_http(exc: HTTPException):
        messages = {
            404: "Recurso no encontrado",
            405: "Método no permitido",
            413: "Petición demasiado grande",
            415: "El cuerpo debe ser JSON",
        }
        message = messages.get(exc.code, exc.name)
        return jsonify(_payload(message, exc.name.lower().replace(" ", "_"))), exc.code

    @app.errorhandler(Exception)
    def handle_unexpected(exc: Exception):
        incident = uuid.uuid4().hex[:12]
        log.exception("Error no controlado [%s] en %s %s", incident, request.method, request.path)
        body = _payload("Error interno del servidor", "internal_error")
        body["incident"] = incident
        return jsonify(body), 500

    @app.before_request
    def _request_id():
        g.request_id = request.headers.get("X-Request-ID") or uuid.uuid4().hex
