"""Recursos de un peluquero: horario, disponibilidad, agenda, actividad y estadísticas."""
from __future__ import annotations

from datetime import timedelta

from flask import Blueprint, current_app, jsonify, request

from .. import serializers as ser
from ..db import fetch_all, transaction
from ..errors import BadRequest, Conflict, Forbidden
from ..extensions import limiter
from ..schemas import (BarberActiveIn, BarberRoleIn, DateRangeQuery, ScheduleIn, StatsEmailIn,
                       parse, parse_date_param, parse_int_param, validate_schedule)
from ..security.audit import audit
from ..security.auth import (ADMIN, LOCAL_MANAGERS, REVIEW_MODERATORS, STAFF_ROLES, current_user,
                             get_barber_or_404, require_auth, require_barber_access)
from ..security.tokens import revoke_all_sessions
from ..services import appointments as appt
from ..services import stats as stats_service
from ..timeutils import now_local

bp = Blueprint("barbers", __name__, url_prefix="/barbers")

# Quién, además del propio peluquero, puede ver su agenda y actividad.
AGENDA_VIEWERS = REVIEW_MODERATORS


@bp.get("/<int:peluqueroid>/schedule")
@require_auth()
def get_schedule(peluqueroid: int):
    get_barber_or_404(peluqueroid)
    rows = fetch_all(
        """
        SELECT diasemana, horainicio, horafin FROM horarios_peluqueros WHERE peluqueroid = %s
        ORDER BY array_position(ARRAY['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo']::text[],
                                diasemana::text), horainicio
        """,
        (peluqueroid,),
    )
    return jsonify([ser.schedule_entry(r) for r in rows]), 200


@bp.put("/<int:peluqueroid>/schedule")
@require_auth(*STAFF_ROLES)
def update_schedule(peluqueroid: int):
    require_barber_access(peluqueroid, manager_roles=LOCAL_MANAGERS)
    raw = request.get_json(silent=True)
    # Se acepta tanto {"horario": [...]} como la lista directa que enviaba la app.
    data = parse(ScheduleIn, {"horario": raw} if isinstance(raw, list) else raw)
    validate_schedule(data.horario)
    with transaction() as cur:
        cur.execute("DELETE FROM horarios_peluqueros WHERE peluqueroid = %s", (peluqueroid,))
        for item in data.horario:
            cur.execute(
                "INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin) VALUES (%s, %s, %s, %s)",
                (peluqueroid, item.dia, item.inicio, item.fin),
            )
    return jsonify({"message": "Horario actualizado correctamente", "username": None}), 200


@bp.get("/<int:peluqueroid>/blocked-hours")
@require_auth(*STAFF_ROLES)
def blocked_hours(peluqueroid: int):
    require_barber_access(peluqueroid, manager_roles=AGENDA_VIEWERS)
    rows = fetch_all(
        """
        SELECT fecha, horainicio, horafin, motivo FROM bloqueoshorarios
        WHERE peluqueroid = %s AND fecha >= %s ORDER BY fecha, horainicio
        """,
        (peluqueroid, now_local().date()),
    )
    return jsonify([ser.blocked_hours(r) for r in rows]), 200


@bp.get("/<int:peluqueroid>/availability")
@require_auth()
@limiter.limit("120 per minute")
def availability(peluqueroid: int):
    day = parse_date_param("fecha")
    servicioid = parse_int_param("servicioid", required=False)
    duracion = parse_int_param("duracion", required=False, minimum=5, maximum=480)
    if servicioid is None and duracion is None:
        raise BadRequest("Indica 'servicioid'", code="missing_param")
    slots = appt.available_slots(peluqueroid, day, servicioid=servicioid, duration_minutes=duracion)
    return jsonify(slots), 200


@bp.get("/<int:peluqueroid>/appointments")
@require_auth(*STAFF_ROLES)
def appointments(peluqueroid: int):
    require_barber_access(peluqueroid, manager_roles=AGENDA_VIEWERS)
    single = parse_date_param("date", required=False)
    if single:
        start = end = single
    else:
        rng = DateRangeQuery(start=parse_date_param("start"), end=parse_date_param("end"))
        start, end = rng.start, rng.end
    now = now_local()
    rows = appt.barber_appointments(peluqueroid, start, end)
    return jsonify({"dates": [ser.barber_appointment(r, now) for r in rows]}), 200


