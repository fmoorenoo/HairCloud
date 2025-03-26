from datetime import datetime
from flask_mail import Message
from app.extensions import mail

def send_verification_email(recipient, code, minutes, purpose):
    if purpose == "email_verification":
        subject = "Verificación de correo - HairCloud"
        title = "Verificación de Correo Electrónico"
    else:
        subject = "Recuperación de contraseña - HairCloud"
        title = "Recuperación de Contraseña"

    # HTML for email with inline styles
    html_message = f"""
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>{title}</title>
    </head>
    <body style="margin: 0; padding: 0; box-sizing: border-box; font-family: Arial, sans-serif; line-height: 1.6; color: #E0E0E0; background-color: #0A0A0A; display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 20px;">
        <div style="background-color: #121212; border-radius: 16px; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.75), 0 10px 15px -3px rgba(0, 0, 0, 0.5); max-width: 500px; width: 100%; height: 70vh; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.1);">
            <div style="background: linear-gradient(135deg, #6fabf0, #3B82F6); color: white; text-align: center; padding: 22px;">
                <h2 style="font-weight: 600; font-size: 1.25rem; letter-spacing: -0.025em;">{title}</h2>
            </div>
            <div style="padding: 30px; text-align: center;">
                <h3 style="margin-bottom: 30px; color: #E0E0E0; font-weight: 400; font-size: 1.25rem;">Hola,</h3>
                <p style="margin-bottom: 30px; color: #E0E0E0; font-weight: 400; font-size: 1.25rem;">Tu código de verificación es:</p>
                <div style="background-color: #0A0A0A; border: 2px dashed #5356ee; color: #6fabf0; padding: 10px; margin: 25px 0; border-radius: 10px; font-size: 33px; font-weight: 600; letter-spacing: 10px; box-shadow: 2px 18px 12px rgba(0, 0, 0, 0.5); text-align: center;">
                    {code}
                </div>
                <p style="color: #888888; font-size: 1.1rem; margin-bottom: 10px; padding-top: 24px;">Código válido por {minutes} minutos</p>
            </div>
            <div style="background-color: rgba(255, 255, 255, 0.05); text-align: center; padding: 20px; font-size: 0.9rem; color: #888888; border-top: 1px solid rgba(255, 255, 255, 0.1); border-bottom: 1px solid rgba(255, 255, 255, 0.1);">
                © {datetime.now().year} HairCloud. Todos los derechos reservados.
            </div>
        </div>
    </body>
    </html>
    """

    msg = Message(subject, recipients=[recipient])
    msg.body = f"""Código de Verificación: {code}
Este código es válido por {minutes} minutos."""
    msg.html = html_message

    mail.send(msg)