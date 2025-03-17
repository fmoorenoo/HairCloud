import psycopg2
from flask import Flask, jsonify

app = Flask(__name__)


# Conexión a la base de datos
connection = psycopg2.connect(
    host="localhost",
    port="5432",
    dbname="HairCloud",
    user="postgres",
    password="profesor1234",
    options="-c search_path=public"
)
cursor = connection.cursor()


# Prueba de conexión
@app.route('/test_connection', methods=['GET'])
def test_connection():
    try:
        cursor.execute("SELECT 1")
        result = cursor.fetchone()

        if result:
            return jsonify({"message": "Conectado a la base de datos correctamente."}), 200
        else:
            return jsonify({"message": "Error al realizar la consulta."}), 500
    except Exception as e:
        return jsonify({"message": f"Error en la conexión: {str(e)}"}), 500


if __name__ == '__main__':
    app.run(debug=True)