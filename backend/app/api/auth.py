"""Autenticación: login, refresco, logout, registro y recuperación de contraseña."""
from __future__ import annotations

from datetime import timedelta

from flask import Blueprint, current_app, jsonify, request

from ..db import transaction
from ..errors import Conflict, Forbidden, TooManyRequests, Unauthorized
from ..extensions import limiter
from ..schemas import (ChangePasswordIn, LoginIn, RefreshIn, RegisterIn, ResetPasswordIn,
                       SendCodeIn, VerifyCodeIn, parse)
from ..security.audit import audit
from ..security.auth import STAFF_ROLES, current_user, load_user, require_auth
from ..security.passwords import hash_password, needs_rehash, verify_password
from ..security.tokens import (issue_access_token, issue_refresh_token, revoke_all_sessions,
                               revoke_refresh_token, rotate_refresh_token)
from ..services import verification
from ..services.mailer import send_mail
from ..timeutils import now_local

bp = Blueprint("auth", __name__, url_prefix="/auth")

INVALID_CREDENTIALS = "Usuario o contraseña incorrectos"


def _username_key() -> str:
    data = request.get_json(silent=True) or {}
    name = str(data.get("nombreusuario", ""))[:100].strip().lower()
    return f"login:{name}"


def _session_payload(user, refresh_token: str) -> dict:
    access_token, expires_in = issue_access_token(user)
    return {
        "message": "Login realizado correctamente",
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "Bearer",
        "expires_in": expires_in,
        "usuarioid": user["usuarioid"],
        "nombreusuario": user["nombreusuario"],
        "rol": user["rol"],
    }


@bp.post("/login")
@limiter.limit("10 per minute")
@limiter.limit("20 per 15 minutes", key_func=_username_key)
def login():
    data = parse(LoginIn)
    cfg = current_app.config
    now = now_local()

    with transaction() as cur:
        cur.execute(
            """
            SELECT u.usuarioid, u.nombreusuario, u.password_hash, u.rol, u.token_version,
                   u.failed_logins, u.locked_until, COALESCE(p.activo, TRUE) AS activo
            FROM usuarios u LEFT JOIN peluqueros p ON p.usuarioid = u.usuarioid
            WHERE lower(u.nombreusuario) = lower(%s) OR lower(u.email) = lower(%s)
            FOR UPDATE OF u
            """,
            (data.nombreusuario, data.nombreusuario),
        )
        user = cur.fetchone()

        if user and user["locked_until"] and user["locked_until"] > now:
            locked = True
        else:
            locked = False

        # Siempre se ejecuta bcrypt, exista o no el usuario, para que el tiempo
        # de respuesta no revele qué cuentas existen.
        ok = verify_password(data.password, user["password_hash"] if user else None)

        if user is None:
            outcome = "unknown_user"
        elif locked:
            outcome = "locked"
        elif not ok:
            failures = user["failed_logins"] + 1
            lock_until = None
            if failures >= cfg["LOGIN_MAX_FAILURES"]:
                lock_until = now + timedelta(minutes=cfg["LOGIN_LOCK_MINUTES"])
                failures = 0
            cur.execute("UPDATE usuarios SET failed_logins = %s, locked_until = %s WHERE usuarioid = %s",
                        (failures, lock_until, user["usuarioid"]))
            outcome = "locked_now" if lock_until else "bad_password"
        elif user["rol"] in STAFF_ROLES and not user["activo"]:
            outcome = "inactive"
        else:
            outcome = "ok"
            new_hash = hash_password(data.password) if needs_rehash(user["password_hash"]) else None
            cur.execute(
                """
                UPDATE usuarios SET failed_logins = 0, locked_until = NULL, last_login_at = %s,
                       password_hash = COALESCE(%s, password_hash)
                WHERE usuarioid = %s
                """,
                (now, new_hash, user["usuarioid"]),
            )
            refresh_token = issue_refresh_token(cur, user["usuarioid"])

    uid = user["usuarioid"] if user else None
    if outcome == "ok":
        audit("login_ok", usuarioid=uid)
        return jsonify(_session_payload(user, refresh_token)), 200

    audit(f"login_{outcome}", usuarioid=uid, detalles={"usuario": data.nombreusuario[:50]})
    if outcome in ("locked", "locked_now"):
        raise TooManyRequests(
            f"Demasiados intentos fallidos. Inténtalo de nuevo en {cfg['LOGIN_LOCK_MINUTES']} minutos.",
            code="account_locked")
    if outcome == "inactive":
        # Solo se informa tras verificar la contraseña correcta.
        raise Forbidden("Actualmente no formas parte del personal activo", code="account_inactive")
    raise Unauthorized(INVALID_CREDENTIALS, code="invalid_credentials")


@bp.post("/refresh")
@limiter.limit("30 per minute")
def refresh():
    data = parse(RefreshIn)
    usuarioid, new_refresh = rotate_refresh_token(data.refresh_token)
    user = load_user(usuarioid)
    if user is None or (user.is_staff and not user.activo):
        revoke_refresh_token(new_refresh)
        raise Unauthorized("La sesión ya no es válida", code="token_revoked")
    payload = _session_payload(user.__dict__, new_refresh)
    payload["message"] = "Sesión renovada"
    return jsonify(payload), 200


