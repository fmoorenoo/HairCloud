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


def send_booking_info_email(recipient, local_info, barber_name, service_info, hora_inicio, hora_fin, fecha):
    subject = "Confirmación de tu cita - HairCloud"
    title = "Tu cita ha sido confirmada"

    html_message = f"""
    <html>
    <head>
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
            body {{
                font-family: 'Poppins', Arial, sans-serif;
                background-color: #f8fafc;
                margin: 0;
                padding: 0;
                -webkit-font-smoothing: antialiased;
            }}
            .container {{
                max-width: 600px;
                margin: auto;
                background-color: white;
                border-radius: 12px;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
                overflow: hidden;
            }}
            .header {{
                background-color: #3B82F6;
                color: white;
                padding: 30px 25px;
                text-align: center;
            }}
            .header h1 {{
                margin: 0;
                font-weight: 600;
                font-size: 24px;
            }}
            .content {{
                padding: 25px;
            }}
            .local-info {{
                background-color: #f1f5f9;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
            }}
            .local-name {{
                font-size: 18px;
                font-weight: 600;
                color: #0f172a;
                margin-top: 0;
                margin-bottom: 15px;
            }}
            .info-row {{
                display: flex;
                margin-bottom: 12px;
                align-items: center;
            }}
            .info-icon {{
                width: 22px;
                margin-right: 10px;
                opacity: 0.7;
            }}
            .info-text {{
                font-size: 14px;
                color: #334155;
            }}
            .service-box {{
                background-color: #eff6ff;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
            }}
            .service-name {{
                font-size: 16px;
                font-weight: 600;
                color: #1e40af;
                margin-top: 0;
                margin-bottom: 15px;
            }}
            .service-detail {{
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
                font-size: 14px;
            }}
            .service-label {{
                color: #64748b;
            }}
            .service-value {{
                color: #334155;
                font-weight: 500;
            }}
            .appointment-box {{
                background-color: #f0fdf4;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
                border-left: 4px solid #22c55e;
            }}
            .time-row {{
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
            }}
            .time-label {{
                color: #64748b;
                font-size: 14px;
            }}
            .time-value {{
                color: #0f172a;
                font-weight: 500;
                font-size: 14px;
            }}
            .footer {{
                text-align: center;
                padding: 20px;
                background-color: #f8fafc;
                color: #64748b;
                font-size: 13px;
            }}
            .logo {{
                text-align: center;
                margin-bottom: 10px;
            }}
            .divider {{
                height: 1px;
                background-color: #e2e8f0;
                margin: 15px 0;
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>{title}</h1>
            </div>
            <div class="content">
                <div class="local-info">
                    <h2 class="local-name">{local_info['nombre']}</h2>
                    <div class="info-row">
                        <span class="info-icon">📍</span>
                        <span class="info-text">{local_info['direccion']}, {local_info['localidad']}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">📞</span>
                        <span class="info-text">{local_info['telefono']}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-icon">💇</span>
                        <span class="info-text"><strong>Peluquero:</strong> {barber_name}</span>
                    </div>
                </div>

                <div class="service-box">
                    <h3 class="service-name">{service_info['nombre']}</h3>
                    <div class="service-detail">
                        <span class="service-label">Descripción:</span><br>
                        <span class="service-value">{service_info['descripcion']}</span>
                    </div>
                    <div class="divider"></div>
                    <div class="service-detail">
                        <span class="service-label">Duración:</span><br>
                        <span class="service-value">{service_info['duracion']} minutos</span>
                    </div>
                    <div class="divider"></div>
                    <div class="service-detail">
                        <span class="service-label">Precio:</span><br>
                        <span class="service-value">{service_info['precio']}€</span>
                    </div>
                </div>
                
                <div style="text-align: center; margin-bottom: 20px;">
                    <h2 style="color: #1e3a8a; font-size: 20px; font-weight: 600; margin: 0;">📅 {fecha}</h2>
                </div>

                <div class="appointment-box">
                    <div class="time-row">
                        <span class="time-label">Inicio:</span><br>
                        <span class="time-value">{hora_inicio}</span>
                    </div>
                    <div class="divider"></div>
                    <div class="time-row">
                        <span class="time-label">Fin:</span><br>
                        <span class="time-value">{hora_fin}</span>
                    </div>
                </div>
            </div>

            <div class="footer">
                <div class="logo">
                    <strong style="color: #3B82F6;">HairCloud</strong>
                </div>
                <p>Gracias por reservar con nosotros. Si necesitas realizar algún cambio, ponte en contacto con el local.</p>
            </div>
        </div>
    </body>
    </html>
    """

    msg = Message(subject, recipients=[recipient])
    msg.html = html_message
    mail.send(msg)


