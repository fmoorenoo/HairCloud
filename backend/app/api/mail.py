from app.api import mail_bp
from app.db.connection import get_connection
from app.utils.email import send_booking_info_email
from flask import request, jsonify
from datetime import datetime


@mail_bp.route('/send_info_date', methods=['POST'])
def send_info_date():
    connection = None
    cursor = None
    try:
        data = request.get_json()

        clienteid = data['clienteid']
        peluqueroid = data['peluqueroid']
        servicioid = data['servicioid']
        localid = data['localid']
        fechainicio = data['fechainicio']
        fechafin = data['fechafin']

        connection = get_connection()
        cursor = connection.cursor()

        cursor.execute("SELECT nombre, direccion, telefono, localidad FROM local WHERE localid = %s", (localid,))
        local_row = cursor.fetchone()
        if not local_row:
            raise Exception("No se encontró el local.")
        local_info = dict(zip(['nombre', 'direccion', 'telefono', 'localidad'], local_row))

        cursor.execute("SELECT nombre FROM peluqueros WHERE peluqueroid = %s", (peluqueroid,))
        barber = cursor.fetchone()
        if not barber:
            raise Exception("No se encontró el peluquero.")
        barber_name = barber[0]

        cursor.execute("SELECT nombre, descripcion, duracion, precio FROM servicios WHERE servicioid = %s", (servicioid,))
        service_row = cursor.fetchone()
        if not service_row:
            raise Exception("No se encontró el servicio.")
        service_info = dict(zip(['nombre', 'descripcion', 'duracion', 'precio'], service_row))

        cursor.execute("SELECT usuarioid FROM clientes WHERE clienteid = %s", (clienteid,))
        cliente_row = cursor.fetchone()
        if not cliente_row:
            raise Exception("No se encontró el cliente.")
        usuarioid = cliente_row[0]

        cursor.execute("SELECT email FROM usuarios WHERE usuarioid = %s", (usuarioid,))
        email_row = cursor.fetchone()
        if not email_row:
            raise Exception("No se encontró el usuario vinculado al cliente.")
        recipient = email_row[0]

        dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
        meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
                 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre']

        dt_inicio = datetime.strptime(fechainicio, "%Y-%m-%d %H:%M")
        dt_fin = datetime.strptime(fechafin, "%Y-%m-%d %H:%M")

        hora_inicio = dt_inicio.strftime("%H:%M")
        hora_fin = dt_fin.strftime("%H:%M")

        fecha = f"{dias[dt_inicio.weekday()]} {dt_inicio.day} de {meses[dt_inicio.month - 1]}"

        send_booking_info_email(
            recipient,
            local_info,
            barber_name,
            service_info,
            hora_inicio,
            hora_fin,
            fecha
        )

        return jsonify({"message": "Correo enviado con éxito"})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()