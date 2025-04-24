from flask import request, jsonify
from app.api import clients_bp
from app.db.connection import get_connection
from datetime import datetime


@clients_bp.route('/get_client/<int:client_id>', methods=['GET'])
def get_client(client_id):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT c.*, u.email, u.nombreusuario 
        FROM clientes c
        JOIN usuarios u ON c.usuarioid = u.usuarioid
        WHERE c.usuarioid = %s
    """, (client_id,))

    client = cursor.fetchone()
    cursor.close()
    connection.close()

    if not client:
        return jsonify({"error": "Cliente no encontrado"}), 404

    client_data = dict(client)

    if client_data.get("fecharegistro"):
        client_data["fecharegistro"] = client_data["fecharegistro"].strftime("%Y-%m-%d")

    return jsonify(client_data), 200


@clients_bp.route('/update_client/<int:user_id>', methods=['PUT'])
def update_client(user_id):
    data = request.json or {}
    email = data.get("email")
    nombreusuario = data.get("nombreusuario")
    nombre = data.get("nombre")
    telefono = data.get("telefono")
    if telefono == "nulo":
        telefono = None

    connection = get_connection()
    cursor = connection.cursor()

    # Verificar que el cliente existe
    cursor.execute("SELECT * FROM clientes WHERE usuarioid = %s", (user_id,))
    if not cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "Cliente no encontrado"}), 404

    # Validaciones: email y nombreusuario deben ser únicos (si se proporcionan)
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

    # Actualizar tabla 'usuarios'
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

    # Actualizar tabla 'clientes'
    client_updates = []
    client_values = []

    if nombre is not None:
        client_updates.append("nombre = %s")
        client_values.append(nombre)
    if "telefono" in data:
        client_updates.append("telefono = %s")
        client_values.append(telefono)

    if client_updates:
        client_values.append(user_id)
        cursor.execute(f"""
            UPDATE clientes
            SET {', '.join(client_updates)}
            WHERE usuarioid = %s
        """, tuple(client_values))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Info actualizada correctamente"}), 200


@clients_bp.route('/get_client_dates/<int:client_id>', methods=['GET'])
def get_client_dates(client_id):
    now = datetime.now()

    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT 
            c.*, 
            s.nombre AS servicio_nombre, 
            s.precio AS servicio_precio, 
            s.duracion AS servicio_duracion, 
            b.nombre AS barber_nombre,
            l.nombre AS local_nombre,
            l.direccion AS local_direccion
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN peluqueros b ON c.peluqueroid = b.peluqueroid
        JOIN local l ON c.localid = l.localid
        WHERE c.clienteid = %s
        ORDER BY c.fechainicio ASC
    """, (client_id,))

    citas = cursor.fetchall()
    cursor.close()
    connection.close()

    citas_list = []
    for cita in citas:
        cita_dict = dict(cita)

        for key in ["fechainicio", "fechafin"]:
            if cita_dict.get(key):
                cita_dict[key] = cita_dict[key].strftime("%Y-%m-%d %H:%M")

        fechafin_obj = cita["fechafin"]
        cita_dict["finalizada"] = fechafin_obj < now if fechafin_obj else False

        citas_list.append(cita_dict)

    return jsonify({"appointments": citas_list}), 200


@clients_bp.route('/get_client_stats/<int:client_id>', methods=['GET'])
def get_client_stats(client_id):
    now = datetime.now()

    connection = get_connection()
    cursor = connection.cursor()

    # Total de citas finalizadas
    cursor.execute("""
        SELECT COUNT(*) 
        FROM citas 
        WHERE clienteid = %s AND fechafin < %s
    """, (client_id, now))
    total_citas_finalizadas = cursor.fetchone()[0]

    # Local con más citas
    cursor.execute("""
        SELECT l.nombre, COUNT(*) as total
        FROM citas c
        JOIN local l ON c.localid = l.localid
        WHERE c.clienteid = %s AND c.fechafin < %s
        GROUP BY l.nombre
        ORDER BY total DESC
        LIMIT 1
    """, (client_id, now))
    local_mas_frecuentado = cursor.fetchone()
    local_nombre = local_mas_frecuentado[0] if local_mas_frecuentado else None
    local_visitas = local_mas_frecuentado[1] if local_mas_frecuentado else 0

    # Próxima cita
    cursor.execute("""
        SELECT c.*, s.nombre AS servicio_nombre, b.nombre AS peluquero_nombre, l.nombre AS local_nombre
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN peluqueros b ON c.peluqueroid = b.peluqueroid
        JOIN local l ON c.localid = l.localid
        WHERE c.clienteid = %s AND c.fechainicio > %s
        ORDER BY c.fechainicio ASC
        LIMIT 1
    """, (client_id, now))
    proxima_cita = cursor.fetchone()
    proxima_cita_dict = None
    if proxima_cita:
        proxima_cita_dict = dict(proxima_cita)
        for key in ["fechainicio", "fechafin"]:
            if proxima_cita_dict.get(key):
                proxima_cita_dict[key] = proxima_cita_dict[key].strftime("%Y-%m-%d %H:%M")

    # Servicio más solicitado + nombre del local
    cursor.execute("""
        SELECT s.nombre AS servicio_nombre, l.nombre AS local_nombre, COUNT(*) as total
        FROM citas c
        JOIN servicios s ON c.servicioid = s.servicioid
        JOIN local l ON c.localid = l.localid
        WHERE c.clienteid = %s AND c.fechafin < %s
        GROUP BY s.nombre, l.nombre
        ORDER BY total DESC
        LIMIT 1
    """, (client_id, now))
    servicio_favorito = cursor.fetchone()
    servicio_nombre = servicio_favorito[0] if servicio_favorito else None
    servicio_local = servicio_favorito[1] if servicio_favorito else None

    cursor.close()
    connection.close()

    return jsonify({
        "total_citas_finalizadas": total_citas_finalizadas,
        "local_mas_frecuentado": local_nombre,
        "local_mas_frecuentado_visitas": local_visitas,
        "proxima_cita": proxima_cita_dict,
        "servicio_favorito": servicio_nombre,
        "servicio_favorito_local": servicio_local
    }), 200