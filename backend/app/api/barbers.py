from flask import request, jsonify

from app import bcrypt
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
    especialidad = data.get("especialidad")
    if especialidad == "nulo":
        especialidad = None

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
    if "especialidad" in data:
        barber_updates.append("especialidad = %s")
        barber_values.append(especialidad)

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


@barbers_bp.route('/activate_barber/<int:user_id>', methods=['PUT'])
def activate_barber(user_id):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT * FROM peluqueros WHERE usuarioid = %s", (user_id,))
    if not cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "Barbero no encontrado"}), 404

    cursor.execute("UPDATE peluqueros SET activo = TRUE WHERE usuarioid = %s", (user_id,))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Barbero activado correctamente"}), 200


@barbers_bp.route('/get_inactive_barbers', methods=['GET'])
def get_inactive_barbers():
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT p.*, u.email, u.nombreusuario, u.rol
        FROM peluqueros p
        JOIN usuarios u ON p.usuarioid = u.usuarioid
        WHERE p.activo = FALSE
        ORDER BY p.nombre
    """)

    barbers = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    result = []
    for barber in barbers:
        item = {}
        for i, value in enumerate(barber):
            if isinstance(value, (datetime, timedelta)):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200


@barbers_bp.route('/create_barber', methods=['POST'])
def create_barber():
    data = request.json or {}

    nombreusuario = data.get("nombreusuario")
    contrasena = data.get("contrasena")
    email = data.get("email")
    nombre = data.get("nombre")
    especialidad = data.get("especialidad")
    localid = data.get("localid")
    work_schedules = data.get("horario")

    if not all([nombreusuario, contrasena, email, nombre, localid, work_schedules]):
        return jsonify({"error": "Faltan campos obligatorios"}), 400

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT 1 FROM usuarios WHERE nombreusuario = %s OR email = %s", (nombreusuario, email))
    if cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "El nombre de usuario o email ya existen"}), 404

    hashed_password = bcrypt.generate_password_hash(contrasena).decode('utf-8')

    cursor.execute("""
        INSERT INTO usuarios (nombreusuario, contraseña, rol, localid, email)
        VALUES (%s, %s, 'peluquero', %s, %s)
        RETURNING usuarioid
    """, (nombreusuario, hashed_password, localid, email))

    usuarioid = cursor.fetchone()[0]

    cursor.execute("""
        INSERT INTO peluqueros (usuarioid, nombre, telefono, especialidad, fechacontratacion, localid, activo)
        VALUES (%s, %s, NULL, %s, CURRENT_DATE, %s, TRUE)
        RETURNING peluqueroid
    """, (usuarioid, nombre, especialidad, localid))

    peluqueroid = cursor.fetchone()[0]

    if work_schedules:
        for item in work_schedules:
            dia = item.get("dia")
            inicio = item.get("inicio")
            fin = item.get("fin")
            if dia and inicio and fin:
                cursor.execute("""
                    INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin)
                    VALUES (%s, %s, %s, %s)
                """, (peluqueroid, dia, inicio, fin))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Barbero añadido correctamente al personal", "usuarioid": usuarioid}), 201


@barbers_bp.route('/update_barber_schedule/<int:peluquero_id>', methods=['PUT'])
def update_barber_schedule(peluquero_id):
    data = request.json or []
    if not isinstance(data, list):
        return jsonify({"error": "Se esperaba una lista de horarios"}), 400

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("SELECT 1 FROM peluqueros WHERE peluqueroid = %s", (peluquero_id,))
    if not cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "Peluquero no encontrado"}), 404

    cursor.execute("DELETE FROM horarios_peluqueros WHERE peluqueroid = %s", (peluquero_id,))

    for item in data:
        dia = item.get("dia")
        inicio = item.get("inicio")
        fin = item.get("fin")
        if not all([dia, inicio, fin]):
            continue
        cursor.execute("""
            INSERT INTO horarios_peluqueros (peluqueroid, diasemana, horainicio, horafin)
            VALUES (%s, %s, %s, %s)
        """, (peluquero_id, dia, inicio, fin))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Horario actualizado correctamente"}), 200

@barbers_bp.route('/get_barber_activity/<int:peluqueroid>', methods=['GET'])
def get_barber_activity(peluqueroid):

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        DELETE FROM actividad_peluquero
        WHERE peluqueroid = %s AND fecha < %s
    """, (peluqueroid, datetime.now() - timedelta(days=7)))

    cursor.execute("""
        SELECT 
            a.actividadid, a.tipo, a.fecha,
            c.nombre AS cliente_nombre,
            ci.fechainicio, ci.fechafin, ci.estado,
            s.nombre AS servicio_nombre,
            l.nombre AS local_nombre
        FROM actividad_peluquero a
        LEFT JOIN citas ci ON a.citaid = ci.citaid
        LEFT JOIN clientes c ON a.clienteid = c.clienteid
        LEFT JOIN servicios s ON ci.servicioid = s.servicioid
        LEFT JOIN local l ON ci.localid = l.localid
        WHERE a.peluqueroid = %s
        ORDER BY a.fecha DESC
    """, (peluqueroid,))

    actividades = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    result = []
    for row in actividades:
        actividad = {}
        for i, value in enumerate(row):
            if isinstance(value, datetime):
                actividad[column_names[i]] = value.strftime('%Y-%m-%d %H:%M')
            else:
                actividad[column_names[i]] = value
        result.append(actividad)

    return jsonify(result), 200


