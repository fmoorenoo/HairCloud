"""Reglas de negocio de las citas.

Problemas del diseño anterior que se corrigen aquí:

* El cliente enviaba ``clienteid``, ``localid`` y ``fechafin``: se podía
  reservar en nombre de otro cliente, en otro local o con una duración
  arbitraria. Ahora el cliente sale del token, el local del peluquero y la
  hora de fin de la duración del servicio.
* No se comprobaba ni el horario del peluquero ni los solapes: dos clientes
  podían reservar la misma hora. Ahora se valida el horario, los bloqueos y
  las citas existentes bajo un bloqueo de fila del peluquero, y además la BD
  tiene una restricción de exclusión que impide solapes aunque falle la app.
* Cualquiera podía cambiar el estado de cualquier cita. Ahora hay una máquina
  de estados con permisos por rol.
* El email de confirmación lo disparaba la app con otra llamada (que permitía
  enviar correos a cualquier cliente). Ahora lo envía el servidor tras
  confirmar la transacción.
* Las citas pendientes ya terminadas se consideran "Completada" mediante la
  vista ``v_citas`` en lugar de hacer UPDATE dentro de peticiones GET.
"""
from __future__ import annotations

import logging
from datetime import date, datetime, timedelta

from flask import current_app

from ..db import fetch_all, fetch_one, transaction
from ..errors import BadRequest, Conflict, Forbidden, NotFound
from ..security.auth import CurrentUser, REVIEW_MODERATORS
from ..timeutils import fmt_datetime, now_local, weekday_es, weekday_long_es
from . import availability
from .mailer import send_mail

log = logging.getLogger(__name__)

PENDING, DONE, CANCELLED, NOT_DONE = "Pendiente", "Completada", "Cancelada", "No completada"
CLIENT_CANCEL_REASON = "Cita cancelada por el cliente"
DEFAULT_CANCEL_REASON = "No hay motivo especificado"

# Roles del local (además del propio peluquero) que pueden gestionar citas.
APPOINTMENT_MANAGERS = REVIEW_MODERATORS


def effective_state(estado: str, fechafin: datetime, now: datetime) -> str:
    if estado == PENDING and fechafin <= now:
        return DONE
    return estado


# --------------------------------------------------------------------------
# Disponibilidad
# --------------------------------------------------------------------------
def _schedule_for(cur, peluqueroid: int, day: date) -> list[tuple]:
    cur.execute(
        """
        SELECT horainicio, horafin FROM horarios_peluqueros
        WHERE peluqueroid = %s AND diasemana = %s ORDER BY horainicio
        """,
        (peluqueroid, weekday_es(day)),
    )
    return [(r["horainicio"], r["horafin"]) for r in cur.fetchall()]


def _busy_for(cur, peluqueroid: int, day: date, exclude_cita: int | None = None) -> list[tuple]:
    start = datetime.combine(day, datetime.min.time())
    end = start + timedelta(days=1)
    cur.execute(
        """
        SELECT fechainicio, fechafin FROM citas
        WHERE peluqueroid = %s AND estado <> 'Cancelada'
          AND fechainicio < %s AND fechafin > %s
          AND (%s::int IS NULL OR citaid <> %s::int)
        """,
        (peluqueroid, end, start, exclude_cita, exclude_cita),
    )
    busy = [(r["fechainicio"], r["fechafin"]) for r in cur.fetchall()]
    cur.execute(
        "SELECT horainicio, horafin FROM bloqueoshorarios WHERE peluqueroid = %s AND fecha = %s",
        (peluqueroid, day),
    )
    busy += [(datetime.combine(day, r["horainicio"]), datetime.combine(day, r["horafin"]))
             for r in cur.fetchall()]
    return busy


def _check_booking_window(day: date, now: datetime) -> None:
    max_days = current_app.config["BOOKING_MAX_DAYS_AHEAD"]
    if day < now.date():
        raise BadRequest("No se pueden consultar ni reservar fechas pasadas", code="date_in_past")
    if day > now.date() + timedelta(days=max_days):
        raise BadRequest(f"Solo se puede reservar con {max_days} días de antelación como máximo",
                         code="date_too_far")


