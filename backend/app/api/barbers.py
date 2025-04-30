from flask import request, jsonify
from app.api import barbers_bp
from app.db.connection import get_connection
from datetime import datetime, timedelta


@barbers_bp.route('/get_barber/<int:user_id>', methods=['GET'])
def get_barber(user_id):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT b.*, u.email, u.nombreusuario, u.rol
        FROM peluqueros b
        JOIN usuarios u ON b.usuarioid = u.usuarioid
        WHERE b.usuarioid = %s
    """, (user_id,))

    barber = cursor.fetchone()
    cursor.close()
    connection.close()

    if not barber:
        return jsonify({"error": "Barbero no encontrado"}), 404

    barber_data = dict(barber)

    if barber_data.get("fechacontratacion"):
        barber_data["fechacontratacion"] = barber_data["fechacontratacion"].strftime("%Y-%m-%d")

    return jsonify(barber_data), 200


@barbers_bp.route('/get_barber_dates/<int:barber_id>', methods=['GET'])
def get_barber_dates(barber_id):
    date_str = request.args.get("date")

    if not date_str:
        return jsonify({"error": "Falta el parámetro 'date'"}), 400

    try:
        selected_date = datetime.strptime(date_str, "%Y-%m-%d")
    except ValueError:
        return jsonify({"error": "Formato de fecha inválido. Usa YYYY-MM-DD"}), 400

    next_day = selected_date.replace(hour=0, minute=0, second=0) + timedelta(days=1)

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT c.*, 
               s.nombre AS servicio_nombre, s.duracion, s.precio,
               cl.nombre AS cliente_nombre, cl.telefono AS cliente_telefono
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN clientes cl ON c.clienteid = cl.clienteid
        WHERE c.peluqueroid = %s 
        AND c.fechainicio >= %s 
        AND c.fechainicio < %s
        ORDER BY c.fechainicio ASC
    """, (barber_id, selected_date, next_day))

    citas = cursor.fetchall()
    cursor.close()
    connection.close()

    citas_list = []
    for cita in citas:
        cita_dict = dict(cita)
        for campo in ["fechainicio", "fechafin"]:
            if cita_dict.get(campo):
                cita_dict[campo] = cita_dict[campo].strftime("%Y-%m-%d %H:%M")
        citas_list.append(cita_dict)

    return jsonify({"dates": citas_list}), 200


@barbers_bp.route('/update_barber/<int:user_id>', methods=['PUT'])
def update_barber(user_id):
    data = request.json or {}
    email = data.get("email")
    nombreusuario = data.get("nombreusuario")
    nombre = data.get("nombre")
    telefono = data.get("telefono")
    if telefono == "nulo":
        telefono = None

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT * FROM peluqueros WHERE usuarioid = %s", (user_id,))
    if not cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "Barbero no encontrado"}), 404

    if email:
        cursor.execute("SELECT 1 FROM usuarios WHERE email = %s AND usuarioid != %s", (email, user_id))
        if cursor.fetchone():
            cursor.close()
            connection.close()
            return jsonify({"error": "El email ya está en uso por otro usuario"}), 400

    if nombreusuario:
        cursor.execute("SELECT 1 FROM usuarios WHERE nombreusuario = %s AND usuarioid != %s", (nombreusuario, user_id))
        if cursor.fetchone():
            cursor.close()
            connection.close()
            return jsonify({"error": "El nombre de usuario ya está en uso"}), 400

    user_updates = []
    user_values = []

    if email:
        user_updates.append("email = %s")
        user_values.append(email)
    if nombreusuario:
        user_updates.append("nombreusuario = %s")
        user_values.append(nombreusuario)

    if user_updates:
        user_values.append(user_id)
        cursor.execute(f"""
            UPDATE usuarios
            SET {', '.join(user_updates)}
            WHERE usuarioid = %s
        """, tuple(user_values))

    barber_updates = []
    barber_values = []

    if nombre is not None:
        barber_updates.append("nombre = %s")
        barber_values.append(nombre)
    if "telefono" in data:
        barber_updates.append("telefono = %s")
        barber_values.append(telefono)

    if barber_updates:
        barber_values.append(user_id)
        cursor.execute(f"""
            UPDATE peluqueros
            SET {', '.join(barber_updates)}
            WHERE usuarioid = %s
        """, tuple(barber_values))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Info actualizada correctamente"}), 200