@barbers_bp.route('/get_barber_stats', methods=['GET'])
def get_barber_stats():
    peluqueroid = request.args.get("peluqueroid", type=int)
    localid = request.args.get("localid", type=int)
    start_date_str = request.args.get("start")
    end_date_str = request.args.get("end")

    if not all([peluqueroid, localid, start_date_str, end_date_str]):
        return jsonify({"error": "Se requieren 'peluqueroid', 'localid', 'start' y 'end'"}), 400

    try:
        fecha_inicio = datetime.strptime(start_date_str, "%Y-%m-%d")
        fecha_fin = datetime.strptime(end_date_str, "%Y-%m-%d") + timedelta(days=1)
    except ValueError:
        return jsonify({"error": "Formato de fecha inválido. Usa YYYY-MM-DD"}), 400

    connection = get_connection()
    cursor = connection.cursor()

    # Total de clientes distintos atendidos
    cursor.execute("""
        SELECT COUNT(DISTINCT clienteid)
        FROM citas
        WHERE peluqueroid = %s AND localid = %s
        AND fechainicio >= %s AND fechainicio < %s
        AND estado = 'Completada'
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    total_clientes = cursor.fetchone()[0]

    # Total citas realizadas
    cursor.execute("""
        SELECT COUNT(*)
        FROM citas
        WHERE peluqueroid = %s AND localid = %s
        AND fechainicio >= %s AND fechainicio < %s
        AND estado = 'Completada'
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    total_citas = cursor.fetchone()[0]

    # Citas canceladas
    cursor.execute("""
        SELECT COUNT(*)
        FROM citas
        WHERE peluqueroid = %s AND localid = %s
        AND fechainicio >= %s AND fechainicio < %s
        AND estado = 'Cancelada'
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    total_canceladas = cursor.fetchone()[0]

    # Citas no completadas
    cursor.execute("""
        SELECT COUNT(*)
        FROM citas
        WHERE peluqueroid = %s AND localid = %s
        AND fechainicio >= %s AND fechainicio < %s
        AND estado = 'No completada'
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    total_no_completadas = cursor.fetchone()[0]

    # Servicio más solicitado
    cursor.execute("""
        SELECT c.servicioid, s.nombre, COUNT(*) as total
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        WHERE c.peluqueroid = %s AND c.localid = %s
        AND c.fechainicio >= %s AND c.fechainicio < %s
        AND c.estado = 'Completada'
        GROUP BY c.servicioid, s.nombre
        ORDER BY total DESC
        LIMIT 1
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    servicio_data = cursor.fetchone()
    servicio_mas_solicitado = {
        "servicioid": servicio_data[0],
        "nombre": servicio_data[1],
        "cantidad": servicio_data[2]
    } if servicio_data else None

    # Ingresos totales
    cursor.execute("""
        SELECT SUM(s.precio)
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        WHERE c.peluqueroid = %s AND c.localid = %s
        AND c.fechainicio >= %s AND c.fechainicio < %s
        AND c.estado = 'Completada'
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    ingresos_totales = cursor.fetchone()[0] or 0.0

    # Cliente más frecuente
    cursor.execute("""
        SELECT cl.clienteid, cl.nombre, COUNT(*) as total_citas
        FROM citas c
        JOIN clientes cl ON c.clienteid = cl.clienteid
        WHERE c.peluqueroid = %s AND c.localid = %s
        AND c.fechainicio >= %s AND c.fechainicio < %s
        AND c.estado = 'Completada'
        GROUP BY cl.clienteid, cl.nombre
        ORDER BY total_citas DESC
        LIMIT 1
    """, (peluqueroid, localid, fecha_inicio, fecha_fin))
    cliente_data = cursor.fetchone()
    cliente_mas_frecuente = {
        "clienteid": cliente_data[0],
        "nombre": cliente_data[1],
        "total_citas": cliente_data[2]
    } if cliente_data else None


    # Media de citas por día
    dias = (fecha_fin - fecha_inicio).days or 1
    promedio_citas_por_dia = total_citas / dias

    cursor.close()
    connection.close()

    return jsonify({
        "total_clientes_atendidos": total_clientes,
        "total_citas": total_citas,
        "total_canceladas": total_canceladas,
        "total_no_completadas": total_no_completadas,
        "servicio_mas_solicitado": servicio_mas_solicitado,
        "cliente_mas_frecuente": cliente_mas_frecuente,
        "ingresos_totales": float(ingresos_totales),
        "promedio_citas_por_dia": round(promedio_citas_por_dia, 2)
    }), 200


