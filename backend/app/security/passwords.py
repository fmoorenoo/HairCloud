"""Hash y verificación de contraseñas con bcrypt.

Compatible con los hashes ``$2b$`` generados por Flask-Bcrypt en la versión
anterior, de modo que los usuarios existentes pueden seguir iniciando sesión.
"""
from __future__ import annotations

import bcrypt
from flask import current_app

# Hash de una contraseña aleatoria: se usa para gastar el mismo tiempo cuando
# el usuario no existe y no revelar así qué nombres de usuario son válidos.
_DUMMY_HASH = bcrypt.hashpw(b"haircloud-timing-equalizer", bcrypt.gensalt(rounds=10))


def hash_password(password: str) -> str:
    rounds = current_app.config["BCRYPT_ROUNDS"]
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(rounds=rounds)).decode("ascii")


def verify_password(password: str, password_hash: str | None) -> bool:
    candidate = password.encode("utf-8")
    if len(candidate) > 72:
        # bcrypt >= 4.1 lanza error con más de 72 bytes; ninguna contraseña
        # válida puede superarlo, así que se trata como incorrecta.
        bcrypt.checkpw(b"x", _DUMMY_HASH)
        return False
    if not password_hash:
        bcrypt.checkpw(candidate, _DUMMY_HASH)
        return False
    try:
        return bcrypt.checkpw(candidate, password_hash.encode("ascii"))
    except ValueError:
        return False


def needs_rehash(password_hash: str) -> bool:
    try:
        rounds = int(password_hash.split("$")[2])
    except (IndexError, ValueError):
        return True
    return rounds < current_app.config["BCRYPT_ROUNDS"]
