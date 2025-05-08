from flask import jsonify, request
from app.api import services_bp
from app.db.connection import get_connection


@services_bp.route('/get_services/<int:localid>', methods=['GET'])
def get_services(localid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT * FROM servicios
        WHERE localid = %s
        ORDER BY servicioid
    """, (localid,))

    servicios = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    result = []
    for s in servicios:
        item = {}
        for i, value in enumerate(s):
            item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200


@services_bp.route('/get_service/<int:servicioid>', methods=['GET'])
def get_service_by_id(servicioid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT * FROM servicios
        WHERE servicioid = %s
    """, (servicioid,))

    servicio = cursor.fetchone()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    if servicio is None:
        return jsonify({"error": "Servicio no encontrado"}), 404

    result = {}
    for i, value in enumerate(servicio):
        result[column_names[i]] = value

    return jsonify(result), 200


@services_bp.route('/create_service', methods=['POST'])
def create_service():
    data = request.get_json()
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT 1 FROM servicios
        WHERE nombre = %s AND localid = %s
    """, (data['nombre'], data['localId']))

    if cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"message": f"Ya existe un servicio con ese nombre"}), 409

    cursor.execute("""
        INSERT INTO servicios (nombre, descripcion, duracion, precio, localid)
        VALUES (%s, %s, %s, %s, %s)
        RETURNING servicioid
    """, (
        data['nombre'],
        data.get('descripcion'),
        data['duracion'],
        data['precio'],
        data['localId']
    ))

    servicioid = cursor.fetchone()[0]
    connection.commit()

    cursor.close()
    connection.close()

    service_name = data['nombre']
    message = f'Servicio "{service_name}" creado'

    return jsonify({"message": message, "servicioid": servicioid}), 201


@services_bp.route('/edit_service/<int:servicioid>', methods=['PUT'])
def edit_service(servicioid):
    data = request.get_json()
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        UPDATE servicios
        SET nombre = %s, descripcion = %s, duracion = %s, precio = %s, localid = %s
        WHERE servicioid = %s
    """, (
        data['nombre'],
        data.get('descripcion'),
        data['duracion'],
        data['precio'],
        data['localId'],
        servicioid
    ))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Servicio actualizado"}), 200


@services_bp.route('/delete_service/<int:servicioid>', methods=['DELETE'])
def delete_service(servicioid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        DELETE FROM servicios
        WHERE servicioid = %s
    """, (servicioid,))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Servicio eliminado"}), 200