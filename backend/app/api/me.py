"""Recursos del usuario autenticado (``/me``).

Sustituyen a rutas como ``/clients/get_client/<id>`` o
``/clients/update_client/<id>`` en las que bastaba cambiar el número para
leer o modificar la cuenta de otra persona.
"""
from __future__ import annotations

from flask import Blueprint, jsonify

from .. import serializers as ser
from ..db import fetch_all, fetch_one, transaction
from ..errors import Conflict, NotFound
from ..schemas import BarberUpdateIn, ClientUpdateIn, parse
from ..security.audit import audit
from ..security.auth import CLIENT, STAFF_ROLES, current_user, require_auth, require_client
from ..services import appointments as appt
from ..services import stats as stats_service
from ..timeutils import now_local
from .barbershops import BARBERSHOP_SELECT

bp = Blueprint("me", __name__, url_prefix="/me")


def _update_account(cur, usuarioid: int, email, nombreusuario) -> None:
    if email is not None:
        cur.execute("SELECT 1 FROM usuarios WHERE lower(email) = %s AND usuarioid <> %s", (email, usuarioid))
        if cur.fetchone():
            raise Conflict("El email ya está en uso por otro usuario", code="email_taken")
    if nombreusuario is not None:
        cur.execute("SELECT 1 FROM usuarios WHERE lower(nombreusuario) = lower(%s) AND usuarioid <> %s",
                    (nombreusuario, usuarioid))
        if cur.fetchone():
            raise Conflict("El nombre de usuario ya está en uso", code="username_taken")
    cur.execute(
        """
        UPDATE usuarios SET email = COALESCE(%s, email), nombreusuario = COALESCE(%s, nombreusuario)
        WHERE usuarioid = %s
        """,
        (email, nombreusuario, usuarioid),
    )


def _check_phone_unique(cur, table: str, id_col: str, phone, own_id: int) -> None:
    if phone is None:
        return
    cur.execute(f"SELECT 1 FROM {table} WHERE telefono = %s AND {id_col} <> %s", (phone, own_id))
    if cur.fetchone():
        raise Conflict("Ese teléfono ya está registrado en otra cuenta", code="phone_taken")


# --------------------------------------------------------------------------
# Cliente
# --------------------------------------------------------------------------
CLIENT_SELECT = """
    SELECT c.clienteid, c.usuarioid, c.nombre, c.telefono, c.fecharegistro, u.email, u.nombreusuario,
           (SELECT max(fechainicio) FROM v_citas v
             WHERE v.clienteid = c.clienteid AND v.estado_efectivo = 'Completada') AS ultimacita
    FROM clientes c JOIN usuarios u ON c.usuarioid = u.usuarioid
    WHERE c.usuarioid = %s
"""


@bp.get("/client")
@require_auth(CLIENT)
def get_client():
    user = require_client()
    row = fetch_one(CLIENT_SELECT, (user.usuarioid,))
    if row is None:
        raise NotFound("Cliente no encontrado")
    return jsonify(ser.client(row)), 200


@bp.patch("/client")
@require_auth(CLIENT)
def update_client():
    user = require_client()
    data = parse(ClientUpdateIn)
    fields = data.model_fields_set
    with transaction() as cur:
        _update_account(cur, user.usuarioid, data.email, data.nombreusuario)
        if "telefono" in fields:
            _check_phone_unique(cur, "clientes", "clienteid", data.telefono, user.clienteid)
            cur.execute("UPDATE clientes SET telefono = %s WHERE clienteid = %s", (data.telefono, user.clienteid))
        if data.nombre is not None:
            cur.execute("UPDATE clientes SET nombre = %s WHERE clienteid = %s", (data.nombre, user.clienteid))
    if data.email is not None and data.email != user.email:
        audit("email_changed", usuarioid=user.usuarioid)
    return jsonify({"message": "Info actualizada correctamente", "username": None}), 200


@bp.get("/appointments")
@require_auth(CLIENT)
def client_appointments():
    user = require_client()
    now = now_local()
    rows = appt.client_appointments(user.clienteid)
    return jsonify({"appointments": [ser.client_appointment(r, now) for r in rows]}), 200


@bp.get("/stats")
@require_auth(CLIENT)
def client_stats():
    user = require_client()
    return jsonify(stats_service.client_stats(user.clienteid)), 200


@bp.get("/favorites")
@require_auth(CLIENT)
def favorites():
    user = require_client()
    rows = fetch_all(
        BARBERSHOP_SELECT + " WHERE fc.localid IS NOT NULL GROUP BY l.localid, fc.localid ORDER BY l.localid",
        {"cliente": user.clienteid},
    )
    return jsonify([ser.barbershop(r) for r in rows]), 200


@bp.put("/favorites/<int:localid>")
@require_auth(CLIENT)
def add_favorite(localid: int):
    user = require_client()
    with transaction() as cur:
        cur.execute("SELECT 1 FROM local WHERE localid = %s", (localid,))
        if cur.fetchone() is None:
            raise NotFound("Barbería no encontrada")
        cur.execute(
            """
            INSERT INTO favoritos_clientes (clienteid, localid, fecha_agregado) VALUES (%s, %s, %s)
            ON CONFLICT (clienteid, localid) DO NOTHING
            """,
            (user.clienteid, localid, now_local()),
        )
    return jsonify({"message": "Favorito agregado", "username": None}), 200


@bp.delete("/favorites/<int:localid>")
@require_auth(CLIENT)
def remove_favorite(localid: int):
    user = require_client()
    with transaction() as cur:
        cur.execute("DELETE FROM favoritos_clientes WHERE clienteid = %s AND localid = %s",
                    (user.clienteid, localid))
    return jsonify({"message": "Favorito eliminado", "username": None}), 200


# --------------------------------------------------------------------------
# Personal
# --------------------------------------------------------------------------
BARBER_SELECT = """
    SELECT p.peluqueroid, p.usuarioid, p.nombre, p.telefono, p.especialidad, p.fechacontratacion,
           p.localid, u.email, u.nombreusuario, u.rol
    FROM peluqueros p JOIN usuarios u ON p.usuarioid = u.usuarioid
    WHERE p.usuarioid = %s
"""


@bp.get("/barber")
@require_auth(*STAFF_ROLES)
def get_barber():
    user = current_user()
    row = fetch_one(BARBER_SELECT, (user.usuarioid,))
    if row is None:
        raise NotFound("Peluquero no encontrado")
    return jsonify(ser.barber_profile(row)), 200


@bp.patch("/barber")
@require_auth(*STAFF_ROLES)
def update_barber():
    user = current_user()
    if user.peluqueroid is None:
        raise NotFound("Peluquero no encontrado")
    data = parse(BarberUpdateIn)
    fields = data.model_fields_set
    with transaction() as cur:
        _update_account(cur, user.usuarioid, data.email, data.nombreusuario)
        if "telefono" in fields:
            _check_phone_unique(cur, "peluqueros", "peluqueroid", data.telefono, user.peluqueroid)
            cur.execute("UPDATE peluqueros SET telefono = %s WHERE peluqueroid = %s",
                        (data.telefono, user.peluqueroid))
        if "especialidad" in fields:
            cur.execute("UPDATE peluqueros SET especialidad = %s WHERE peluqueroid = %s",
                        (data.especialidad, user.peluqueroid))
        if data.nombre is not None:
            cur.execute("UPDATE peluqueros SET nombre = %s WHERE peluqueroid = %s",
                        (data.nombre, user.peluqueroid))
    return jsonify({"message": "Info actualizada correctamente", "username": None}), 200
