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

    connection = get_connection()
    cursor = connection.cursor()

    # Verificar que el cliente existe
    cursor.execute("SELECT * FROM clientes WHERE usuarioid = %s", (user_id,))
    existing_client = cursor.fetchone()

    if not existing_client:
        cursor.close()
        connection.close()
        return jsonify({"error": "Cliente no encontrado"}), 404

    # Validaciones: email y nombreusuario deben ser únicos (si se proporcionan)
    if email:
        cursor.execute("SELECT * FROM usuarios WHERE email = %s AND usuarioid != %s", (email, user_id))
        if cursor.fetchone():
            cursor.close()
            connection.close()
            return jsonify({"error": "El email ya está en uso por otro usuario"}), 400

    if nombreusuario:
        cursor.execute("SELECT * FROM usuarios WHERE nombreusuario = %s AND usuarioid != %s", (nombreusuario, user_id))
        if cursor.fetchone():
            cursor.close()
            connection.close()
            return jsonify({"error": "El nombre de usuario ya está en uso"}), 400

    # Actualización dinámica en usuarios
    if email or nombreusuario:
        update_fields = []
        values = []

        if email:
            update_fields.append("email = %s")
            values.append(email)
        if nombreusuario:
            update_fields.append("nombreusuario = %s")
            values.append(nombreusuario)

        values.append(user_id)
        cursor.execute(f"""
            UPDATE usuarios
            SET {', '.join(update_fields)}
            WHERE usuarioid = %s
        """, tuple(values))

    # Actualización dinámica en clientes
    if nombre or telefono:
        update_fields = []
        values = []

        if nombre:
            update_fields.append("nombre = %s")
            values.append(nombre)
        if telefono:
            update_fields.append("telefono = %s")
            values.append(telefono)

        values.append(user_id)
        cursor.execute(f"""
            UPDATE clientes
            SET {', '.join(update_fields)}
            WHERE usuarioid = %s
        """, tuple(values))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Info actualizada correctamente"}), 200
