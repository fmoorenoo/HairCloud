from flask import jsonify, request
from app.api import barbershops_bp
from app.db.connection import get_connection
from datetime import time, date, datetime

@barbershops_bp.route('/get_all_barbershops/<int:clienteid>', methods=['GET'])
def get_all_barbershops(clienteid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT 
            l.*,
            ROUND(AVG(r.calificacion)::numeric, 1) AS rating,
            COUNT(r.resenaid) AS cantidad_resenas,
            CASE WHEN fc.localid IS NOT NULL THEN true ELSE false END AS es_favorito
        FROM local l
        LEFT JOIN resenas r ON l.localid = r.localid AND r.peluqueroid IS NULL
        LEFT JOIN favoritos_clientes fc ON fc.localid = l.localid AND fc.clienteid = %s
        GROUP BY l.localid, fc.localid
        ORDER BY l.localid
    """, (clienteid,))

    barberias = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for b in barberias:
        item = {}
        for i, value in enumerate(b):
            if isinstance(value, (time, datetime, date)):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200


@barbershops_bp.route('/get_barbershop/<int:clienteid>/<int:localid>', methods=['GET'])
def get_barbershop(clienteid, localid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT 
            l.*,
            ROUND(AVG(r.calificacion)::numeric, 1) AS rating,
            COUNT(r.resenaid) AS cantidad_resenas,
            CASE WHEN fc.localid IS NOT NULL THEN true ELSE false END AS es_favorito
        FROM local l
            LEFT JOIN resenas r ON l.localid = r.localid AND r.peluqueroid IS NULL
            LEFT JOIN favoritos_clientes fc ON fc.localid = l.localid AND fc.clienteid = %s
            WHERE l.localid = %s
        GROUP BY l.localid, fc.localid;
    """, (clienteid, localid))

    row = cursor.fetchone()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    if row is None:
        return jsonify({"error": "Barbería no encontrada"}), 404

    item = {}
    for i, value in enumerate(row):
        if isinstance(value, (time, datetime, date)):
            item[column_names[i]] = value.isoformat()
        else:
            item[column_names[i]] = value

    return jsonify(item), 200


@barbershops_bp.route('/update_barbershop/<int:localid>', methods=['PUT'])
def update_barbershop(localid):
    data = request.get_json()
    campos = ['nombre', 'direccion', 'telefono', 'horarioapertura', 'horariocierre', 'descripcion']

    connection = get_connection()
    cursor = connection.cursor()

    updates = []
    values = []

    for campo in campos:
        if campo in data:
            updates.append(f"{campo} = %s")
            values.append(data[campo])

    if not updates:
        return jsonify({"error": "No hay datos para actualizar"}), 400

    values.append(localid)

    query = f"""
        UPDATE local
        SET {", ".join(updates)}
        WHERE localid = %s
    """

    cursor.execute(query, tuple(values))
    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Info actualizada correctamente"}), 200


@barbershops_bp.route('/get_favorite_barbershops/<int:clienteid>', methods=['GET'])
def get_favorite_barbershops(clienteid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT 
            l.*,
            ROUND(AVG(r.calificacion)::numeric, 1) AS rating,
            COUNT(r.resenaid) AS cantidad_resenas,
            true AS es_favorito
        FROM favoritos_clientes fc
        JOIN local l ON fc.localid = l.localid
        LEFT JOIN resenas r ON l.localid = r.localid AND r.peluqueroid IS NULL
        WHERE fc.clienteid = %s
        GROUP BY l.localid
        ORDER BY l.localid
    """, (clienteid,))

    favoritos = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for b in favoritos:
        item = {}
        for i, value in enumerate(b):
            if isinstance(value, (time, datetime, date)):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200


@barbershops_bp.route('/add_favorite', methods=['POST'])
def add_favorite():
    data = request.get_json()
    clienteid = data['clienteid']
    localid = data['localid']

    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute("""
        INSERT INTO favoritos_clientes (clienteid, localid, fecha_agregado)
        VALUES (%s, %s, NOW())
        ON CONFLICT DO NOTHING
    """, (clienteid, localid))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Favorito agregado"}), 201


@barbershops_bp.route('/remove_favorite', methods=['DELETE'])
def remove_favorite():
    data = request.get_json()
    clienteid = data['clienteid']
    localid = data['localid']

    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute("""
        DELETE FROM favoritos_clientes
        WHERE clienteid = %s AND localid = %s
    """, (clienteid, localid))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Favorito eliminado"}), 200


@barbershops_bp.route('/get_barbershop_reviews/<int:localid>', methods=['GET'])
def get_barbershop_reviews(localid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT r.*, c.nombre AS cliente_nombre
        FROM resenas r
        JOIN clientes c ON r.clienteid = c.clienteid
        WHERE r.localid = %s AND r.peluqueroid IS NULL
        ORDER BY r.fecharesena DESC
    """, (localid,))

    resenas = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    result = []
    for r in resenas:
        item = {}
        for i, value in enumerate(r):
            if isinstance(value, (time, datetime, date)):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200


@barbershops_bp.route('/add_review', methods=['POST'])
def add_review():
    data = request.get_json()
    clienteid = data['clienteid']
    localid = data['localid']
    calificacion = data['calificacion']
    comentario = data.get('comentario', '')

    connection = get_connection()
    cursor = connection.cursor()

    # Cantidad de reseñas del cliente para esa barbería
    cursor.execute("""
        SELECT COUNT(*) FROM resenas
        WHERE clienteid = %s AND localid = %s AND peluqueroid IS NULL
    """, (clienteid, localid))
    count = cursor.fetchone()[0]

    if count >= 3:
        cursor.close()
        connection.close()
        return jsonify({"error": "Ya tienes 3 reseñas en esta barbería"}), 400

    # Insertar nueva reseña
    cursor.execute("""
        INSERT INTO resenas (clienteid, localid, calificacion, comentario, fecharesena, peluqueroid)
        VALUES (%s, %s, %s, %s, NOW(), NULL)
    """, (clienteid, localid, calificacion, comentario))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Reseña añadida correctamente"}), 201


@barbershops_bp.route('/delete_review/<int:resenaid>', methods=['DELETE'])
def delete_review(resenaid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("DELETE FROM resenas WHERE resenaid = %s", (resenaid,))

    if cursor.rowcount == 0:
        cursor.close()
        connection.close()
        return jsonify({"error": "Reseña no encontrada"}), 404

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Reseña eliminada"}), 200


@barbershops_bp.route('/get_barbers/<int:localid>', methods=['GET'])
def get_barbers(localid):
    connection = get_connection()
    cursor = connection.cursor()

    cursor.execute("""
        SELECT * FROM peluqueros
        WHERE localid = %s
        ORDER BY nombre
    """, (localid,))

    peluqueros = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]

    cursor.close()
    connection.close()

    result = []
    for p in peluqueros:
        item = {}
        for i, value in enumerate(p):
            if isinstance(value, (time, datetime, date)):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200
