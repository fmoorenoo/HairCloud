"""Servidor de desarrollo.

    python app.py              -> escucha solo en 127.0.0.1 (emulador Android vía 10.0.2.2)
    python app.py --lan        -> escucha en la red local (dispositivo físico)

No usar en producción: ver ``wsgi.py``.
"""
import os
import sys

from app import create_app

if __name__ == "__main__":
    os.environ.setdefault("APP_ENV", "development")
    application = create_app()
    host = "0.0.0.0" if "--lan" in sys.argv else "127.0.0.1"
    application.run(host=host, port=int(os.getenv("PORT", "5000")), debug=application.debug)
