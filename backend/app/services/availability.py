"""Cálculo de huecos disponibles y validación de reservas.

La lógica de calendario se separa en funciones puras (sin BD ni Flask) para
poder probarla de forma aislada. Corrige varios fallos del original:

* ``actual.replace(hour=actual.hour + 1)`` lanzaba ``ValueError`` a partir de
  las 23:50.
* El nombre del día se calculaba con ``strftime('%A')``, dependiente del
  locale del servidor.
* Solo se leía un tramo horario por día (``fetchone``); ahora se admiten
  jornadas partidas.
* El servidor confiaba en la duración enviada por el cliente; ahora sale
  siempre del servicio.
"""
from __future__ import annotations

from dataclasses import dataclass
from datetime import date, datetime, time, timedelta
from typing import Iterable, Sequence

Range = tuple[datetime, datetime]


@dataclass(frozen=True)
class Slot:
    start: datetime
    end: datetime

    def as_dict(self) -> dict:
        return {"desde": self.start.strftime("%H:%M"), "hasta": self.end.strftime("%H:%M")}


def ceil_to_step(moment: datetime, step_minutes: int) -> datetime:
    """Redondea hacia arriba al siguiente múltiplo de ``step_minutes``."""
    base = moment.replace(second=0, microsecond=0)
    if base < moment:
        base += timedelta(minutes=1)
    remainder = base.minute % step_minutes
    if remainder:
        base += timedelta(minutes=step_minutes - remainder)
    return base


def overlaps(a_start: datetime, a_end: datetime, b_start: datetime, b_end: datetime) -> bool:
    return a_start < b_end and b_start < a_end


def working_ranges(day: date, schedule: Iterable[tuple[time, time]]) -> list[Range]:
    ranges = [(datetime.combine(day, start), datetime.combine(day, end))
              for start, end in schedule if start < end]
    ranges.sort()
    return ranges


def compute_slots(*, day: date, schedule: Iterable[tuple[time, time]], busy: Sequence[Range],
                  duration: timedelta, now: datetime, step_minutes: int = 5,
                  round_now_to: int = 10) -> list[Slot]:
    """Devuelve huecos consecutivos no solapados de ``duration`` dentro del horario.

    Se mantiene el comportamiento de la app original (al encontrar un hueco
    libre el siguiente candidato empieza donde termina el anterior), pero sin
    los errores de borde.
    """
    if duration <= timedelta(0):
        return []
    step = timedelta(minutes=step_minutes)
    busy_sorted = sorted(busy)
    earliest = ceil_to_step(now, round_now_to) if day == now.date() else None
    if day < now.date():
        return []

    slots: list[Slot] = []
    for range_start, range_end in working_ranges(day, schedule):
        cursor = range_start
        if earliest is not None and earliest > cursor:
            cursor = earliest
        while cursor + duration <= range_end:
            end = cursor + duration
            conflict = next((b for b in busy_sorted if overlaps(cursor, end, b[0], b[1])), None)
            if conflict is None:
                slots.append(Slot(cursor, end))
                cursor = end
            else:
                # Saltamos directamente al final del bloque ocupado, alineado al paso.
                cursor = max(cursor + step, ceil_to_step(conflict[1], step_minutes))
    return slots


def is_bookable(*, start: datetime, duration: timedelta, schedule: Iterable[tuple[time, time]],
                busy: Sequence[Range], now: datetime) -> str | None:
    """Valida una reserva concreta. Devuelve ``None`` si es válida o el motivo."""
    end = start + duration
    if start <= now:
        return "No se pueden reservar citas en el pasado"
    if end.date() != start.date():
        return "La cita debe empezar y terminar el mismo día"
    ranges = working_ranges(start.date(), schedule)
    if not any(r_start <= start and end <= r_end for r_start, r_end in ranges):
        return "La hora elegida está fuera del horario del peluquero"
    if any(overlaps(start, end, b_start, b_end) for b_start, b_end in busy):
        return "Ese horario ya está ocupado"
    return None
