"""Servicios ofrecidos por un local."""
from __future__ import annotations

from flask import Blueprint, jsonify

from .. import serializers as ser
from ..db import fetch_one, transaction
from ..errors import Conflict, NotFound
from ..schemas import ServiceIn, parse
from ..security.auth import SERVICE_MANAGERS, current_user, require_auth

bp = Blueprint("services", __name__, url_prefix="/services")


def _service_for_update(cur, servicioid: int) -> dict:
    cur.execute("SELECT servicioid, localid, nombre FROM servicios WHERE servicioid = %s AND activo FOR UPDATE",
                (servicioid,))
    row = cur.fetchone()
    if row is None:
        raise NotFound("Servicio no encontrado")
    current_user().require_local_role(row["localid"], SERVICE_MANAGERS)
    return row


@bp.get("/<int:servicioid>")
@require_auth()
def get_service(servicioid: int):
    row = fetch_one(
        "SELECT servicioid, nombre, descripcion, duracion, precio, localid FROM servicios WHERE servicioid = %s AND activo",
        (servicioid,),
    )
    if row is None:
        raise NotFound("Servicio no encontrado")
    return jsonify(ser.service(row)), 200


@bp.put("/<int:servicioid>")
@require_auth(*SERVICE_MANAGERS)
def edit_service(servicioid: int):
    data = parse(ServiceIn)
    with transaction() as cur:
        svc = _service_for_update(cur, servicioid)
        cur.execute(
            """
            SELECT 1 FROM servicios
            WHERE localid = %s AND lower(nombre) = lower(%s) AND activo AND servicioid <> %s
            """,
            (svc["localid"], data.nombre, servicioid),
        )
        if cur.fetchone():
            raise Conflict("Ya existe un servicio con ese nombre", code="service_exists")
        # El local de un servicio no se puede cambiar (antes se aceptaba
        # "localId" del cliente y se podía mover un servicio a otro local).
        cur.execute(
            "UPDATE servicios SET nombre = %s, descripcion = %s, duracion = %s, precio = %s WHERE servicioid = %s",
            (data.nombre, data.descripcion, data.duracion, data.precio, servicioid),
        )
    return jsonify({"message": "Servicio actualizado", "username": None}), 200


@bp.delete("/<int:servicioid>")
@require_auth(*SERVICE_MANAGERS)
def delete_service(servicioid: int):
    with transaction() as cur:
        _service_for_update(cur, servicioid)
        # Baja lógica: antes el DELETE borraba en cascada todas las citas
        # (pasadas y futuras) de ese servicio, destruyendo el historial.
        cur.execute("UPDATE servicios SET activo = FALSE WHERE servicioid = %s", (servicioid,))
    return jsonify({"message": "Servicio eliminado", "username": None}), 200
