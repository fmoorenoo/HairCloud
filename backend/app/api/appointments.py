"""Reserva y gestión del estado de citas."""
from __future__ import annotations

from flask import Blueprint, jsonify

from ..extensions import limiter
from ..schemas import AppointmentCreateIn, AppointmentStatusIn, parse
from ..security.auth import CLIENT, current_user, require_auth, require_client
from ..services import appointments as appt

bp = Blueprint("appointments", __name__, url_prefix="/appointments")


@bp.post("")
@require_auth(CLIENT)
@limiter.limit("20 per hour")
def create_appointment():
    user = require_client()
    data = parse(AppointmentCreateIn)
    result = appt.create_appointment(user, data.peluqueroid, data.servicioid, data.fechainicio)
    return jsonify({"message": "Cita creada con éxito", "username": None, **result}), 201


@bp.patch("/<int:citaid>/status")
@require_auth()
def change_status(citaid: int):
    data = parse(AppointmentStatusIn)
    message = appt.change_status(current_user(), citaid, data.estado, data.motivo)
    return jsonify({"message": message, "username": None}), 200
