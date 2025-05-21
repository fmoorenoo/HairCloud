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

    rango_fechas = f"{format_date(start_date)} - {format_date(end_date)}"

    # Colores principales
    primary_color = "#3B82F6"
    secondary_color = "#1E40AF"
    accent_color = "#FBBF24"
    dark_bg = "#1f2937"
    light_bg = "#F9FAFB"
    text_dark = "#111827"
    text_light = "#F3F4F6"
    text_muted = "#6B7280"

    servicio = stats.get('servicio_mas_solicitado')
    cliente = stats.get('cliente_mas_frecuente')

    # Crear gráfico SVG simple para mostrar la relación entre citas completadas y canceladas
    total_citas = stats['total_citas']
    canceladas = stats['total_canceladas']
    completadas = total_citas - canceladas - stats['total_no_completadas']

    porcentaje_completadas = round((completadas / total_citas) * 100) if total_citas > 0 else 0
    porcentaje_canceladas = round((canceladas / total_citas) * 100) if total_citas > 0 else 0
    porcentaje_no_completadas = 100 - porcentaje_completadas - porcentaje_canceladas

    html_message = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>{subject}</title>
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <style>
            body {{
                font-family: 'Poppins', Arial, sans-serif;
                background: #f1f5f9;
                margin: 0;
                padding: 0;
                color: {text_dark};
                line-height: 1.6;
            }}
            .container {{
                max-width: 650px;
                margin: 30px auto;
                background: white;
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
            }}
            .header {{
                background: linear-gradient(135deg, {primary_color}, {secondary_color});
                color: {text_light};
                padding: 30px 25px;
                text-align: center;
                position: relative;
                overflow: hidden;
            }}
            .header h2 {{
                margin: 0;
                font-size: 28px;
                font-weight: 600;
                letter-spacing: 0.5px;
            }}
            .header p {{
                margin-top: 10px;
                font-size: 16px;
                opacity: 0.9;
            }}
            .header::before {{
                content: '';
                position: absolute;
                top: -50%;
                left: -50%;
                width: 200%;
                height: 200%;
                background: repeating-linear-gradient(
                    45deg,
                    rgba(255,255,255,0.1),
                    rgba(255,255,255,0.1) 10px,
                    transparent 10px,
                    transparent 20px
                );
                animation: slide 20s linear infinite;
                opacity: 0.3;
            }}
            @keyframes slide {{
                from {{ transform: translateX(0) translateY(0); }}
                to {{ transform: translateX(-50px) translateY(-50px); }}
            }}
            .logo {{
                display: inline-block;
                margin-bottom: 15px;
                font-size: 24px;
                font-weight: 700;
                color: {text_light};
                text-transform: uppercase;
                letter-spacing: 1px;
            }}
            .logo span {{
                color: {accent_color};
            }}
            .date-range {{
                display: inline-block;
                background: rgba(255,255,255,0.2);
                padding: 8px 15px;
                border-radius: 30px;
                font-size: 15px;
                margin-top: 10px;
            }}
            .stats-container {{
                padding: 30px;
                display: flex;
                flex-wrap: wrap;
                justify-content: space-between;
            }}
            .stat-card {{
                background: {light_bg};
                border-radius: 12px;
                padding: 20px;
                width: calc(33.33% - 15px);
                margin-bottom: 20px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.03);
                transition: transform 0.2s, box-shadow 0.2s;
                border: 1px solid rgba(0,0,0,0.05);
                text-align: center;
                position: relative;
                overflow: hidden;
            }}
            .stat-card:hover {{
                transform: translateY(-3px);
                box-shadow: 0 6px 12px rgba(0,0,0,0.08);
            }}
            .stat-label {{
                font-size: 14px;
                color: {text_muted};
                margin-bottom: 8px;
                font-weight: 500;
            }}
            .stat-value {{
                font-size: 24px;
                font-weight: 700;
                color: {text_dark};
            }}
            .highlight-card {{
                background: linear-gradient(135deg, {primary_color}, {secondary_color});
                color: {text_light};
            }}
            .highlight-card .stat-label {{
                color: rgba(255,255,255,0.8);
            }}
            .highlight-card .stat-value {{
                color: {text_light};
            }}
            .highlight-card::after {{
                content: '';
                position: absolute;
                top: 0;
                right: 0;
                border-width: 0 20px 20px 0;
                border-style: solid;
                border-color: transparent {accent_color} transparent transparent;
            }}
            .details-section {{
                padding: 0 30px 30px 30px;
            }}
            .section-title {{
                color: {secondary_color};
                font-size: 18px;
                font-weight: 600;
                margin: 20px 0 15px 0;
                display: flex;
                align-items: center;
                padding-bottom: 8px;
                border-bottom: 2px solid rgba(0,0,0,0.05);
            }}
            .section-title svg {{
                margin-right: 10px;
            }}
            .detail-card {{
                background: {light_bg};
                border-radius: 10px;
                padding: 20px;
                margin-bottom: 20px;
                border-left: 4px solid {primary_color};
            }}
            .detail-name {{
                font-weight: 600;
                font-size: 17px;
                margin-bottom: 5px;
            }}
            .detail-value {{
                color: {text_muted};
                font-size: 15px;
            }}
            .chart-container {{
                margin: 25px 0;
                padding: 20px;
                background: {light_bg};
                border-radius: 10px;
                text-align: center;
            }}
            .chart-title {{
                margin-bottom: 15px;
                font-weight: 500;
                color: {text_dark};
            }}
            .bar-chart {{
                display: flex;
                height: 25px;
                border-radius: 6px;
                overflow: hidden;
                margin-bottom: 10px;
            }}
            .bar-segment {{
                height: 100%;
                text-align: center;
                color: white;
                font-size: 12px;
                font-weight: 500;
                display: flex;
                align-items: center;
                justify-content: center;
            }}
            .chart-legend {{
                display: flex;
                justify-content: center;
                flex-wrap: wrap;
                margin-top: 10px;
            }}
            .legend-item {{
                display: flex;
                align-items: center;
                margin: 0 10px;
                font-size: 13px;
            }}
            .legend-color {{
                width: 12px;
                height: 12px;
                border-radius: 3px;
                margin-right: 5px;
            }}
            .footer {{
                background: {dark_bg};
                padding: 20px;
                text-align: center;
                color: rgba(255,255,255,0.7);
                font-size: 14px;
            }}
            .signature {{
                margin-top: 5px;
                color: {accent_color};
                font-weight: 600;
            }}
            @media (max-width: 600px) {{
                .stat-card {{
                    width: 100%;
                }}
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo">Hair<span>Cloud</span></div>
                <h2>{title}</h2>
                <div class="date-range">📅 {rango_fechas}</div>
            </div>

            <div class="stats-container">
                <div class="stat-card highlight-card">
                    <div class="stat-label">Ingresos totales</div>
                    <div class="stat-value">{stats['ingresos_totales']} €</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Citas completadas</div>
                    <div class="stat-value">{stats['total_citas']}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Clientes atendidos</div>
                    <div class="stat-value">{stats['total_clientes_atendidos']}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Promedio citas/día</div>
                    <div class="stat-value">{stats['promedio_citas_por_dia']}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Citas canceladas</div>
                    <div class="stat-value">{stats['total_canceladas']}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">No completadas</div>
                    <div class="stat-value">{stats['total_no_completadas']}</div>
                </div>
            </div>

            <div class="chart-container">
                <div class="chart-title">Distribución de citas</div>
                <div class="bar-chart">
                    <div class="bar-segment" style="width: {porcentaje_completadas}%; background-color: #10B981;">
                        {porcentaje_completadas}%
                    </div>
                    <div class="bar-segment" style="width: {porcentaje_canceladas}%; background-color: #EF4444;">
                        {porcentaje_canceladas}%
                    </div>
                    <div class="bar-segment" style="width: {porcentaje_no_completadas}%; background-color: #F59E0B;">
                        {porcentaje_no_completadas}%
                    </div>
                </div>
                <div class="chart-legend">
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #10B981;"></div>
                        <span>Completadas</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #EF4444;"></div>
                        <span>Canceladas</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #F59E0B;"></div>
                        <span>No completadas</span>
                    </div>
                </div>
            </div>

            <div class="details-section">
                <div class="section-title">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 16L12 8M12 8L8 12M12 8L16 12" stroke="{secondary_color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <circle cx="12" cy="12" r="10" stroke="{secondary_color}" stroke-width="2"/>
                    </svg>
                    Servicio más solicitado
                </div>
                <div class="detail-card">
                    <div class="detail-name">{servicio['nombre']}</div>
                    <div class="detail-value">Solicitado {servicio['cantidad']} veces</div>
                </div>

                <div class="section-title">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M17 21V19C17 16.7909 15.2091 15 13 15H5C2.79086 15 1 16.7909 1 19V21" stroke="{secondary_color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <circle cx="9" cy="7" r="4" stroke="{secondary_color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M23 21V19C22.9986 17.1771 21.765 15.5857 20 15.13" stroke="{secondary_color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M16 3.13C17.7699 3.58317 19.0078 5.17799 19.0078 7.005C19.0078 8.83201 17.7699 10.4268 16 10.88" stroke="{secondary_color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Cliente más frecuente
                </div>
                <div class="detail-card">
                    <div class="detail-name">{cliente['nombre']}</div>
                    <div class="detail-value">{cliente['total_citas']} citas durante este período</div>
                </div>
            </div>

            <div class="footer">
                <div class="signature">HairCloud - Creciendo juntos</div>
            </div>
        </div>
    </body>
    </html>
    """

    msg = Message(subject, recipients=[recipient])
    msg.html = html_message
    mail.send(msg)