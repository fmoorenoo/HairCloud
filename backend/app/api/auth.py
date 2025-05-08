from flask import request, jsonify
from datetime import datetime, timedelta, timezone
import random
from app.api import auth_bp
from app.db.connection import get_connection
from app.extensions import bcrypt
from app.utils.email import send_verification_email
import jwt as pyjwt
import os
from dotenv import load_dotenv


@auth_bp.route('/test_connection', methods=['GET'])
def test_connection():
    try:
        connection = get_connection()
        cursor = connection.cursor()
        cursor.execute("SELECT 1")
        result = cursor.fetchone()
        cursor.close()
        connection.close()

        if result:
            return jsonify({"message": "Conectado a la base de datos correctamente."}), 200
        else:
            return jsonify({"message": "Error al realizar la consulta."}), 500
    except Exception as e:
        return jsonify({"message": f"Error en la conexión: {str(e)}"}), 500


@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.json
    nombreusuario = data.get('nombreusuario')
    password = data.get('password')

    connection = get_connection()
    cursor = connection.cursor()

    # Buscar usuario en la base de datos
    cursor.execute("SELECT usuarioid, nombreusuario, contraseña, rol FROM usuarios WHERE nombreusuario = %s",
                   (nombreusuario,))
    user = cursor.fetchone()
    cursor.close()
    connection.close()

    if not user:
        return jsonify({"error": "Usuario no encontrado"}), 404

    user_id, username, hashed_password, rol = user

    # Verificar contraseña
    if bcrypt.check_password_hash(hashed_password, password):
        load_dotenv()
        SECRET_KEY = os.getenv("SECRET_KEY")
        exp_time = datetime.now(timezone.utc) + timedelta(days=7)

        token = pyjwt.encode(
            {
                "usuarioid": user_id,
                "rol": rol,
                "exp": exp_time
            },
            SECRET_KEY,
            algorithm="HS256"
        )
        return jsonify({
            "message": "Login realizado correctamente",
            "usuarioid": user_id,
            "nombreusuario": username,
            "rol": rol,
            "token": token
        }), 200
    else:
        return jsonify({"error": "Contraseña incorrecta"}), 401


@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.json
    nombre_completo = data.get('nombre_completo')
    email = data.get('email')
    nombreusuario = data.get('nombreusuario')
    password = data.get('password')

    connection = get_connection()
    cursor = connection.cursor()

    # Verificar si el email y/o el usuario ya están registrados
    cursor.execute("SELECT usuarioid FROM usuarios WHERE email = %s", (email,))
    if cursor.fetchone():
        cursor.close()
        connection.close()
        return jsonify({"error": "El email ya está registrado"}), 400

    cursor.execute("SELECT usuarioid FROM usuarios WHERE nombreusuario = %s", (nombreusuario,))
    if cursor.fetchone():
        cursor.close()
        connection.close()
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
        "INSERT INTO clientes (usuarioid, nombre, telefono, fecharegistro) VALUES (%s, %s, NULL, NOW())",
        (usuarioid, nombre_completo)
    )

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Registro realizado correctamente", "usuarioid": usuarioid}), 201