def available_slots(peluqueroid: int, day: date, servicioid: int | None = None,
                    duration_minutes: int | None = None) -> list[dict]:
    now = now_local()
    _check_booking_window(day, now)
    with transaction() as cur:
        cur.execute("SELECT localid, COALESCE(activo, TRUE) AS activo FROM peluqueros WHERE peluqueroid = %s",
                    (peluqueroid,))
        barber = cur.fetchone()
        if barber is None or not barber["activo"]:
            raise NotFound("Peluquero no encontrado")
        if servicioid is not None:
            cur.execute(
                "SELECT duracion FROM servicios WHERE servicioid = %s AND localid = %s AND activo",
                (servicioid, barber["localid"]),
            )
            svc = cur.fetchone()
            if svc is None:
                raise NotFound("Servicio no encontrado en este local")
            duration_minutes = svc["duracion"]
        if not duration_minutes or not 5 <= duration_minutes <= 480:
            raise BadRequest("Duración no válida", code="invalid_duration")
        schedule = _schedule_for(cur, peluqueroid, day)
        busy = _busy_for(cur, peluqueroid, day)

    slots = availability.compute_slots(
        day=day, schedule=schedule, busy=busy, duration=timedelta(minutes=duration_minutes), now=now,
        step_minutes=current_app.config["SLOT_STEP_MINUTES"],
    )
    return [s.as_dict() for s in slots]


# --------------------------------------------------------------------------
# Crear cita
# --------------------------------------------------------------------------
def create_appointment(user: CurrentUser, peluqueroid: int, servicioid: int, start: datetime) -> dict:
    cfg = current_app.config
    now = now_local()
    _check_booking_window(start.date(), now)

    with transaction() as cur:
        # Bloqueo de la fila del peluquero: serializa las reservas concurrentes
        # sobre el mismo peluquero y evita la doble reserva.
        cur.execute(
            """
            SELECT p.peluqueroid, p.localid, p.nombre, COALESCE(p.activo, TRUE) AS activo
            FROM peluqueros p WHERE p.peluqueroid = %s FOR UPDATE
            """,
            (peluqueroid,),
        )
        barber = cur.fetchone()
        if barber is None or not barber["activo"]:
            raise NotFound("Peluquero no encontrado")

        cur.execute(
            """
            SELECT servicioid, nombre, descripcion, duracion, precio, localid
            FROM servicios WHERE servicioid = %s AND activo
            """,
            (servicioid,),
        )
        svc = cur.fetchone()
        if svc is None:
            raise NotFound("Servicio no encontrado")
        if svc["localid"] != barber["localid"]:
            raise BadRequest("El servicio no pertenece al local del peluquero", code="service_mismatch")

        duration = timedelta(minutes=svc["duracion"])
        end = start + duration

        reason = availability.is_bookable(
            start=start, duration=duration, schedule=_schedule_for(cur, peluqueroid, start.date()),
            busy=_busy_for(cur, peluqueroid, start.date()), now=now,
        )
        if reason:
            raise Conflict(reason, code="slot_unavailable")

        # El cliente no puede tener dos citas a la vez ni acumular reservas.
        cur.execute(
            """
            SELECT 1 FROM citas WHERE clienteid = %s AND estado <> 'Cancelada'
              AND fechainicio < %s AND fechafin > %s LIMIT 1
            """,
            (user.clienteid, end, start),
        )
        if cur.fetchone():
            raise Conflict("Ya tienes otra cita a esa hora", code="client_overlap")
        cur.execute(
            "SELECT count(*) AS n FROM citas WHERE clienteid = %s AND estado = 'Pendiente' AND fechainicio > %s",
            (user.clienteid, now),
        )
        if cur.fetchone()["n"] >= cfg["MAX_PENDING_PER_CLIENT"]:
            raise Conflict("Has alcanzado el máximo de citas pendientes", code="too_many_pending")

        cur.execute(
            """
            INSERT INTO citas (clienteid, peluqueroid, servicioid, fechainicio, fechafin, estado, localid,
                               created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, 'Pendiente', %s, %s, %s)
            RETURNING citaid
            """,
            (user.clienteid, peluqueroid, servicioid, start, end, barber["localid"], now, now),
        )
        citaid = cur.fetchone()["citaid"]
        cur.execute(
            "INSERT INTO actividad_peluquero (peluqueroid, tipo, citaid, clienteid, fecha) VALUES (%s, 'Reserva', %s, %s, %s)",
            (peluqueroid, citaid, user.clienteid, now),
        )
        cur.execute(
            "SELECT nombre, direccion, telefono, localidad FROM local WHERE localid = %s",
            (barber["localid"],),
        )
        local = cur.fetchone()

    try:
        send_mail(
            user.email, "Confirmación de tu cita - HairCloud", "booking_confirmation",
            title="Tu cita ha sido confirmada", local=local, barber_name=barber["nombre"], service=svc,
            price=f"{svc['precio']:.2f}", fecha=weekday_long_es(start.date()),
            hora_inicio=start.strftime("%H:%M"), hora_fin=end.strftime("%H:%M"),
            cancel_minutes=cfg["CANCELLATION_MIN_MINUTES"],
        )
    except Exception:
        log.exception("No se pudo preparar el email de confirmación de la cita %s", citaid)

    return {"citaid": citaid, "fechainicio": fmt_datetime(start), "fechafin": fmt_datetime(end),
            "localid": barber["localid"], "estado": PENDING}