@bp.post("/logout")
@limiter.limit("30 per minute")
def logout():
    data = parse(RefreshIn)
    revoke_refresh_token(data.refresh_token)
    return jsonify({"message": "Sesión cerrada"}), 200


@bp.post("/register")
@limiter.limit("5 per minute; 30 per hour")
def register():
    data = parse(RegisterIn)
    now = now_local()
    with transaction() as cur:
        verification.consume_token(cur, data.verification_token, verification.EMAIL_VERIFICATION,
                                   email=data.email)
        cur.execute("SELECT 1 FROM usuarios WHERE lower(nombreusuario) = lower(%s)", (data.nombreusuario,))
        if cur.fetchone():
            raise Conflict("El nombre de usuario ya está en uso", code="username_taken")
        cur.execute("SELECT 1 FROM usuarios WHERE lower(email) = %s", (data.email,))
        if cur.fetchone():
            raise Conflict("El email ya está registrado", code="email_taken")
        cur.execute(
            """
            INSERT INTO usuarios (nombreusuario, password_hash, rol, localid, email, created_at)
            VALUES (%s, %s, 'cliente', NULL, %s, %s) RETURNING usuarioid
            """,
            (data.nombreusuario, hash_password(data.password), data.email, now),
        )
        usuarioid = cur.fetchone()["usuarioid"]
        cur.execute(
            "INSERT INTO clientes (usuarioid, nombre, telefono, fecharegistro) VALUES (%s, %s, NULL, %s)",
            (usuarioid, data.nombre_completo, now.date()),
        )
    audit("register", usuarioid=usuarioid)
    return jsonify({"message": "Registro realizado correctamente", "usuarioid": usuarioid}), 201


@bp.post("/verification-codes")
@limiter.limit("5 per minute; 20 per hour")
def send_verification_code():
    data = parse(SendCodeIn)
    verification.request_code(data.email, data.purpose)
    # Mensaje idéntico exista o no la cuenta (evita enumeración de usuarios).
    return jsonify({"message": "Si los datos son correctos, recibirás un código en tu correo",
                    "username": None}), 200


@bp.post("/verification-codes/verify")
@limiter.limit("10 per minute; 40 per hour")
def verify_code():
    data = parse(VerifyCodeIn)
    token = verification.verify_code(data.email, data.purpose, data.codigo)
    username = None
    if data.purpose == verification.PASSWORD_RESET:
        # El nombre de usuario solo se revela tras demostrar el control del email.
        with transaction() as cur:
            cur.execute("SELECT nombreusuario FROM usuarios WHERE lower(email) = %s", (data.email,))
            row = cur.fetchone()
            username = row["nombreusuario"] if row else None
    return jsonify({"message": "Código verificado correctamente", "verification_token": token,
                    "username": username}), 200


@bp.post("/password-reset")
@limiter.limit("5 per minute; 20 per hour")
def reset_password():
    data = parse(ResetPasswordIn)
    with transaction() as cur:
        email = verification.consume_token(cur, data.reset_token, verification.PASSWORD_RESET)
        cur.execute(
            """
            UPDATE usuarios SET password_hash = %s, failed_logins = 0, locked_until = NULL
            WHERE lower(email) = %s RETURNING usuarioid, nombreusuario
            """,
            (hash_password(data.password), email),
        )
        user = cur.fetchone()
        if user:
            revoke_all_sessions(cur, user["usuarioid"])
    if user:
        audit("password_reset", usuarioid=user["usuarioid"])
        send_mail(email, "Tu contraseña ha cambiado - HairCloud", "password_changed",
                  title="Contraseña actualizada", username=user["nombreusuario"])
    return jsonify({"message": "Contraseña restablecida correctamente"}), 200


@bp.get("/me")
@require_auth()
def me():
    user = current_user()
    return jsonify({
        "usuarioid": user.usuarioid,
        "nombreusuario": user.nombreusuario,
        "rol": user.rol,
        "email": user.email,
        "clienteid": user.clienteid,
        "peluqueroid": user.peluqueroid,
        "localid": user.localid,
    }), 200


@bp.post("/change-password")
@require_auth()
@limiter.limit("5 per minute")
def change_password():
    data = parse(ChangePasswordIn)
    user = current_user()
    with transaction() as cur:
        cur.execute("SELECT password_hash FROM usuarios WHERE usuarioid = %s FOR UPDATE", (user.usuarioid,))
        row = cur.fetchone()
        if not verify_password(data.current_password, row["password_hash"]):
            raise Unauthorized("La contraseña actual no es correcta", code="invalid_credentials")
        cur.execute("UPDATE usuarios SET password_hash = %s WHERE usuarioid = %s",
                    (hash_password(data.new_password), user.usuarioid))
        revoke_all_sessions(cur, user.usuarioid)
        refresh_token = issue_refresh_token(cur, user.usuarioid)
    audit("password_changed", usuarioid=user.usuarioid)
    fresh = load_user(user.usuarioid)
    payload = _session_payload(fresh.__dict__, refresh_token)
    payload["message"] = "Contraseña actualizada"
    return jsonify(payload), 200
