"""Locales (barberías) y sus recursos anidados: servicios, reseñas y personal."""
from __future__ import annotations

from flask import Blueprint, current_app, jsonify

from .. import serializers as ser
from ..db import fetch_all, fetch_one, transaction
from ..errors import BadRequest, Conflict, NotFound
from ..schemas import BarberCreateIn, BarbershopUpdateIn, ReviewIn, ServiceIn, parse, validate_schedule
from ..security.audit import audit
from ..security.auth import (CLIENT, LOCAL_MANAGERS, SERVICE_MANAGERS, STAFF_ROLES, current_user,
                             require_auth, require_client)
from ..security.passwords import hash_password
from ..timeutils import now_local

bp = Blueprint("barbershops", __name__, url_prefix="/barbershops")

BARBERSHOP_SELECT = """
    SELECT l.localid, l.nombre, l.direccion, l.telefono, l.horarioapertura, l.horariocierre,
           l.descripcion, l.localidad, l.imagen_url,
           ROUND(AVG(r.calificacion)::numeric, 1) AS rating,
           COUNT(r.resenaid) AS cantidad_resenas,
           (fc.localid IS NOT NULL) AS es_favorito
    FROM local l
    LEFT JOIN resenas r ON l.localid = r.localid AND r.peluqueroid IS NULL
    LEFT JOIN favoritos_clientes fc ON fc.localid = l.localid AND fc.clienteid = %(cliente)s
"""


def _local_or_404(cur, localid: int) -> dict:
    cur.execute("SELECT localid, horarioapertura, horariocierre FROM local WHERE localid = %s", (localid,))
    row = cur.fetchone()
    if row is None:
        raise NotFound("Barbería no encontrada")
    return row


@bp.get("")
@require_auth()
def list_barbershops():
    user = current_user()
    rows = fetch_all(BARBERSHOP_SELECT + " GROUP BY l.localid, fc.localid ORDER BY l.localid",
                     {"cliente": user.clienteid})
    return jsonify([ser.barbershop(r) for r in rows]), 200


@bp.get("/<int:localid>")
@require_auth()
def get_barbershop(localid: int):
    user = current_user()
    row = fetch_one(BARBERSHOP_SELECT + " WHERE l.localid = %(local)s GROUP BY l.localid, fc.localid",
                    {"cliente": user.clienteid, "local": localid})
    if row is None:
        raise NotFound("Barbería no encontrada")
    return jsonify(ser.barbershop(row)), 200


@bp.patch("/<int:localid>")
@require_auth(*LOCAL_MANAGERS)
def update_barbershop(localid: int):
    current_user().require_local_role(localid, LOCAL_MANAGERS)
    data = parse(BarbershopUpdateIn)
    changes = {k: getattr(data, k) for k in data.model_fields_set}
    # Campos obligatorios en BD: no se permite vaciarlos.
    for required in ("nombre", "direccion", "telefono", "horarioapertura", "horariocierre"):
        if required in changes and changes[required] is None:
            raise BadRequest(f"El campo '{required}' no puede estar vacío", code="validation_error")
    if not changes:
        raise BadRequest("No hay datos para actualizar", code="empty_update")

    with transaction() as cur:
        current = _local_or_404(cur, localid)
        opening = changes.get("horarioapertura", current["horarioapertura"])
        closing = changes.get("horariocierre", current["horariocierre"])
        if opening >= closing:
            raise BadRequest("La hora de apertura debe ser anterior a la de cierre", code="invalid_hours")
        if "telefono" in changes:
            cur.execute("SELECT 1 FROM local WHERE telefono = %s AND localid <> %s",
                        (changes["telefono"], localid))
            if cur.fetchone():
                raise Conflict("Ese teléfono ya pertenece a otro local", code="phone_taken")
        # Los nombres de columna salen de una lista blanca (el esquema), nunca del cliente.
        allowed = ("nombre", "direccion", "telefono", "horarioapertura", "horariocierre", "descripcion")
        sets = ", ".join(f"{col} = %s" for col in allowed if col in changes)
        values = [changes[col] for col in allowed if col in changes]
        cur.execute(f"UPDATE local SET {sets} WHERE localid = %s", (*values, localid))
    audit("barbershop_updated", actor=current_user().usuarioid, detalles={"localid": localid,
                                                                       "campos": sorted(changes)})
    return jsonify({"message": "Info actualizada correctamente", "username": None}), 200


# --------------------------------------------------------------------------
# Servicios
# --------------------------------------------------------------------------
@bp.get("/<int:localid>/services")
@require_auth()
def list_services(localid: int):
    rows = fetch_all(
        """
        SELECT servicioid, nombre, descripcion, duracion, precio, localid
        FROM servicios WHERE localid = %s AND activo ORDER BY servicioid
        """,
        (localid,),
    )
    return jsonify([ser.service(r) for r in rows]), 200


@bp.post("/<int:localid>/services")
@require_auth(*SERVICE_MANAGERS)
def create_service(localid: int):
    current_user().require_local_role(localid, SERVICE_MANAGERS)
    data = parse(ServiceIn)
    with transaction() as cur:
        _local_or_404(cur, localid)
        cur.execute("SELECT 1 FROM servicios WHERE localid = %s AND lower(nombre) = lower(%s) AND activo",
                    (localid, data.nombre))
        if cur.fetchone():
            raise Conflict("Ya existe un servicio con ese nombre", code="service_exists")
        cur.execute(
            """
            INSERT INTO servicios (nombre, descripcion, duracion, precio, localid, activo)
            VALUES (%s, %s, %s, %s, %s, TRUE) RETURNING servicioid
            """,
            (data.nombre, data.descripcion, data.duracion, data.precio, localid),
        )
        servicioid = cur.fetchone()["servicioid"]
    return jsonify({"message": f'Servicio "{data.nombre}" creado', "servicioid": servicioid,
                    "username": None}), 201


