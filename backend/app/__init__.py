"""Fábrica de la aplicación HairCloud API."""
from __future__ import annotations

import logging

from flask import Flask, request
from flask.json.provider import DefaultJSONProvider
from werkzeug.middleware.proxy_fix import ProxyFix

from .config import get_config
from .db import init_db
from .errors import register_error_handlers
from .extensions import limiter
from .serializers import jsonable
from .services.mailer import init_mailer


class ApiJSONProvider(DefaultJSONProvider):
    ensure_ascii = False
    sort_keys = False

    @staticmethod
    def default(o):
        try:
            return jsonable(o)
        except TypeError:
            return DefaultJSONProvider.default(o)


def _configure_logging(app: Flask) -> None:
    level = logging.DEBUG if app.debug else logging.INFO
    logging.basicConfig(level=level, format="%(asctime)s %(levelname)s [%(name)s] %(message)s")
    # Werkzeug registra cada petición con su query string: lo bajamos a WARNING
    # fuera de desarrollo para no volcar datos personales en los logs.
    if not app.debug:
        logging.getLogger("werkzeug").setLevel(logging.WARNING)


def _security_headers(app: Flask) -> None:
    @app.after_request
    def add_headers(response):
        response.headers.setdefault("X-Content-Type-Options", "nosniff")
        response.headers.setdefault("X-Frame-Options", "DENY")
        response.headers.setdefault("Referrer-Policy", "no-referrer")
        response.headers.setdefault("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
        response.headers.setdefault("Cross-Origin-Resource-Policy", "same-origin")
        response.headers.setdefault("Cache-Control", "no-store")
        if request.is_secure:
            response.headers.setdefault("Strict-Transport-Security", "max-age=63072000; includeSubDomains")
        response.headers.pop("Server", None)
        return response


def create_app(config_name: str | None = None) -> Flask:
    config_cls = get_config(config_name)
    config_cls.validate()

    app = Flask(__name__)
    app.config.from_object(config_cls)
    app.json = ApiJSONProvider(app)
    app.url_map.strict_slashes = False

    _configure_logging(app)

    if app.config["TRUSTED_PROXIES"]:
        hops = app.config["TRUSTED_PROXIES"]
        app.wsgi_app = ProxyFix(app.wsgi_app, x_for=hops, x_proto=hops, x_host=hops)

    init_db(app)
    init_mailer(app)
    limiter.init_app(app)
    register_error_handlers(app)
    _security_headers(app)

    from .api import register_blueprints
    register_blueprints(app)

    from .cli import register_cli
    register_cli(app)

    return app


__all__ = ["create_app"]
