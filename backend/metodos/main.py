import os
import psycopg2
from flask import Flask, jsonify, request
from flask_bcrypt import Bcrypt
from flask_mail import Mail, Message
import random
import datetime
from dotenv import load_dotenv

load_dotenv()
app = Flask(__name__)
bcrypt = Bcrypt(app)

# Configuración para enviar correos
app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = os.getenv('MAIL_USERNAME')
app.config['MAIL_PASSWORD'] = os.getenv('MAIL_PASSWORD')
app.config['MAIL_DEFAULT_SENDER'] = "Soporte de HairCloud"

mail = Mail(app)

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


# Registrar un nuevo usuario (cliente)
@app.route('/register', methods=['POST'])
def register():
    data = request.json
    nombre_completo = data.get('nombre_completo')
    email = data.get('email')
    nombreusuario = data.get('nombreusuario')
    password = data.get('password')

    # Verificar si el email y/o el usuario ya están registrados
    cursor.execute("SELECT usuarioid FROM usuarios WHERE email = %s", (email,))
    if cursor.fetchone():
        return jsonify({"error": "El email ya está registrado"}), 400

    cursor.execute("SELECT usuarioid FROM usuarios WHERE nombreusuario = %s", (nombreusuario,))
    if cursor.fetchone():
        return jsonify({"error": "El nombre de usuario ya está en uso"}), 400

    # Encriptar la contraseña
    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    # Insertar en la tabla de usuarios y clientes
    cursor.execute(
        "INSERT INTO usuarios (nombreusuario, contraseña, rol, localid, email) VALUES (%s, %s, %s, %s, %s) RETURNING usuarioid",
        (nombreusuario, hashed_password, 'cliente', None, email)
    )
    usuarioid = cursor.fetchone()[0]

    cursor.execute(
        "INSERT INTO clientes (usuarioid, nombre, telefono, fecharegistro, ultimacita) VALUES (%s, %s, NULL, NOW(), NULL)",
        (usuarioid, nombre_completo)
    )

    connection.commit()
    return jsonify({"message": "Registro realizado correctamente", "usuarioid": usuarioid}), 201


# Enviar correo por pérdida de contraseña
@app.route('/forgot_password', methods=['POST'])
def forgot_password():
    data = request.json
    email = data.get('email')

    # Verificar si el email está registrado
    cursor.execute("SELECT usuarioid FROM usuarios WHERE email = %s", (email,))
    user = cursor.fetchone()

    if not user:
        return jsonify({"error": "No existe ninguna cuenta con ese email"}), 404

    # Generar código de 6 dígitos
    codigo = f"{random.randint(100000, 999999)}"
    expiracion = datetime.datetime.now() + datetime.timedelta(minutes=3)

    # Guardar el código en la base de datos (reemplazar si ya existe)
    cursor.execute("""
        INSERT INTO codigos_recup_contrasenas (email, codigo, expiracion)
        VALUES (%s, %s, %s)
        ON CONFLICT (email) DO UPDATE SET codigo = EXCLUDED.codigo, expiracion = EXCLUDED.expiracion
    """, (email, codigo, expiracion))
    connection.commit()

    # Enviar correo con el código
    try:
        msg = Message(
            "Recuperación de contraseña - HairCloud",
            recipients=[email]
        )
        msg.body = f"""
        Hola,

        Tu código de verificación es: {codigo}

        Este código expirará en 3 minutos.

        Saludos,  
        El equipo de HairCloud
        """

        mail.send(msg)
        return jsonify({"message": "Código de verificación enviado"}), 200
    except Exception as e:
        return jsonify({"error": f"No se pudo enviar el email: {str(e)}"}), 500


# Verificar código
@app.route('/verify_code', methods=['POST'])
def verify_code():
    data = request.json
    email = data.get('email')
    codigo_ingresado = data.get('codigo')

    # Buscar el código en la base de datos
    cursor.execute("SELECT codigo, expiracion FROM codigos_recup_contrasenas WHERE email = %s", (email,))
    record = cursor.fetchone()

    if not record:
        return jsonify({"error": "Código no encontrado"}), 404

    codigo_correcto, expiracion = record

    # Verificar si el código ha expirado
    if datetime.datetime.now() > expiracion:
        return jsonify({"error": "El código ha expirado"}), 400

    # Verificar si el código es correcto
    if codigo_correcto == codigo_ingresado:
        return jsonify({"message": "Código verificado correctamente"}), 200
    else:
        return jsonify({"error": "Código incorrecto"}), 400


# Restablecer la contraseña
@app.route('/reset_password', methods=['POST'])
def reset_password():
    data = request.json
    email = data.get('email')
    codigo_ingresado = data.get('codigo')
    nueva_password = data.get('password')

    # Verificar código antes de restablecer contraseña
    cursor.execute("SELECT codigo, expiracion FROM codigos_recup_contrasenas WHERE email = %s", (email,))
    record = cursor.fetchone()

    if not record:
        return jsonify({"error": "Código no encontrado"}), 404

    codigo_correcto, expiracion = record

    if datetime.datetime.now() > expiracion:
        return jsonify({"error": "El código ha expirado"}), 400

    if codigo_correcto != codigo_ingresado:
        return jsonify({"error": "Código incorrecto"}), 400

    # Encriptar la nueva contraseña
    hashed_password = bcrypt.generate_password_hash(nueva_password).decode('utf-8')

    # Actualizar la contraseña en la base de datos
    cursor.execute("UPDATE usuarios SET contraseña = %s WHERE email = %s", (hashed_password, email))
    connection.commit()

    # Eliminar el código usado
    cursor.execute("DELETE FROM codigos_recup_contrasenas WHERE email = %s", (email,))
    connection.commit()

    return jsonify({"message": "Contraseña restablecida correctamente"}), 200


if __name__ == '__main__':
    app.run(debug=True)