# --------------------------------------------------------------------------
# Reseñas
# --------------------------------------------------------------------------
@bp.get("/<int:localid>/reviews")
@require_auth()
def list_reviews(localid: int):
    rows = fetch_all(
        """
        SELECT r.resenaid, r.clienteid, r.peluqueroid, r.calificacion, r.comentario, r.fecharesena,
               r.localid, c.nombre AS cliente_nombre
        FROM resenas r JOIN clientes c ON r.clienteid = c.clienteid
        WHERE r.localid = %s AND r.peluqueroid IS NULL
        ORDER BY r.fecharesena DESC
        """,
        (localid,),
    )
    return jsonify([ser.review(r) for r in rows]), 200


@bp.post("/<int:localid>/reviews")
@require_auth(CLIENT)
def add_review(localid: int):
    user = require_client()
    data = parse(ReviewIn)
    max_reviews = current_app.config["MAX_REVIEWS_PER_LOCAL"]
    with transaction() as cur:
        _local_or_404(cur, localid)
        # Bloqueo por cliente para que el límite no se pueda saltar con peticiones simultáneas.
        cur.execute("SELECT clienteid FROM clientes WHERE clienteid = %s FOR UPDATE", (user.clienteid,))
        cur.execute(
            "SELECT count(*) AS n FROM resenas WHERE clienteid = %s AND localid = %s AND peluqueroid IS NULL",
            (user.clienteid, localid),
        )
        if cur.fetchone()["n"] >= max_reviews:
            raise Conflict(f"Ya tienes {max_reviews} reseñas en esta barbería", code="review_limit")
        cur.execute(
            """
            INSERT INTO resenas (clienteid, localid, calificacion, comentario, fecharesena, peluqueroid)
            VALUES (%s, %s, %s, %s, %s, NULL) RETURNING resenaid
            """,
            (user.clienteid, localid, data.calificacion, data.comentario or "", now_local()),
        )
        resenaid = cur.fetchone()["resenaid"]
    return jsonify({"message": "Reseña añadida correctamente", "resenaid": resenaid, "username": None}), 201


# --------------------------------------------------------------------------
# Personal del local
# --------------------------------------------------------------------------
BARBERS_SELECT = """
    SELECT p.peluqueroid, p.usuarioid, p.nombre, p.telefono, p.especialidad, p.fechacontratacion,
           p.localid, COALESCE(p.activo, TRUE) AS activo, u.rol
    FROM peluqueros p JOIN usuarios u ON p.usuarioid = u.usuarioid
    WHERE p.localid = %s AND COALESCE(p.activo, TRUE) = %s
    ORDER BY p.nombre
"""


@bp.get("/<int:localid>/barbers")
@require_auth()
def list_barbers(localid: int):
    user = current_user()
    # El teléfono del personal solo lo ven sus compañeros del mismo local.
    include_contact = user.has_local_role(localid, STAFF_ROLES)
    rows = fetch_all(BARBERS_SELECT, (localid, True))
    out = []
    for r in rows:
        item = ser.barber_public(r, include_contact=include_contact)
        item.pop("activo", None)
        out.append(item)
    return jsonify(out), 200


@bp.get("/<int:localid>/barbers/inactive")
@require_auth(*LOCAL_MANAGERS)
def list_inactive_barbers(localid: int):
    current_user().require_local_role(localid, LOCAL_MANAGERS)
    rows = fetch_all(BARBERS_SELECT, (localid, False))
    return jsonify([ser.barber_public(r, include_contact=True) for r in rows]), 200


@bp.post("/<int:localid>/barbers")
@require_auth(*LOCAL_MANAGERS)
def create_barber(localid: int):
    admin = current_user()
    admin.require_local_role(localid, LOCAL_MANAGERS)
    data = parse(BarberCreateIn)
    validate_schedule(data.horario)
    now = now_local()
    with transaction() as cur:
        _local_or_404(cur, localid)
        cur.execute("SELECT 1 FROM usuarios WHERE lower(nombreusuario) = lower(%s) OR lower(email) = %s",
                    (data.nombreusuario, data.email))
        if cur.fetchone():
            raise Conflict("El nombre de usuario o email ya existen", code="user_exists")
        cur.execute(
            """
            INSERT INTO usuarios (nombreusuario, password_hash, rol, localid, email, created_at)
            VALUES (%s, %s, 'peluquero', %s, %s, %s) RETURNING usuarioid
            """,
            (data.nombreusuario, hash_password(data.contrasena), localid, data.email, now),
        )
        usuarioid = cur.fetchone()["usuarioid"]
        cur.execute(
            """
            INSERT INTO peluqueros (usuarioid, nombre, telefono, especialidad, fechacontratacion, localid, activo)
            VALUES (%s, %s, NULL, %s, %s, %s, TRUE) RETURNING peluqueroid
            """,
            (usuarioid, data.nombre, data.especialidad, now.date(), localid),
        )
        peluqueroid = cur.fetchone()["peluqueroid"]
        for item in data.horario:
            cur.execute(
                "INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin) VALUES (%s, %s, %s, %s)",
                (peluqueroid, item.dia, item.inicio, item.fin),
            )
    audit("barber_created", usuarioid=usuarioid, actor=admin.usuarioid, detalles={"localid": localid})
    return jsonify({"message": "Barbero añadido correctamente al personal", "usuarioid": usuarioid,
                    "peluqueroid": peluqueroid, "username": None}), 201
