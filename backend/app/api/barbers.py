from flask import jsonify
from app.api import barbershops_bp
from app.db.connection import get_connection
from datetime import time, date, datetime

@barbershops_bp.route('/get_barbershops', methods=['GET'])
def get_barbershops():
    connection = get_connection()
    cursor = connection.cursor()

    # Traemos barberías con calificación promedio y número de reseñas hechas al local
    cursor.execute("""
        SELECT 
            l.*,
            ROUND(AVG(r.calificacion)::numeric, 1) AS rating,
            COUNT(r.resenaid) AS cantidad_resenas
        FROM local l
        LEFT JOIN resenas r ON l.localid = r.localid AND r.peluqueroid IS NULL
        GROUP BY l.localid
        ORDER BY l.localid
    """)

    barberias = cursor.fetchall()
    column_names = [desc[0] for desc in cursor.description]
    cursor.close()
    connection.close()

    result = []
    for b in barberias:
        item = {}
        for i, value in enumerate(b):
            if isinstance(value, time):
                item[column_names[i]] = value.strftime('%H:%M:%S')
            elif isinstance(value, datetime):
                item[column_names[i]] = value.isoformat()
            elif isinstance(value, date):
                item[column_names[i]] = value.isoformat()
            else:
                item[column_names[i]] = value
        result.append(item)

    return jsonify(result), 200



