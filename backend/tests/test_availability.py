"""Tests unitarios (sin BD) del cálculo de huecos."""
from datetime import date, datetime, time, timedelta

from app.services.availability import ceil_to_step, compute_slots, is_bookable

DAY = date(2030, 1, 7)  # lunes
SCHEDULE = [(time(9), time(13)), (time(16), time(20))]


def dt(h, m=0, day=DAY):
    return datetime.combine(day, time(h, m))


def test_ceil_to_step_handles_end_of_day_without_crashing():
    # El código original lanzaba ValueError (hour=24) a partir de las 23:50.
    assert ceil_to_step(datetime(2030, 1, 7, 23, 55), 10) == datetime(2030, 1, 8, 0, 0)
    assert ceil_to_step(datetime(2030, 1, 7, 10, 0), 10) == datetime(2030, 1, 7, 10, 0)
    assert ceil_to_step(datetime(2030, 1, 7, 10, 0, 30), 10) == datetime(2030, 1, 7, 10, 10)


def test_slots_cover_split_shift():
    slots = compute_slots(day=DAY, schedule=SCHEDULE, busy=[], duration=timedelta(minutes=60),
                          now=dt(0, 0, date(2030, 1, 1)))
    starts = [s.start.time() for s in slots]
    assert starts == [time(9), time(10), time(11), time(12), time(16), time(17), time(18), time(19)]


def test_slots_skip_busy_ranges():
    busy = [(dt(10), dt(10, 30))]
    slots = compute_slots(day=DAY, schedule=[(time(9), time(12))], busy=busy,
                          duration=timedelta(minutes=30), now=dt(0, 0, date(2030, 1, 1)))
    starts = [s.start.strftime("%H:%M") for s in slots]
    assert "10:00" not in starts
    assert starts[:2] == ["09:00", "09:30"]
    assert "10:30" in starts


def test_slots_today_start_after_now():
    slots = compute_slots(day=DAY, schedule=[(time(9), time(12))], busy=[],
                          duration=timedelta(minutes=30), now=dt(10, 3))
    assert slots[0].start == dt(10, 10)


def test_no_slots_for_past_days():
    assert compute_slots(day=DAY, schedule=SCHEDULE, busy=[], duration=timedelta(minutes=30),
                         now=dt(9, 0, date(2030, 1, 8))) == []


def test_is_bookable_rules():
    now = dt(8)
    d30 = timedelta(minutes=30)
    assert is_bookable(start=dt(9), duration=d30, schedule=SCHEDULE, busy=[], now=now) is None
    assert is_bookable(start=dt(12, 45), duration=d30, schedule=SCHEDULE, busy=[], now=now)  # cruza el cierre
    assert is_bookable(start=dt(14), duration=d30, schedule=SCHEDULE, busy=[], now=now)  # descanso
    assert is_bookable(start=dt(9), duration=d30, schedule=SCHEDULE, busy=[(dt(9, 15), dt(9, 45))], now=now)
    assert is_bookable(start=dt(7), duration=d30, schedule=SCHEDULE, busy=[], now=dt(7, 30))  # pasado
