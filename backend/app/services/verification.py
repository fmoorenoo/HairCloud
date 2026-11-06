"""Códigos de verificación por email (alta de cuenta y recuperación de contraseña).

Mejoras frente a la versión anterior:

* El código se genera con ``secrets`` (CSPRNG) en lugar de ``random``.
* En BD solo se guarda un HMAC del código, nunca el código en claro.
* Máximo de intentos por código: tras N fallos el código queda invalidado
  (antes un código de 6 dígitos se podía probar por fuerza bruta sin límite).
* Verificar el código devuelve un *token de verificación* de un solo uso y
  vida corta. El registro y el cambio de contraseña exigen ese token, de modo
  que ya no es posible registrarse sin verificar el correo ni reutilizar un
  código para cambiar la contraseña varias veces.
* Las respuestas no revelan si un email está registrado (antes devolvía
  "No existe ninguna cuenta con ese email" e incluso el nombre de usuario).
"""
from __future__ import annotations

import secrets
from datetime import timedelta

from flask import current_app

from ..db import transaction
from ..errors import BadRequest, TooManyRequests
from ..security.tokens import constant_time_equals, hmac_digest, sha256
from ..timeutils import now_local
from .mailer import send_mail

EMAIL_VERIFICATION = "email_verification"
PASSWORD_RESET = "password_reset"

INVALID_CODE_MSG = "Código incorrecto o caducado"


def _code_digest(email: str, purpose: str, code: str) -> str:
    return hmac_digest(f"{purpose}:{email}:{code}")


def _new_code() -> str:
    return f"{secrets.randbelow(1_000_000):06d}"


def request_code(email: str, purpose: str) -> None:
    """Genera y envía un código. Responde igual exista o no la cuenta."""
    cfg = current_app.config
    now = now_local()
    minutes = cfg["VERIFICATION_CODE_MINUTES"]
    resend_after = timedelta(seconds=cfg["VERIFICATION_RESEND_SECONDS"])

    mail_to_send = None
    with transaction() as cur:
        cur.execute("SELECT usuarioid, nombreusuario FROM usuarios WHERE lower(email) = %s", (email,))
        account = cur.fetchone()

        cur.execute(
            "SELECT created_at FROM codigos_verificacion WHERE email = %s AND tipo = %s FOR UPDATE",
            (email, purpose),
        )
        existing = cur.fetchone()
        if existing and existing["created_at"] and existing["created_at"] + resend_after > now:
            remaining = int((existing["created_at"] + resend_after - now).total_seconds()) + 1
            raise TooManyRequests(
                f"Espera {remaining} segundos antes de solicitar otro código",
                code="code_resend_wait",
                headers={"Retry-After": str(remaining)},
            )

        if purpose == EMAIL_VERIFICATION and account is not None:
            # No revelamos que el email existe: avisamos al titular por correo.
            mail_to_send = ("account_exists", "Ya tienes una cuenta en HairCloud", {})
            code = None
        elif purpose == PASSWORD_RESET and account is None:
            code = None
        else:
            code = _new_code()

        # Se registra la solicitud incluso sin código para aplicar el mismo
        # límite de reenvío (si no, el tiempo de respuesta delataría la cuenta).
        cur.execute(
            """
            INSERT INTO codigos_verificacion (email, tipo, codigo_hash, expiracion, intentos, created_at,
                                              token_hash, token_expira)
            VALUES (%s, %s, %s, %s, 0, %s, NULL, NULL)
            ON CONFLICT (email, tipo) DO UPDATE
               SET codigo_hash = EXCLUDED.codigo_hash, expiracion = EXCLUDED.expiracion,
                   intentos = 0, created_at = EXCLUDED.created_at,
                   token_hash = NULL, token_expira = NULL
            """,
            (email, purpose, _code_digest(email, purpose, code) if code else None,
             now + timedelta(minutes=minutes), now),
        )

    if code is not None:
        if purpose == EMAIL_VERIFICATION:
            title = "Verificación de Correo Electrónico"
            subject = "Verificación de correo - HairCloud"
            intro = "Tu código para verificar el correo y completar el registro es:"
        else:
            title = "Recuperación de Contraseña"
            subject = "Recuperación de contraseña - HairCloud"
            intro = "Tu código para restablecer la contraseña es:"
        send_mail(email, subject, "verification_code", title=title, intro=intro, code=code,
                  minutes=minutes)
    elif mail_to_send:
        template, subject, ctx = mail_to_send
        send_mail(email, subject, template, title=subject, **ctx)


def verify_code(email: str, purpose: str, code: str) -> str:
    """Comprueba el código y devuelve un token de verificación de un solo uso."""
    cfg = current_app.config
    now = now_local()
    max_attempts = cfg["VERIFICATION_MAX_ATTEMPTS"]
    token = None

    with transaction() as cur:
        cur.execute(
            """
            SELECT id, codigo_hash, expiracion, intentos
            FROM codigos_verificacion WHERE email = %s AND tipo = %s FOR UPDATE
            """,
            (email, purpose),
        )
        row = cur.fetchone()
        if row is None or row["codigo_hash"] is None or row["expiracion"] <= now:
            invalid = True
        elif row["intentos"] >= max_attempts:
            invalid = True
        elif constant_time_equals(row["codigo_hash"], _code_digest(email, purpose, code)):
            invalid = False
            token = secrets.token_urlsafe(32)
            cur.execute(
                """
                UPDATE codigos_verificacion
                   SET codigo_hash = NULL, token_hash = %s, token_expira = %s
                 WHERE id = %s
                """,
                (sha256(token), now + cfg["VERIFICATION_TOKEN_TTL"], row["id"]),
            )
        else:
            invalid = True
            attempts = row["intentos"] + 1
            if attempts >= max_attempts:
                # Código quemado: hay que pedir uno nuevo.
                cur.execute("UPDATE codigos_verificacion SET intentos = %s, codigo_hash = NULL WHERE id = %s",
                            (attempts, row["id"]))
            else:
                cur.execute("UPDATE codigos_verificacion SET intentos = %s WHERE id = %s",
                            (attempts, row["id"]))

    if invalid:
        raise BadRequest(INVALID_CODE_MSG, code="invalid_code")
    return token


def consume_token(cur, token: str, purpose: str, email: str | None = None) -> str:
    """Valida y elimina un token de verificación. Devuelve el email asociado."""
    cur.execute(
        """
        DELETE FROM codigos_verificacion
         WHERE token_hash = %s AND tipo = %s AND token_expira > %s
        RETURNING email
        """,
        (sha256(token), purpose, now_local()),
    )
    row = cur.fetchone()
    if row is None or (email is not None and row["email"] != email):
        raise BadRequest("La verificación ha caducado. Solicita un nuevo código.",
                         code="invalid_verification_token")
    return row["email"]
