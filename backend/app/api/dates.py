from flask import jsonify, request
from app.api import dates_bp
from app.db.connection import get_connection
from datetime import datetime, timedelta


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
        INSERT INTO citas (clienteid, peluqueroid, servicioid, fechainicio, fechafin, estado, localid)
        VALUES (%s, %s, %s, %s, %s, 'Pendiente', %s)
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

    if fechainicio - timedelta(minutes=30) <= now < fechainicio:
        cursor.close()
        connection.close()
        return jsonify({'error': 'No es posible cancelar una cita a falta de 30 minutos de su inicio'}), 403

    cursor.execute("DELETE FROM citas WHERE citaid = %s", (citaid,))
    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({'message': 'Cita cancelada con éxito'}), 200


@dates_bp.route('/update_date/<int:citaid>', methods=['PUT'])
def update_date(citaid):
    data = request.get_json()
    nuevo_estado = data.get("estado")

    if not nuevo_estado:
        return jsonify({'error': 'Se requiere el nuevo estado'}), 400

    if nuevo_estado not in ["Pendiente", "Completada", "Cancelada"]:
        return jsonify({'error': 'Estado no válido'}), 400

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT estado FROM citas WHERE citaid = %s", (citaid,))
    result = cursor.fetchone()

    if result is None:
        cursor.close()
        connection.close()
        return jsonify({'error': 'La cita no existe'}), 404

    cursor.execute("""
        UPDATE citas
        SET estado = %s
        WHERE citaid = %s
    """, (nuevo_estado, citaid))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({'message': f'Estado actualizado a "{nuevo_estado}" correctamente'}), 200
