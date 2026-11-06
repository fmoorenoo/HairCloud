"""Estadísticas de clientes y peluqueros.

Se calculan en el servidor a partir de ``v_citas`` (estado efectivo). El
email de estadísticas ya no acepta los números desde la app (antes se podía
enviar cualquier contenido, que además se inyectaba sin escapar en el HTML).
"""
from __future__ import annotations

from datetime import date, datetime, timedelta

from ..db import transaction
from ..serializers import money
from ..timeutils import fmt_datetime, long_date_es, now_local
from .mailer import send_mail


def barber_stats(peluqueroid: int, localid: int, start: date, end_inclusive: date) -> dict:
    start_dt = datetime.combine(start, datetime.min.time())
    end_dt = datetime.combine(end_inclusive + timedelta(days=1), datetime.min.time())
    params = {"p": peluqueroid, "l": localid, "s": start_dt, "e": end_dt}
    base = "c.peluqueroid = %(p)s AND c.localid = %(l)s AND c.fechainicio >= %(s)s AND c.fechainicio < %(e)s"

    with transaction() as cur:
        cur.execute(
            f"""
            SELECT
              count(DISTINCT c.clienteid) FILTER (WHERE c.estado_efectivo = 'Completada') AS clientes,
              count(*) FILTER (WHERE c.estado_efectivo = 'Completada') AS completadas,
              count(*) FILTER (WHERE c.estado_efectivo = 'Cancelada') AS canceladas,
              count(*) FILTER (WHERE c.estado_efectivo = 'No completada') AS no_completadas,
              COALESCE(sum(s.precio) FILTER (WHERE c.estado_efectivo = 'Completada'), 0) AS ingresos
            FROM v_citas c JOIN servicios s ON s.servicioid = c.servicioid
            WHERE {base}
            """,
            params,
        )
        totals = cur.fetchone()
        cur.execute(
            f"""
            SELECT c.servicioid, s.nombre, count(*) AS total
            FROM v_citas c JOIN servicios s ON c.servicioid = s.servicioid
            WHERE {base} AND c.estado_efectivo = 'Completada'
            GROUP BY c.servicioid, s.nombre ORDER BY total DESC, s.nombre LIMIT 1
            """,
            params,
        )
        top_service = cur.fetchone()
        cur.execute(
            f"""
            SELECT cl.clienteid, cl.nombre, count(*) AS total
            FROM v_citas c JOIN clientes cl ON c.clienteid = cl.clienteid
            WHERE {base} AND c.estado_efectivo = 'Completada'
            GROUP BY cl.clienteid, cl.nombre ORDER BY total DESC, cl.nombre LIMIT 1
            """,
            params,
        )
        top_client = cur.fetchone()

    days = (end_inclusive - start).days + 1
    return {
        "total_clientes_atendidos": totals["clientes"],
        "total_citas": totals["completadas"],
        "total_canceladas": totals["canceladas"],
        "total_no_completadas": totals["no_completadas"],
        "servicio_mas_solicitado": (
            {"servicioid": top_service["servicioid"], "nombre": top_service["nombre"],
             "cantidad": top_service["total"]} if top_service else None),
        "cliente_mas_frecuente": (
            {"clienteid": top_client["clienteid"], "nombre": top_client["nombre"],
             "total_citas": top_client["total"]} if top_client else None),
        "ingresos_totales": money(totals["ingresos"]),
        "promedio_citas_por_dia": round(totals["completadas"] / days, 2) if days > 0 else 0.0,
    }


def send_barber_stats(email: str, stats: dict, start: date, end: date) -> None:
    total = stats["total_citas"] + stats["total_canceladas"] + stats["total_no_completadas"]

    def pct(n: int) -> int:
        return round(n * 100 / total) if total else 0

    bars = [
        ("Completadas", stats["total_citas"], pct(stats["total_citas"]), "#22c55e"),
        ("Canceladas", stats["total_canceladas"], pct(stats["total_canceladas"]), "#ef4444"),
        ("No completadas", stats["total_no_completadas"], pct(stats["total_no_completadas"]), "#eab308"),
    ]
    send_mail(email, "Estadísticas de rendimiento - HairCloud", "barber_stats",
              title="Resumen de tu actividad", stats=stats, bars=bars,
              start=long_date_es(start), end=long_date_es(end))


def client_stats(clienteid: int) -> dict:
    now = now_local()
    done = "c.clienteid = %(c)s AND c.estado_efectivo = 'Completada'"
    params = {"c": clienteid, "now": now}
    with transaction() as cur:
        cur.execute(f"SELECT count(*) AS n FROM v_citas c WHERE {done}", params)
        total = cur.fetchone()["n"]
        cur.execute(
            f"""
            SELECT l.nombre, count(*) AS total FROM v_citas c JOIN local l ON c.localid = l.localid
            WHERE {done} GROUP BY l.localid, l.nombre ORDER BY total DESC, l.nombre LIMIT 1
            """,
            params,
        )
        top_local = cur.fetchone()
        cur.execute(
            """
            SELECT c.citaid, c.fechainicio, c.fechafin, s.nombre AS servicio_nombre,
                   b.nombre AS peluquero_nombre, l.nombre AS local_nombre
            FROM v_citas c
            JOIN servicios s ON c.servicioid = s.servicioid
            JOIN peluqueros b ON c.peluqueroid = b.peluqueroid
            JOIN local l ON c.localid = l.localid
            WHERE c.clienteid = %(c)s AND c.fechainicio > %(now)s AND c.estado_efectivo = 'Pendiente'
            ORDER BY c.fechainicio ASC LIMIT 1
            """,
            params,
        )
        next_appt = cur.fetchone()
        cur.execute(
            f"""
            SELECT s.nombre AS servicio_nombre, l.nombre AS local_nombre, count(*) AS total
            FROM v_citas c JOIN servicios s ON c.servicioid = s.servicioid JOIN local l ON c.localid = l.localid
            WHERE {done} GROUP BY s.nombre, l.nombre ORDER BY total DESC, s.nombre LIMIT 1
            """,
            params,
        )
        fav = cur.fetchone()

    if next_appt:
        next_appt = {**next_appt, "fechainicio": fmt_datetime(next_appt["fechainicio"]),
                     "fechafin": fmt_datetime(next_appt["fechafin"])}
    return {
        "total_citas_finalizadas": total,
        "local_mas_frecuentado": top_local["nombre"] if top_local else None,
        "local_mas_frecuentado_visitas": top_local["total"] if top_local else 0,
        "proxima_cita": next_appt,
        "servicio_favorito": fav["servicio_nombre"] if fav else None,
        "servicio_favorito_local": fav["local_nombre"] if fav else None,
    }
