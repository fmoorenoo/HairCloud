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

    return jsonify(client_data), 200