# --------------------------------------------------------------------------
# Cambios de estado
# --------------------------------------------------------------------------
def _load_for_update(cur, citaid: int) -> dict:
    cur.execute(
        """
        SELECT ci.citaid, ci.clienteid, ci.peluqueroid, ci.localid, ci.estado, ci.fechainicio, ci.fechafin,
               cl.nombre AS cliente_nombre, cu.email AS cliente_email,
               p.nombre AS peluquero_nombre, s.nombre AS servicio_nombre,
               l.nombre AS local_nombre, l.direccion AS local_direccion, l.localidad AS local_localidad
        FROM citas ci
        JOIN clientes cl ON cl.clienteid = ci.clienteid
        JOIN usuarios cu ON cu.usuarioid = cl.usuarioid
        JOIN peluqueros p ON p.peluqueroid = ci.peluqueroid
        JOIN servicios s ON s.servicioid = ci.servicioid
        JOIN local l ON l.localid = ci.localid
        WHERE ci.citaid = %s
        FOR UPDATE OF ci
        """,
        (citaid,),
    )
    row = cur.fetchone()
    if row is None:
        raise NotFound("La cita no existe")
    return row


def _client_transition(cita: dict, target: str, now: datetime) -> None:
    if target != CANCELLED:
        raise Forbidden("Solo puedes cancelar tus citas")
    min_minutes = current_app.config["CANCELLATION_MIN_MINUTES"]
    if now >= cita["fechafin"]:
        raise Forbidden("No puedes cancelar una cita que ya ha finalizado", code="appointment_finished")
    if cita["fechainicio"] <= now:
        raise Forbidden("No puedes cancelar una cita que está en curso", code="appointment_in_progress")
    if now >= cita["fechainicio"] - timedelta(minutes=min_minutes):
        raise Forbidden(
            f"No es posible cancelar una cita a falta de {min_minutes} minutos de su inicio",
            code="cancellation_window_closed",
        )


def _staff_transition(cita: dict, current: str, target: str, now: datetime) -> None:
    finished = cita["fechafin"] <= now
    started = cita["fechainicio"] <= now
    if target == CANCELLED:
        if current != PENDING or finished:
            raise Conflict("Solo se pueden cancelar citas pendientes que no han finalizado",
                           code="invalid_transition")
    elif target == NOT_DONE:
        if not started:
            raise Conflict("No se puede marcar como no completada una cita que no ha empezado",
                           code="invalid_transition")
    elif target == PENDING:
        if finished:
            raise Conflict("Una cita finalizada no puede volver a estar pendiente",
                           code="invalid_transition")


