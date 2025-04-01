from flask import request, jsonify
from app.api import clients_bp
from app.db.connection import get_connection


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
