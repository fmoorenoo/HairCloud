"""Punto de entrada WSGI.

Producción (Windows o Linux):
    waitress-serve --listen=0.0.0.0:5000 wsgi:app
CLI:
    flask --app wsgi db-upgrade
"""
from app import create_app

app = create_app()
