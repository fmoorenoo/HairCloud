"""Sondas de salud. No devuelven detalles internos (antes /auth/test_connection
devolvía el texto completo del error de conexión a la BD)."""
from __future__ import annotations

import logging

from flask import Blueprint, jsonify

from ..db import fetch_value
from ..extensions import limiter

bp = Blueprint("health", __name__, url_prefix="/health")
log = logging.getLogger(__name__)


@bp.get("")
@limiter.exempt
def live():
    return jsonify({"status": "ok"}), 200


@bp.get("/ready")
@limiter.limit("30 per minute")
def ready():
    try:
        fetch_value("SELECT 1")
    except Exception:
        log.exception("La base de datos no responde")
        return jsonify({"status": "unavailable"}), 503
    return jsonify({"status": "ok"}), 200