@auth_bp.route('/send_verification_code', methods=['POST'])
def send_verification_code():
    data = request.json
    email = data.get('email')
    purpose = data.get('purpose')  # "password_reset" o "email_verification"
    minutos = 3

    connection = get_connection()
    cursor = connection.cursor()
    username = None

    if purpose not in ["password_reset", "email_verification"]:
        return jsonify({"error": "Propósito no válido"}), 400

    # Si es verificación de email, comprobar que el correo no esté registrado
    if purpose == "email_verification":
        cursor.execute("SELECT usuarioid FROM usuarios WHERE email = %s", (email,))
        if cursor.fetchone():
            cursor.close()
            connection.close()
            return jsonify({"error": "El email ya está registrado"}), 400

    # Si es recuperación de contraseña, verificar que el correo sí exista
    elif purpose == "password_reset":
        cursor.execute("SELECT usuarioid, nombreusuario FROM usuarios WHERE email = %s", (email,))
        user = cursor.fetchone()
        if not user:
            cursor.close()
            connection.close()
            return jsonify({"error": "No existe ninguna cuenta con ese email"}), 404

        usuarioid, username = user

    # Comprobar si ya existe un código no expirado para el mismo propósito
    cursor.execute("""
        SELECT expiracion FROM codigos_verificacion 
        WHERE email = %s AND tipo = %s AND expiracion > NOW()
    """, (email, purpose))
    existing_code = cursor.fetchone()

    if existing_code:
        # Calcular el tiempo restante
        tiempo_restante = existing_code[0] - datetime.now()
        if tiempo_restante.total_seconds() > 0:
            minutos_restantes, segundos_restantes = divmod(tiempo_restante.total_seconds(), 60)
            tiempo_formateado = f"{int(minutos_restantes):02d}:{int(segundos_restantes):02d}"
            cursor.close()
            connection.close()
            return jsonify(
                {"error": f"Espera {minutos} minutos para solicitar otro código.\nRestante: {tiempo_formateado}"}), 429

    # Generar código de 6 dígitos
    codigo = f"{random.randint(100000, 999999)}"
    expiracion = datetime.now() + timedelta(minutes=minutos)

    try:
        # Enviar el email
        send_verification_email(email, codigo, minutos, purpose)

        # Guardar el código en la base de datos (reemplazar si ya existe)
        cursor.execute("""
            INSERT INTO codigos_verificacion (email, codigo, expiracion, tipo)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (email, tipo) DO UPDATE SET codigo = EXCLUDED.codigo, expiracion = EXCLUDED.expiracion
        """, (email, codigo, expiracion, purpose))
        connection.commit()

        # Devolver el nombre de usuario si es recuperación de contraseña
        response_data = {"message": "Código de verificación enviado"}
        if purpose == "password_reset":
            response_data["username"] = username

        cursor.close()
        connection.close()
        return jsonify(response_data), 200
    except Exception as e:
        cursor.close()
        connection.close()
        return jsonify({"error": f"No se pudo enviar el email"}), 500


@auth_bp.route('/verify_code', methods=['POST'])
def verify_code():
    data = request.json
    email = data.get('email')
    codigo_ingresado = data.get('codigo')
    purpose = data.get('purpose')

    connection = get_connection()
    cursor = connection.cursor()

    if purpose not in ["password_reset", "email_verification"]:
        cursor.close()
        connection.close()
        return jsonify({"error": "Propósito no válido"}), 400

    # Buscar código en la base de datos
    cursor.execute(
        "SELECT codigo, expiracion FROM codigos_verificacion WHERE email = %s AND tipo = %s",
        (email, purpose)
    )
    record = cursor.fetchone()

    if not record:
        cursor.close()
        connection.close()
        return jsonify({"error": "Código no encontrado"}), 404

    codigo_correcto, expiracion = record

    # Verificar si el código ha expirado
    if datetime.now() > expiracion:
        cursor.close()
        connection.close()
        return jsonify({"error": "El código ha expirado"}), 400

    # Verificar si el código es correcto
    if codigo_correcto == codigo_ingresado:
        if purpose == "email_verification":
            # Eliminar el código usado
            cursor.execute("DELETE FROM codigos_verificacion WHERE email = %s AND tipo = 'email_verification'", (email,))
            connection.commit()
        cursor.close()
        connection.close()
        return jsonify({"message": "Código verificado correctamente"}), 200
    else:
        cursor.close()
        connection.close()
        return jsonify({"error": "Código incorrecto"}), 400


@auth_bp.route('/reset_password', methods=['POST'])
def reset_password():
    data = request.json
    email = data.get('email')
    codigo_ingresado = data.get('codigo')
    nueva_password = data.get('password')

    connection = get_connection()
    cursor = connection.cursor()

    # Verificar código antes de restablecer contraseña
    cursor.execute("SELECT codigo, expiracion FROM codigos_verificacion WHERE email = %s AND tipo = 'password_reset'",
                   (email,))
    record = cursor.fetchone()

    if not record:
        cursor.close()
        connection.close()
        return jsonify({"error": "Código no encontrado"}), 404

    codigo_correcto, expiracion = record

    if datetime.now() > expiracion:
        cursor.close()
        connection.close()
        return jsonify({"error": "El código ha expirado"}), 400

    if codigo_correcto != codigo_ingresado:
        cursor.close()
        connection.close()
        return jsonify({"error": "Código incorrecto"}), 400

    # Encriptar la nueva contraseña
    hashed_password = bcrypt.generate_password_hash(nueva_password).decode('utf-8')

    # Actualizar la contraseña en la base de datos
    cursor.execute("UPDATE usuarios SET contraseña = %s WHERE email = %s", (hashed_password, email))

    # Eliminar el código usado
    cursor.execute("DELETE FROM codigos_verificacion WHERE email = %s AND tipo = 'password_reset'", (email,))

    connection.commit()
    cursor.close()
    connection.close()

    return jsonify({"message": "Contraseña restablecida correctamente"}), 200