@bp.get("/<int:peluqueroid>/activity")
@require_auth(*STAFF_ROLES)
def activity(peluqueroid: int):
    require_barber_access(peluqueroid, manager_roles=AGENDA_VIEWERS)
    since = now_local() - timedelta(days=current_app.config["ACTIVITY_RETENTION_DAYS"])
    rows = fetch_all(
        """
        SELECT a.actividadid, a.tipo, a.fecha, c.nombre AS cliente_nombre,
               ci.fechainicio, ci.fechafin, ci.estado_efectivo AS estado,
               s.nombre AS servicio_nombre, l.nombre AS local_nombre
        FROM actividad_peluquero a
        JOIN v_citas ci ON a.citaid = ci.citaid
        JOIN clientes c ON a.clienteid = c.clienteid
        JOIN servicios s ON ci.servicioid = s.servicioid
        JOIN local l ON ci.localid = l.localid
        WHERE a.peluqueroid = %s AND a.fecha >= %s
        ORDER BY a.fecha DESC
        """,
        (peluqueroid, since),
    )
    return jsonify([ser.activity(r) for r in rows]), 200


@bp.get("/<int:peluqueroid>/stats")
@require_auth(*STAFF_ROLES)
def stats(peluqueroid: int):
    barber = require_barber_access(peluqueroid, manager_roles=LOCAL_MANAGERS)
    rng = DateRangeQuery(start=parse_date_param("start"), end=parse_date_param("end"))
    return jsonify(stats_service.barber_stats(peluqueroid, barber["localid"], rng.start, rng.end)), 200


@bp.post("/<int:peluqueroid>/stats/email")
@require_auth(*STAFF_ROLES)
@limiter.limit("5 per hour")
def email_stats(peluqueroid: int):
    user = current_user()
    if user.peluqueroid != peluqueroid:
        raise Forbidden("Solo puedes enviarte tus propias estadísticas")
    data = parse(StatsEmailIn)
    rng = DateRangeQuery(start=data.start_date, end=data.end_date)
    numbers = stats_service.barber_stats(peluqueroid, user.localid, rng.start, rng.end)
    stats_service.send_barber_stats(user.email, numbers, rng.start, rng.end)
    return jsonify({"message": "Estadísticas enviadas con éxito", "username": None}), 200


# --------------------------------------------------------------------------
# Gestión de personal (solo el admin del local)
# --------------------------------------------------------------------------
def _managed_barber(peluqueroid: int) -> dict:
    admin = current_user()
    barber = get_barber_or_404(peluqueroid)
    admin.require_local_role(barber["localid"], LOCAL_MANAGERS)
    if barber["usuarioid"] == admin.usuarioid:
        raise Forbidden("No puedes modificar tu propia cuenta desde la gestión de personal")
    if barber["rol"] == ADMIN and not admin.is_superadmin:
        raise Forbidden("No puedes modificar a otro administrador")
    return barber


@bp.patch("/<int:peluqueroid>/role")
@require_auth(*LOCAL_MANAGERS)
def change_role(peluqueroid: int):
    barber = _managed_barber(peluqueroid)
    data = parse(BarberRoleIn)
    if barber["rol"] == data.rol:
        return jsonify({"message": f"Rol actualizado a {data.rol}", "username": None}), 200
    with transaction() as cur:
        cur.execute("UPDATE usuarios SET rol = %s WHERE usuarioid = %s", (data.rol, barber["usuarioid"]))
    audit("role_changed", usuarioid=barber["usuarioid"], actor=current_user().usuarioid,
          detalles={"de": barber["rol"], "a": data.rol})
    return jsonify({"message": f"Rol actualizado a {data.rol}", "username": None}), 200


@bp.patch("/<int:peluqueroid>/active")
@require_auth(*LOCAL_MANAGERS)
def set_active(peluqueroid: int):
    barber = _managed_barber(peluqueroid)
    data = parse(BarberActiveIn)
    with transaction() as cur:
        cur.execute("UPDATE peluqueros SET activo = %s WHERE peluqueroid = %s", (data.activo, peluqueroid))
        pending = 0
        if not data.activo:
            # La baja cierra inmediatamente todas sus sesiones.
            revoke_all_sessions(cur, barber["usuarioid"])
            cur.execute(
                "SELECT count(*) AS n FROM citas WHERE peluqueroid = %s AND estado = 'Pendiente' AND fechainicio > %s",
                (peluqueroid, now_local()),
            )
            pending = cur.fetchone()["n"]
    audit("barber_activated" if data.activo else "barber_deactivated", usuarioid=barber["usuarioid"],
          actor=current_user().usuarioid)
    if data.activo:
        message = "Barbero activado correctamente"
    else:
        message = "Barbero desactivado correctamente"
        if pending:
            message += f". Tiene {pending} cita(s) pendiente(s) que debes reasignar o cancelar"
    return jsonify({"message": message, "pending_appointments": pending, "username": None}), 200
