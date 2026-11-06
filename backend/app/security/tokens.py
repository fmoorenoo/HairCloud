"""Emisión y validación de tokens.

* **Access token**: JWT HS256 de vida corta (15 min por defecto) con emisor,
  audiencia, ``jti`` y la versión de credenciales del usuario (``ver``).
  Cambiar la contraseña o desactivar a un empleado incrementa la versión e
  invalida al instante todos sus access tokens.
* **Refresh token**: valor opaco aleatorio (256 bits) del que solo se guarda
  el SHA-256. Se rota en cada uso; si se presenta un refresh token ya rotado
  se asume robo y se revoca toda la familia de sesiones.
"""
from __future__ import annotations

import hashlib
import hmac
import secrets
import uuid
from datetime import datetime, timedelta, timezone

import jwt
from flask import current_app, request

from ..db import transaction
from ..errors import Unauthorized
from ..timeutils import now_local


def sha256(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def hmac_digest(value: str) -> str:
    key = current_app.config["SECRET_KEY"].encode("utf-8")
    return hmac.new(key, value.encode("utf-8"), hashlib.sha256).hexdigest()


def constant_time_equals(a: str, b: str) -> bool:
    return hmac.compare_digest(a.encode("utf-8"), b.encode("utf-8"))


# --------------------------------------------------------------------------
# Access tokens
# --------------------------------------------------------------------------
def issue_access_token(user: dict) -> tuple[str, int]:
    cfg = current_app.config
    now = datetime.now(timezone.utc)
    ttl: timedelta = cfg["ACCESS_TOKEN_TTL"]
    payload = {
        "sub": str(user["usuarioid"]),
        "rol": user["rol"],
        "ver": user["token_version"],
        "typ": "access",
        "iss": cfg["JWT_ISSUER"],
        "aud": cfg["JWT_AUDIENCE"],
        "iat": now,
        "nbf": now,
        "exp": now + ttl,
        "jti": uuid.uuid4().hex,
    }
    token = jwt.encode(payload, cfg["JWT_SECRET"], algorithm="HS256")
    return token, int(ttl.total_seconds())


def decode_access_token(token: str) -> dict:
    cfg = current_app.config
    try:
        payload = jwt.decode(
            token,
            cfg["JWT_SECRET"],
            algorithms=["HS256"],  # lista cerrada: impide ataques "alg=none"
            audience=cfg["JWT_AUDIENCE"],
            issuer=cfg["JWT_ISSUER"],
            options={"require": ["exp", "iat", "sub", "ver", "typ"]},
            leeway=10,
        )
    except jwt.ExpiredSignatureError:
        raise Unauthorized("La sesión ha caducado", code="token_expired")
    except jwt.PyJWTError:
        raise Unauthorized("Token no válido", code="invalid_token")
    if payload.get("typ") != "access":
        raise Unauthorized("Token no válido", code="invalid_token")
    return payload


# --------------------------------------------------------------------------
# Refresh tokens
# --------------------------------------------------------------------------
def _client_fingerprint() -> str | None:
    agent = request.headers.get("User-Agent")
    return agent[:200] if agent else None


def issue_refresh_token(cur, usuarioid: int, family_id: str | None = None) -> str:
    token = secrets.token_urlsafe(32)
    now = now_local()
    cur.execute(
        """
        INSERT INTO refresh_tokens (usuarioid, token_hash, family_id, expires_at, created_at, user_agent)
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (usuarioid, sha256(token), family_id or uuid.uuid4().hex,
         now + current_app.config["REFRESH_TOKEN_TTL"], now, _client_fingerprint()),
    )
    return token


def rotate_refresh_token(raw_token: str) -> tuple[int, str]:
    """Valida ``raw_token``, lo revoca y emite uno nuevo de la misma familia.

    Devuelve ``(usuarioid, nuevo_refresh_token)``.
    """
    from .audit import audit

    token_hash = sha256(raw_token)
    now = now_local()
    reused = False
    with transaction() as cur:
        cur.execute(
            """
            SELECT id, usuarioid, family_id, expires_at, revoked_at
            FROM refresh_tokens WHERE token_hash = %s
            FOR UPDATE
            """,
            (token_hash,),
        )
        row = cur.fetchone()
        if row is None:
            raise Unauthorized("Sesión no válida", code="invalid_refresh_token")
        if row["revoked_at"] is not None:
            # Reutilización de un token ya rotado: posible robo. Se cierran
            # todas las sesiones de esa familia. La revocación debe
            # confirmarse (COMMIT) antes de responder con el error.
            cur.execute(
                "UPDATE refresh_tokens SET revoked_at = %s WHERE family_id = %s AND revoked_at IS NULL",
                (now, row["family_id"]),
            )
            audit("refresh_token_reuse", usuarioid=row["usuarioid"], cur=cur)
            reused = True
        elif row["expires_at"] <= now:
            raise Unauthorized("La sesión ha caducado", code="refresh_token_expired")
        else:
            new_token = issue_refresh_token(cur, row["usuarioid"], row["family_id"])
            cur.execute(
                "UPDATE refresh_tokens SET revoked_at = %s, replaced_by = %s WHERE id = %s",
                (now, sha256(new_token), row["id"]),
            )
    if reused:
        raise Unauthorized("Sesión revocada. Inicia sesión de nuevo.", code="refresh_token_reused")
    return row["usuarioid"], new_token


def revoke_refresh_token(raw_token: str) -> None:
    with transaction() as cur:
        cur.execute(
            """
            UPDATE refresh_tokens SET revoked_at = %s
            WHERE family_id = (SELECT family_id FROM refresh_tokens WHERE token_hash = %s)
              AND revoked_at IS NULL
            """,
            (now_local(), sha256(raw_token)),
        )


def revoke_all_sessions(cur, usuarioid: int) -> None:
    """Invalida access y refresh tokens de un usuario (cambio de credenciales)."""
    cur.execute("UPDATE usuarios SET token_version = token_version + 1 WHERE usuarioid = %s",
                (usuarioid,))
    cur.execute("UPDATE refresh_tokens SET revoked_at = %s WHERE usuarioid = %s AND revoked_at IS NULL",
                (now_local(), usuarioid))
