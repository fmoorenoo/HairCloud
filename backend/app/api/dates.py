from flask import jsonify, request
from app.api import dates_bp
from app.db.connection import get_connection
from datetime import datetime


@dates_bp.route('/add_date', methods=['POST'])
def add_date():
    data = request.get_json()

    clienteid = data.get('clienteid')
    peluqueroid = data.get('peluqueroid')
    servicioid = data.get('servicioid')
    localid = data.get('localid')
    fechainicio_str = data.get('fechainicio')
    fechafin_str = data.get('fechafin')

    if not all([clienteid, peluqueroid, servicioid, localid, fechainicio_str, fechafin_str]):
        return jsonify({'error': 'Faltan datos obligatorios'}), 400

    try:
        fechainicio = datetime.strptime(fechainicio_str, '%Y-%m-%d %H:%M')
        fechafin = datetime.strptime(fechafin_str, '%Y-%m-%d %H:%M')
    except ValueError:
        return jsonify({'error': 'Formato de fecha inválido'}), 400

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        INSERT INTO citas (clienteid, peluqueroid, servicioid, fechainicio, fechafin, estado, descuento, localid)
        VALUES (%s, %s, %s, %s, %s, 'Pendiente', FALSE, %s)
    """, (clienteid, peluqueroid, servicioid, fechainicio, fechafin, localid))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({'message': 'Cita creada con éxito'}), 201


@dates_bp.route('/delete_date/<int:citaid>', methods=['DELETE'])
def delete_date(citaid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT fechainicio, fechafin FROM citas WHERE citaid = %s", (citaid,))
    result = cursor.fetchone()
    if result is None:
        cursor.close()
        connection.close()
        return jsonify({'error': 'La cita no existe'}), 404

    fechainicio, fechafin = result
    now = datetime.now()

    if now >= fechafin:
        cursor.close()
        connection.close()
        return jsonify({'error': 'No puedes cancelar una cita que ya ha finalizado'}), 403

    if fechainicio <= now < fechafin:
        cursor.close()
        connection.close()
        return jsonify({'error': 'No puedes cancelar una cita que está en curso'}), 403

    cursor.execute("DELETE FROM citas WHERE citaid = %s", (citaid,))
    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({'message': 'Cita cancelada con éxito'}), 200