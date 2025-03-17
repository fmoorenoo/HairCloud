import psycopg2
from flask import Flask, jsonify, request
from flask_bcrypt import Bcrypt

app = Flask(__name__)
bcrypt = Bcrypt(app)

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


# Login
@app.route('/login', methods=['POST'])
def login():
    data = request.json
    nombreusuario = data.get('nombreusuario')
    password = data.get('password')

    # Buscar usuario en la base de datos
    cursor.execute("SELECT usuarioid, nombreusuario, contraseña, rol FROM usuarios WHERE nombreusuario = %s", (nombreusuario,))
    user = cursor.fetchone()

    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404

    user_id, username, hashed_password, rol = user

    # Verificar contraseña
    if bcrypt.check_password_hash(hashed_password, password):
        return jsonify({
            "message": "Login realizado correctamente",
            "usuarioid": user_id,
            "nombreusuario": username,
            "rol": rol
        }), 200
    else:
        return jsonify({"error": "Contraseña incorrecta"}), 401



if __name__ == '__main__':
    app.run(debug=True)