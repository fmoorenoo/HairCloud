"""Configuración de la aplicación.

Toda la configuración sensible se lee de variables de entorno (o de un fichero
``.env`` en desarrollo). En producción la aplicación se niega a arrancar si
falta algún secreto obligatorio, en lugar de usar valores por defecto
inseguros.
"""
from __future__ import annotations

import os
from datetime import timedelta

from dotenv import load_dotenv

load_dotenv()


def _bool(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _int(name: str, default: int) -> int:
    value = os.getenv(name)
    return int(value) if value not in (None, "") else default


class Config:
    ENV_NAME = "base"
    DEBUG = False
    TESTING = False

    # --- Secretos -----------------------------------------------------------
    SECRET_KEY = os.getenv("SECRET_KEY")
    JWT_SECRET = os.getenv("JWT_SECRET") or os.getenv("SECRET_KEY")
    JWT_ISSUER = os.getenv("JWT_ISSUER", "haircloud-api")
    JWT_AUDIENCE = os.getenv("JWT_AUDIENCE", "haircloud-app")
    ACCESS_TOKEN_TTL = timedelta(minutes=_int("ACCESS_TOKEN_MINUTES", 15))
    REFRESH_TOKEN_TTL = timedelta(days=_int("REFRESH_TOKEN_DAYS", 30))
    VERIFICATION_TOKEN_TTL = timedelta(minutes=_int("VERIFICATION_TOKEN_MINUTES", 15))

    # --- Base de datos ------------------------------------------------------
    DATABASE_URL = os.getenv("DATABASE_URL")
    DB_HOST = os.getenv("DB_HOST", "localhost")
    DB_PORT = os.getenv("DB_PORT", "5432")
    DB_NAME = os.getenv("DB_NAME", "HairCloud")
    DB_USER = os.getenv("DB_USER")
    DB_PASSWORD = os.getenv("DB_PASSWORD")
    DB_POOL_MIN = _int("DB_POOL_MIN", 1)
    DB_POOL_MAX = _int("DB_POOL_MAX", 10)

    # --- Negocio ------------------------------------------------------------
    APP_TIMEZONE = os.getenv("APP_TIMEZONE", "Europe/Madrid")
    CANCELLATION_MIN_MINUTES = _int("CANCELLATION_MIN_MINUTES", 30)
    BOOKING_MAX_DAYS_AHEAD = _int("BOOKING_MAX_DAYS_AHEAD", 90)
    SLOT_STEP_MINUTES = _int("SLOT_STEP_MINUTES", 5)
    MAX_REVIEWS_PER_LOCAL = _int("MAX_REVIEWS_PER_LOCAL", 3)
    MAX_PENDING_PER_CLIENT = _int("MAX_PENDING_PER_CLIENT", 10)
    ACTIVITY_RETENTION_DAYS = _int("ACTIVITY_RETENTION_DAYS", 7)

    # --- Seguridad de cuentas ----------------------------------------------
    VERIFICATION_CODE_MINUTES = _int("VERIFICATION_CODE_MINUTES", 10)
    VERIFICATION_RESEND_SECONDS = _int("VERIFICATION_RESEND_SECONDS", 60)
    VERIFICATION_MAX_ATTEMPTS = _int("VERIFICATION_MAX_ATTEMPTS", 5)
    LOGIN_MAX_FAILURES = _int("LOGIN_MAX_FAILURES", 5)
    LOGIN_LOCK_MINUTES = _int("LOGIN_LOCK_MINUTES", 15)
    BCRYPT_ROUNDS = _int("BCRYPT_ROUNDS", 12)

    # --- Rate limiting ------------------------------------------------------
    RATELIMIT_ENABLED = _bool("RATELIMIT_ENABLED", True)
    RATELIMIT_STORAGE_URI = os.getenv("RATELIMIT_STORAGE_URI", "memory://")
    RATELIMIT_DEFAULT = os.getenv("RATELIMIT_DEFAULT", "300 per minute")
    RATELIMIT_HEADERS_ENABLED = True
    # Detrás de un proxy inverso de confianza, número de saltos a respetar en
    # X-Forwarded-For. 0 = no confiar en cabeceras de proxy.
    TRUSTED_PROXIES = _int("TRUSTED_PROXIES", 0)

    # --- Correo -------------------------------------------------------------
    # "smtp" en producción, "console" en desarrollo, "memory" en tests.
    MAIL_BACKEND = os.getenv("MAIL_BACKEND", "smtp")
    MAIL_SERVER = os.getenv("MAIL_SERVER", "smtp.gmail.com")
    MAIL_PORT = _int("MAIL_PORT", 587)
    MAIL_USE_TLS = _bool("MAIL_USE_TLS", True)
    MAIL_USERNAME = os.getenv("MAIL_USERNAME")
    MAIL_PASSWORD = os.getenv("MAIL_PASSWORD")
    MAIL_SENDER_NAME = os.getenv("MAIL_SENDER_NAME", "Soporte de HairCloud")
    MAIL_SENDER_ADDRESS = os.getenv("MAIL_SENDER_ADDRESS") or os.getenv("MAIL_USERNAME")
    MAIL_ASYNC = _bool("MAIL_ASYNC", True)

    MAX_CONTENT_LENGTH = 64 * 1024  # La API solo recibe JSON pequeño.
    JSON_SORT_KEYS = False

    REQUIRED_SECRETS = ("SECRET_KEY",)

    @classmethod
    def validate(cls) -> None:
        missing = [name for name in cls.REQUIRED_SECRETS if not getattr(cls, name)]
        if missing:
            raise RuntimeError(
                "Faltan variables de entorno obligatorias: " + ", ".join(missing)
            )
        if cls.JWT_SECRET and len(cls.JWT_SECRET) < 32:
            raise RuntimeError("JWT_SECRET/SECRET_KEY debe tener al menos 32 caracteres")
        if not cls.DATABASE_URL and not cls.DB_USER:
            raise RuntimeError("Configura DATABASE_URL o DB_USER/DB_PASSWORD")


class DevelopmentConfig(Config):
    ENV_NAME = "development"
    DEBUG = True
    MAIL_BACKEND = os.getenv("MAIL_BACKEND", "console")
    BCRYPT_ROUNDS = _int("BCRYPT_ROUNDS", 10)


class ProductionConfig(Config):
    ENV_NAME = "production"
    REQUIRED_SECRETS = ("SECRET_KEY", "JWT_SECRET")

    @classmethod
    def validate(cls) -> None:
        super().validate()
        if cls.MAIL_BACKEND == "smtp" and not (cls.MAIL_USERNAME and cls.MAIL_PASSWORD):
            raise RuntimeError("MAIL_USERNAME y MAIL_PASSWORD son obligatorios en producción")


class TestingConfig(Config):
    ENV_NAME = "testing"
    TESTING = True
    SECRET_KEY = os.getenv("TEST_SECRET_KEY", "test-secret-key-que-no-se-usa-en-produccion-0123456789")
    JWT_SECRET = SECRET_KEY
    DATABASE_URL = os.getenv("TEST_DATABASE_URL")
    MAIL_BACKEND = "memory"
    MAIL_ASYNC = False
    BCRYPT_ROUNDS = 4
    RATELIMIT_ENABLED = _bool("TEST_RATELIMIT_ENABLED", False)
    VERIFICATION_RESEND_SECONDS = 0

    @classmethod
    def validate(cls) -> None:
        if not cls.DATABASE_URL:
            raise RuntimeError("Define TEST_DATABASE_URL para ejecutar los tests")


CONFIGS = {
    "development": DevelopmentConfig,
    "production": ProductionConfig,
    "testing": TestingConfig,
}


def get_config(name: str | None):
    name = (name or os.getenv("APP_ENV") or "production").lower()
    if name not in CONFIGS:
        raise RuntimeError(f"Entorno desconocido: {name}")
    return CONFIGS[name]
