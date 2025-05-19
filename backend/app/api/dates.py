from flask import jsonify, request
from app.api import dates_bp
from app.db.connection import get_connection
from datetime import datetime, timedelta
from app.utils.email import send_cancellation_email

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
        RETURNING citaid
    """, (clienteid, peluqueroid, servicioid, fechainicio, fechafin, localid))

    citaid = cursor.fetchone()[0]

    cursor.execute("""
        INSERT INTO actividad_peluquero (peluqueroid, tipo, citaid, clienteid)
        VALUES (%s, 'Reserva', %s, %s)
    """, (peluqueroid, citaid, clienteid))

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
    motivo = data.get("motivo")

    if not nuevo_estado:
        return jsonify({'error': 'Se requiere el nuevo estado'}), 400

    if nuevo_estado not in ["Pendiente", "Completada", "Cancelada", "No completada"]:
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

    if nuevo_estado == "Cancelada":
        cursor.execute("""
            SELECT c.nombre, c.telefono, u.email, u.nombreusuario,
                   ci.fechainicio, ci.fechafin, ci.servicioid,
                   s.nombre, s.descripcion, s.duracion,
                   l.nombre, l.direccion, l.localidad,
                   p.nombre
            FROM citas ci
            JOIN clientes c ON ci.clienteid = c.clienteid
            JOIN usuarios u ON c.usuarioid = u.usuarioid
            JOIN servicios s ON ci.servicioid = s.servicioid
            JOIN local l ON ci.localid = l.localid
            JOIN peluqueros p ON ci.peluqueroid = p.peluqueroid
            WHERE ci.citaid = %s
        """, (citaid,))
        info = cursor.fetchone()
        if info:
            email = info[2]
            cliente_nombre = info[0]
            fecha = info[4].strftime('%d/%m/%Y')
            hora_inicio = info[4].strftime('%H:%M')
            hora_fin = info[5].strftime('%H:%M')
            servicio_nombre = info[7]
            barber_name = info[13]
            local_info = {
                'nombre': info[10],
                'direccion': info[11],
                'localidad': info[12]
            }

            if not motivo:
                motivo = "No hay motivo especificado"

            send_cancellation_email(
                recipient=email,
                cliente_nombre=cliente_nombre,
                fecha=fecha,
                hora_inicio=hora_inicio,
                hora_fin=hora_fin,
                servicio_nombre=servicio_nombre,
                local_info=local_info,
                barber_name=barber_name,
                motivo=motivo
            )

        if motivo.strip() == "Cita cancelada por el cliente":
            cursor.execute("""
                INSERT INTO actividad_peluquero (peluqueroid, tipo, citaid, clienteid)
                VALUES (
                    (SELECT peluqueroid FROM citas WHERE citaid = %s),
                    'Cancelada',
                    %s,
                    (SELECT clienteid FROM citas WHERE citaid = %s)
                )
            """, (citaid, citaid, citaid))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({'message': f'Estado actualizado a "{nuevo_estado}"'}), 200