def send_cancellation_email(recipient, cliente_nombre, fecha, hora_inicio, hora_fin, servicio_nombre, local_info, barber_name, motivo):
    subject = "Cancelación de tu cita - HairCloud"
    title = "Tu cita ha sido cancelada"

    html_message = f"""
    <html>
    <head>
        <style>
            body {{
                font-family: 'Poppins', Arial, sans-serif;
                background-color: #f8fafc;
                margin: 0;
                padding: 0;
            }}
            .container {{
                max-width: 600px;
                margin: auto;
                background-color: white;
                border-radius: 12px;
                overflow: hidden;
                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            }}
            .header {{
                background-color: #ef4444;
                color: white;
                padding: 30px 25px;
                text-align: center;
            }}
            .header h1 {{
                margin: 0;
                font-size: 24px;
                font-weight: 600;
            }}
            .content {{
                padding: 25px;
                color: #1e293b;
            }}
            .content p {{
                font-size: 16px;
                margin-bottom: 15px;
            }}
            .motivo {{
                background-color: #fef2f2;
                border-left: 4px solid #dc2626;
                padding: 15px;
                border-radius: 8px;
                margin-bottom: 20px;
                font-size: 14px;
            }}
            .info-box {{
                background-color: #fef2f2;
                border-left: 4px solid #dc2626;
                padding: 20px;
                border-radius: 8px;
            }}
            .info-row {{
                display: flex;
                justify-content: space-between;
                margin-bottom: 10px;
                font-size: 14px;
            }}
            .footer {{
                text-align: center;
                font-size: 13px;
                padding: 20px;
                color: #64748b;
                background-color: #f8fafc;
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>{title}</h1>
            </div>
            <div class="content">
                <p>Hola {cliente_nombre},</p>
                <p>Tu cita con {barber_name} en {local_info['nombre']} ha sido <strong>cancelada</strong>.</p>
                <div class="motivo">
                    <strong>Motivo:</strong> {motivo}
                </div>
                <div class="info-box">
                    <div class="info-row"><strong>Servicio:</strong> {servicio_nombre}</div>
                    <div class="info-row"><strong>Fecha:</strong> {fecha}</div>
                    <div class="info-row"><strong>Hora inicio:</strong> {hora_inicio}</div>
                    <div class="info-row"><strong>Hora fin:</strong> {hora_fin}</div>
                </div>
                <p style="margin-top: 20px;">Si necesitas más información o deseas reprogramar tu cita, no dudes en contactarnos.</p>
            </div>
            <div class="footer">
                © {datetime.now().year} HairCloud. Gracias por confiar en nosotros.
            </div>
        </div>
    </body>
    </html>
    """

    msg = Message(subject, recipients=[recipient])
    msg.html = html_message
    mail.send(msg)