def change_status(user: CurrentUser, citaid: int, target: str, motivo: str | None) -> str:
    now = now_local()
    notify = None
    with transaction() as cur:
        cita = _load_for_update(cur, citaid)
        current = effective_state(cita["estado"], cita["fechafin"], now)

        is_owner = user.clienteid is not None and user.clienteid == cita["clienteid"]
        is_barber = user.peluqueroid is not None and user.peluqueroid == cita["peluqueroid"]
        is_manager = user.has_local_role(cita["localid"], APPOINTMENT_MANAGERS)
        if not (is_owner or is_barber or is_manager):
            # 404 en vez de 403 para no confirmar la existencia de citas ajenas.
            raise NotFound("La cita no existe")

        if current == CANCELLED:
            raise Conflict("La cita ya está cancelada", code="already_cancelled")
        if current == target:
            return f'La cita ya estaba en estado "{target}"'

        if is_barber or is_manager:
            _staff_transition(cita, current, target, now)
        else:
            _client_transition(cita, target, now)

        if target == CANCELLED:
            reason = CLIENT_CANCEL_REASON if (is_owner and not (is_barber or is_manager)) \
                else (motivo or DEFAULT_CANCEL_REASON)
            cur.execute(
                """
                UPDATE citas SET estado = 'Cancelada', motivo_cancelacion = %s, cancelada_por = %s, updated_at = %s
                WHERE citaid = %s
                """,
                (reason, user.usuarioid, now, citaid),
            )
            if reason == CLIENT_CANCEL_REASON:
                cur.execute(
                    "INSERT INTO actividad_peluquero (peluqueroid, tipo, citaid, clienteid, fecha) VALUES (%s, 'Cancelada', %s, %s, %s)",
                    (cita["peluqueroid"], citaid, cita["clienteid"], now),
                )
            else:
                notify = reason
        else:
            cur.execute("UPDATE citas SET estado = %s, updated_at = %s WHERE citaid = %s",
                        (target, now, citaid))

    if notify:
        try:
            send_mail(
                cita["cliente_email"], "Cancelación de tu cita - HairCloud", "cancellation",
                title="Tu cita ha sido cancelada", header_color="#ef4444",
                client_name=cita["cliente_nombre"], barber_name=cita["peluquero_nombre"],
                local={"nombre": cita["local_nombre"], "direccion": cita["local_direccion"],
                       "localidad": cita["local_localidad"]},
                service_name=cita["servicio_nombre"], motivo=notify,
                fecha=cita["fechainicio"].strftime("%d/%m/%Y"),
                hora_inicio=cita["fechainicio"].strftime("%H:%M"),
                hora_fin=cita["fechafin"].strftime("%H:%M"),
            )
        except Exception:
            log.exception("No se pudo preparar el email de cancelación de la cita %s", citaid)
    return f'Estado actualizado a "{target}"'


# --------------------------------------------------------------------------
# Listados
# --------------------------------------------------------------------------
def client_appointments(clienteid: int) -> list[dict]:
    return fetch_all(
        """
        SELECT c.citaid, c.clienteid, c.peluqueroid, c.servicioid, c.fechainicio, c.fechafin,
               c.estado_efectivo AS estado, c.localid,
               s.nombre AS servicio_nombre, s.precio AS servicio_precio, s.duracion AS servicio_duracion,
               b.nombre AS barber_nombre, l.nombre AS local_nombre, l.direccion AS local_direccion
        FROM v_citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN peluqueros b ON c.peluqueroid = b.peluqueroid
        JOIN local l ON c.localid = l.localid
        WHERE c.clienteid = %s AND c.estado <> 'Cancelada'
        ORDER BY c.fechainicio ASC
        """,
        (clienteid,),
    )


def barber_appointments(peluqueroid: int, start: date, end_inclusive: date) -> list[dict]:
    start_dt = datetime.combine(start, datetime.min.time())
    end_dt = datetime.combine(end_inclusive + timedelta(days=1), datetime.min.time())
    return fetch_all(
        """
        SELECT c.citaid, c.clienteid, c.peluqueroid, c.servicioid, c.fechainicio, c.fechafin,
               c.estado_efectivo AS estado, c.localid,
               s.nombre AS servicio_nombre, s.duracion, s.precio,
               cl.nombre AS cliente_nombre, cl.telefono AS cliente_telefono
        FROM v_citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN clientes cl ON c.clienteid = cl.clienteid
        WHERE c.peluqueroid = %s AND c.fechainicio >= %s AND c.fechainicio < %s
        ORDER BY c.fechainicio ASC
        """,
        (peluqueroid, start_dt, end_dt),
    )


def complete_past_appointments() -> int:
    """Persiste como 'Completada' las citas pendientes ya terminadas (tarea programada)."""
    with transaction() as cur:
        cur.execute(
            "UPDATE citas SET estado = 'Completada', updated_at = %s WHERE estado = 'Pendiente' AND fechafin <= %s",
            (now_local(), now_local()),
        )
        return cur.rowcount
