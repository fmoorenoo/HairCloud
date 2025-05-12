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
    start_date_str = request.args.get("start")
    end_date_str = request.args.get("end")

    # Interpretar rango o fecha individual
    if date_str:
        try:
            selected_date = datetime.strptime(date_str, "%Y-%m-%d")
            next_day = selected_date + timedelta(days=1)
            fecha_inicio = selected_date
            fecha_fin = next_day
        except ValueError:
            return jsonify({"error": "Formato de fecha inválido. Usa YYYY-MM-DD"}), 400
    elif start_date_str and end_date_str:
        try:
            fecha_inicio = datetime.strptime(start_date_str, "%Y-%m-%d")
            fecha_fin = datetime.strptime(end_date_str, "%Y-%m-%d") + timedelta(days=1)
        except ValueError:
            return jsonify({"error": "Fechas inválidas. Usa YYYY-MM-DD"}), 400
    else:
        return jsonify({"error": "Se requiere 'date' o 'start' y 'end'"}), 400

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        UPDATE citas
        SET estado = 'Completada'
        WHERE peluqueroid = %s
        AND fechafin < NOW()
        AND estado = 'Pendiente'
    """, (barber_id,))
    connection.commit()

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
    """, (barber_id, fecha_inicio, fecha_fin))

    citas = cursor.fetchall()
    cursor.close()
    connection.close()

    citas_list = []
    now = datetime.now()
    for cita in citas:
        cita_dict = dict(cita)
        fechainicio = cita_dict.get("fechainicio")
        fechafin = cita_dict.get("fechafin")

        if fechainicio:
            cita_dict["fechainicio"] = fechainicio.strftime("%Y-%m-%d %H:%M")
        if fechafin:
            cita_dict["fechafin"] = fechafin.strftime("%Y-%m-%d %H:%M")
            cita_dict["finalizada"] = fechafin < now
        else:
            cita_dict["finalizada"] = False
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


@barbers_bp.route('/toggle_barber_role/<int:user_id>', methods=['PUT'])
def toggle_barber_role(user_id):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT rol FROM usuarios WHERE usuarioid = %s", (user_id,))
    result = cursor.fetchone()

    if not result:
        cursor.close()
        connection.close()
        return jsonify({"error": "Usuario no encontrado"}), 404

    current_role = result[0]

    if current_role not in ('peluquero', 'semiadmin'):
        cursor.close()
        connection.close()
        return jsonify({"error": "El usuario no tiene rol compatible para cambio"}), 400

    new_role = 'semiadmin' if current_role == 'peluquero' else 'peluquero'

    cursor.execute("UPDATE usuarios SET rol = %s WHERE usuarioid = %s", (new_role, user_id))
    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": f"Rol actualizado a {new_role}"}), 200


@barbers_bp.route('/deactivate_barber/<int:user_id>', methods=['PUT'])
def deactivate_barber(user_id):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT * FROM peluqueros WHERE usuarioid = %s", (user_id,))
    if not cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "Barbero no encontrado"}), 404

    cursor.execute("UPDATE peluqueros SET activo = FALSE WHERE usuarioid = %s", (user_id,))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Barbero desactivado correctamente"}), 200


