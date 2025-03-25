from flask import current_app
from flask_mail import Message
from app.extensions import mail

# Enviar emails
def send_verification_email(recipient, code, minutes, purpose):
    subject = "Verificación de correo - HairCloud" if purpose == "email_verification" else "Recuperación de contraseña - HairCloud"
    message_body = f"""
    Hola,

    Tu código de verificación es: {code}

    Este código expirará en {minutes} minutos.

    Saludos,  
    El equipo de HairCloud
    """

    msg = Message(subject, recipients=[recipient])
    msg.body = message_body
    mail.send(msg)