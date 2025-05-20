from flask import jsonify, request
from app.api import calendar_bp
from app.db.connection import get_connection
from datetime import time, date, datetime, timedelta


@calendar_bp.route('/get_weekly_schedule/<int:peluqueroid>', methods=['GET'])
def get_weekly_schedule(peluqueroid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT diasemana, horainicio, horafin
        FROM horarios_peluqueros
        WHERE peluqueroid = %s
        ORDER BY diasemana, horainicio
    """, (peluqueroid,))

    horarios = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for h in horarios:
        item = {}
        for i, value in enumerate(h):
            item[column_names[i]] = value.isoformat() if isinstance(value, time) else value
        result.append(item)

    return jsonify(result), 200


@calendar_bp.route('/get_blocked_hours/<int:peluqueroid>', methods=['GET'])
def get_blocked_hours(peluqueroid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT fecha, horainicio, horafin, motivo
        FROM bloqueoshorarios
        WHERE peluqueroid = %s
        ORDER BY fecha, horainicio
    """, (peluqueroid,))

    bloqueos = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for b in bloqueos:
        item = {}
        for i, value in enumerate(b):
            item[column_names[i]] = value.isoformat() if isinstance(value, (date, time)) else value
        result.append(item)

    return jsonify(result), 200


@calendar_bp.route('/get_barber_dates/<int:peluqueroid>', methods=['GET'])
def get_barber_dates(peluqueroid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT citaid, fechahora, fechafin, estado
        FROM citas
        WHERE peluqueroid = %s
        ORDER BY fechahora
    """, (peluqueroid,))

    citas = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for c in citas:
        item = {}
        for i, value in enumerate(c):
            item[column_names[i]] = value.isoformat() if isinstance(value, datetime) else value
        result.append(item)

    return jsonify(result), 200


@calendar_bp.route('/get_available_slots/<int:peluqueroid>', methods=['GET'])
def get_available_slots(peluqueroid):
    fecha_str = request.args.get('fecha')
    duracion = int(request.args.get('duracion'))

    fecha = datetime.strptime(fecha_str, "%Y-%m-%d").date()
    connection = get_connection()
    cursor = connection.cursor()

    day_mapping = {
        "monday": "Lunes",
        "tuesday": "Martes",
        "wednesday": "Miércoles",
        "thursday": "Jueves",
        "friday": "Viernes",
        "saturday": "Sábado",
        "sunday": "Domingo"
    }

    dia_ingles = fecha.strftime("%A").lower()
    dia_semana = day_mapping[dia_ingles]

    cursor.execute("""
        SELECT horainicio, horafin
        FROM horarios_peluqueros
        WHERE peluqueroid = %s AND diasemana = %s
    """, (peluqueroid, dia_semana))

    horario = cursor.fetchone()
    if not horario:
        return jsonify({"error": "No hay horario para ese día"}), 404

    hora_inicio, hora_fin = horario
    dt_inicio = datetime.combine(fecha, hora_inicio)
    dt_fin = datetime.combine(fecha, hora_fin)

    cursor.execute("""
        SELECT fechainicio, fechafin
        FROM citas
        WHERE peluqueroid = %s AND DATE(fechainicio) = %s AND estado != 'Cancelada'
        ORDER BY fechainicio
    """, (peluqueroid, fecha))

    citas = cursor.fetchall()

    cursor.execute("""
        SELECT horainicio, horafin
        FROM bloqueoshorarios
        WHERE peluqueroid = %s AND fecha = %s
        ORDER BY horainicio
    """, (peluqueroid, fecha))

    bloqueos = cursor.fetchall()
    cursor.close()
    connection.close()

    rangos_ocupados = []

    for cita_inicio, cita_fin in citas:
        rangos_ocupados.append((cita_inicio, cita_fin))

    for bloque_inicio, bloque_fin in bloqueos:
        bloque_dt_inicio = datetime.combine(fecha, bloque_inicio)
        bloque_dt_fin = datetime.combine(fecha, bloque_fin)
        rangos_ocupados.append((bloque_dt_inicio, bloque_dt_fin))

    rangos_ocupados.sort(key=lambda x: x[0])

    bloques_disponibles = []
    actual = datetime.now()

    if fecha == actual.date():
        minuto = actual.minute
        siguiente_multiplo = ((minuto // 10) + 1) * 10
        if siguiente_multiplo >= 60:
            actual = actual.replace(hour=actual.hour + 1, minute=0, second=0, microsecond=0)
        else:
            actual = actual.replace(minute=siguiente_multiplo, second=0, microsecond=0)
    inicio = max(dt_inicio, actual) if fecha == actual.date() else dt_inicio
    duracion_td = timedelta(minutes=duracion)

    paso = timedelta(minutes=5)
    while inicio + duracion_td <= dt_fin:
        hay_solape = False
        for ocupado_inicio, ocupado_fin in rangos_ocupados:
            if not (inicio + duracion_td <= ocupado_inicio or inicio >= ocupado_fin):
                hay_solape = True
                break

        if not hay_solape:
            bloques_disponibles.append({
                "desde": inicio.strftime("%H:%M"),
                "hasta": (inicio + duracion_td).strftime("%H:%M")
            })

            inicio += duracion_td
        else:
            inicio += paso

    return jsonify(bloques_disponibles), 200