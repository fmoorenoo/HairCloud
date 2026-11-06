"""Registro de blueprints bajo el prefijo versionado ``/api/v1``."""
from __future__ import annotations

from flask import Blueprint, Flask

API_PREFIX = "/api/v1"


def register_blueprints(app: Flask) -> None:
    from . import appointments, auth, barbers, barbershops, health, me, reviews, services

    api = Blueprint("api_v1", __name__, url_prefix=API_PREFIX)
    for module in (auth, me, barbershops, services, reviews, barbers, appointments):
        api.register_blueprint(module.bp)
    app.register_blueprint(api)
    app.register_blueprint(health.bp)