def send_barber_stats_email(recipient, stats, start_date, end_date):
    subject = "Estadísticas de rendimiento - HairCloud"
    title = "Resumen de tu actividad"

    def format_date(fecha_str):
        dt = datetime.strptime(fecha_str, "%Y-%m-%d")
        meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
                 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre']
        return f"{dt.day} de {meses[dt.month - 1]} de {dt.year}"

    rango_fechas = f"Desde {format_date(start_date)}<br>Hasta {format_date(end_date)}"

    servicio = stats.get('servicio_mas_solicitado')
    cliente = stats.get('cliente_mas_frecuente')

    canceladas = stats['total_canceladas']
    completadas = stats['total_citas']
    no_completadas = stats['total_no_completadas']
    total_citas = canceladas + completadas + no_completadas

    porcentaje_completadas = round((completadas / total_citas) * 100) if total_citas > 0 else 0
    porcentaje_canceladas = round((canceladas / total_citas) * 100) if total_citas > 0 else 0
    porcentaje_no_completadas = round((no_completadas / total_citas) * 100) if total_citas > 0 else 0

    html_message = f"""
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
            body {{
                font-family: 'Poppins', Arial, sans-serif;
                background-color: #f8fafc;
                margin: 0;
                padding: 0;
                -webkit-font-smoothing: antialiased;
            }}
            .container {{
                max-width: 600px;
                margin: auto;
                background-color: white;
                border-radius: 12px;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
                overflow: hidden;
            }}
            .header {{
                background-color: #3B82F6;
                color: white;
                padding: 30px 25px;
                text-align: center;
            }}
            .header h1 {{
                margin: 0;
                font-weight: 600;
                font-size: 24px;
            }}
            .content {{
                padding: 25px;
            }}
            .date-range {{
                text-align: center;
                background-color: #eff6ff;
                color: #1e40af;
                font-weight: 500;
                padding: 10px 20px;
                border-radius: 20px;
                display: inline-block;
                margin: 0 auto 25px;
                font-size: 17px;
                width: fit-content;
            }}
            .date-range-container {{
                text-align: center;
                margin-bottom: 20px;
            }}
            .stat-card {{
                background-color: #f1f5f9;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
                text-align: center;
            }}
            .stat-title {{
                font-size: 18px;
                font-weight: 600;
                color: #0f172a;
                margin-top: 0;
                margin-bottom: 15px;
            }}
            .stat-flex {{
                display: inline-block;
                width: 100%;
                margin-bottom: 10px;
            }}
            .stat-box {{
                display: inline-block;
                vertical-align: top;
                background-color: white;
                border-radius: 8px;
                padding: 10px;
                margin: 5px;
                width: 40%;
                text-align: center;
            }}
            .stat-value {{
                font-size: 24px;
                font-weight: 600;
                color: #3B82F6;
                margin-bottom: 5px;
            }}
            .stat-label {{
                font-size: 14px;
                color: #64748b;
            }}
            .chart-container {{
                background-color: #eff6ff;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
            }}
            .chart-title {{
                font-size: 16px;
                font-weight: 600;
                color: #1e40af;
                margin-top: 0;
                margin-bottom: 15px;
            }}
            .progress-container {{
                margin-bottom: 20px;
            }}
            .progress-label {{
                display: flex;
                justify-content: space-between;
                margin-bottom: 5px;
            }}
            .progress-label-text {{
                font-size: 14px;
                font-weight: 500;
            }}
            .progress-label-value {{
                font-size: 14px;
                font-weight: 600;
            }}
            .progress-bar {{
                height: 24px;
                background-color: #e2e8f0;
                border-radius: 6px;
                overflow: hidden;
            }}
            .progress-fill-completed {{
                height: 100%;
                background-color: #22c55e;
                width: {porcentaje_completadas}%;
            }}
            .progress-fill-canceled {{
                height: 100%;
                background-color: #ef4444;
                width: {porcentaje_canceladas}%;
            }}
            .progress-fill-pending {{
                height: 100%;
                background-color: #eab308;
                width: {porcentaje_no_completadas}%;
            }}
            .highlights-box {{
                background-color: #f0fdf4;
                border-radius: 8px;
                padding: 20px;
                margin-bottom: 25px;
                border-left: 4px solid #22c55e;
            }}
            .highlight-row {{
                display: block;
                margin-bottom: 15px;
            }}
            .highlight-item {{
                display: inline-block;
                vertical-align: top;
                width: 45%;
            }}
            .highlight-label {{
                color: #64748b;
                font-size: 14px;
                margin-bottom: 5px;
            }}
            .highlight-value {{
                color: #0f172a;
                font-weight: 600;
                font-size: 16px;
            }}
            .highlight-sub {{
                color: #64748b;
                font-size: 12px;
                margin-top: 3px;
            }}
            .divider {{
                display: inline-block;
                vertical-align: top;
                height: 50px;
                width: 1px;
                background-color: #e2e8f0;
                margin: 0 10px;
            }}
            .footer {{
                text-align: center;
                padding: 20px;
                background-color: #f8fafc;
                color: #64748b;
                font-size: 13px;
            }}
            .logo {{
                text-align: center;
                margin-bottom: 10px;
            }}

            /* Estilos específicos para móviles */
            @media only screen and (max-width: 480px) {{
                .container {{
                    width: 100% !important;
                    max-width: 100% !important;
                }}
                .stat-box {{
                    width: 85% !important;
                    margin: 5px auto !important;
                    display: block !important;
                }}
                .highlight-item {{
                    width: 100% !important;
                    display: block !important;
                    margin-bottom: 15px !important;
                }}
                .divider {{
                    display: none !important;
                }}
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>{title}</h1>
            </div>
            <div class="content">
                <div class="date-range-container">
                    <div class="date-range">
                        {rango_fechas}
                    </div>
                </div>

                <div class="stat-card">
                    <h2 class="stat-title">Resumen general</h2>
                    <div class="stat-flex">
                        <div class="stat-box">
                            <div class="stat-value">{stats['total_clientes_atendidos']}</div>
                            <div class="stat-label">Clientes atendidos</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-value">{stats['total_citas']}</div>
                            <div class="stat-label">Citas completadas</div>
                        </div>
                    </div>
                    <div class="stat-flex">
                        <div class="stat-box">
                            <div class="stat-value">{stats['ingresos_totales']}€</div>
                            <div class="stat-label">Ingresos</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-value">{stats['promedio_citas_por_dia']}</div>
                            <div class="stat-label">Citas / dia</div>
                        </div>
                    </div>
                    <div class="stat-flex">
                        <div class="stat-box">
                            <div class="stat-value">{stats['total_canceladas']}</div>
                            <div class="stat-label">Canceladas</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-value">{stats['total_no_completadas']}</div>
                            <div class="stat-label">No completadas</div>
                        </div>
                    </div>
                </div>

                <div class="chart-container">
                    <h3 class="chart-title">Estado de citas</h3>

                    <!-- Nueva estructura de gráfico de barras -->
                    <div class="progress-container">
                        <div class="progress-label">
                            <span class="progress-label-text">Completadas</span>
                            <span class="progress-label-value">{completadas} ({porcentaje_completadas}%)</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill-completed"></div>
                        </div>
                    </div>

                    <div class="progress-container">
                        <div class="progress-label">
                            <span class="progress-label-text">Canceladas</span>
                            <span class="progress-label-value">{canceladas} ({porcentaje_canceladas}%)</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill-canceled"></div>
                        </div>
                    </div>

                    <div class="progress-container">
                        <div class="progress-label">
                            <span class="progress-label-text">No completadas</span>
                            <span class="progress-label-value">{no_completadas} ({porcentaje_no_completadas}%)</span>
                        </div>
                        <div class="progress-bar">
                            <div class="progress-fill-pending"></div>
                        </div>
                    </div>
                </div>

                <div class="highlights-box">
                    <h3 class="chart-title">Datos destacados</h3>
                    <div class="highlight-row">
                        <div class="highlight-item">
                            <div class="highlight-label">Servicio más solicitado</div>
                            <div class="highlight-value">{servicio['nombre'] if servicio else 'N/A'}</div>
                            <div class="highlight-sub">{servicio['cantidad'] if servicio else 0} veces</div>
                        </div>

                        <div class="divider"></div>

                        <div class="highlight-item">
                            <div class="highlight-label">Cliente más frecuente</div>
                            <div class="highlight-value">{cliente['nombre'] if cliente else 'N/A'}</div>
                            <div class="highlight-sub">{cliente['total_citas'] if cliente else 0} visitas</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="footer">
                <div class="logo">
                    <strong style="color: #3B82F6;">HairCloud</strong>
                </div>
                <p>© {datetime.now().year} HairCloud. Todos los derechos reservados.</p>
            </div>
        </div>
    </body>
    </html>
    """

    msg = Message(subject, recipients=[recipient])
    msg.html = html_message
    mail.send(msg)