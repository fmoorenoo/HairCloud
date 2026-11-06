"""Autenticación por petición y autorización por rol / local.

La identidad del usuario se obtiene **siempre** del token, nunca de
parámetros de la URL o del cuerpo. En la versión anterior ningún endpoint
comprobaba el token, de modo que cualquiera podía leer o modificar datos de
cualquier usuario cambiando un número en la URL (IDOR).

Roles:
    cliente     Reserva citas, gestiona sus favoritos y reseñas.
    peluquero   Ve y gestiona sus propias citas, horario y perfil.
    semiadmin   Peluquero que además gestiona servicios y modera reseñas.
    admin       Responsable del local: todo lo anterior + personal e info del local.
    superadmin  Administración global de la plataforma.
"""
from __future__ import annotations

from dataclasses import dataclass
from functools import wraps
from typing import Callable, Iterable

from flask import g, request

from ..db import fetch_one
from ..errors import Forbidden, NotFound, Unauthorized
from .tokens import decode_access_token

CLIENT = "cliente"
BARBER = "peluquero"
SEMIADMIN = "semiadmin"
ADMIN = "admin"
SUPERADMIN = "superadmin"

STAFF_ROLES = frozenset({BARBER, SEMIADMIN, ADMIN})
SERVICE_MANAGERS = frozenset({SEMIADMIN, ADMIN})
REVIEW_MODERATORS = frozenset({SEMIADMIN, ADMIN})
LOCAL_MANAGERS = frozenset({ADMIN})


@dataclass(frozen=True)
class CurrentUser:
    usuarioid: int
    nombreusuario: str
    rol: str
    email: str
    token_version: int
    clienteid: int | None
    peluqueroid: int | None
    localid: int | None
    activo: bool

    @property
    def is_client(self) -> bool:
        return self.rol == CLIENT

    @property
    def is_staff(self) -> bool:
        return self.rol in STAFF_ROLES

    @property
    def is_superadmin(self) -> bool:
        return self.rol == SUPERADMIN

    def has_local_role(self, localid: int, roles: Iterable[str]) -> bool:
        if self.is_superadmin:
            return True
        return self.localid == localid and self.rol in roles

    def require_local_role(self, localid: int, roles: Iterable[str]) -> None:
        if not self.has_local_role(localid, roles):
            raise Forbidden("No tienes permiso para realizar esta acción en este local")


USER_QUERY = """
    SELECT u.usuarioid, u.nombreusuario, u.rol, u.email, u.token_version,
           c.clienteid, p.peluqueroid,
           COALESCE(p.localid, u.localid) AS localid,
           COALESCE(p.activo, TRUE) AS activo
    FROM usuarios u
    LEFT JOIN clientes c ON c.usuarioid = u.usuarioid
    LEFT JOIN peluqueros p ON p.usuarioid = u.usuarioid
    WHERE u.usuarioid = %s
"""


def load_user(usuarioid: int) -> CurrentUser | None:
    row = fetch_one(USER_QUERY, (usuarioid,))
    return CurrentUser(**row) if row else None


def _bearer_token() -> str:
    header = request.headers.get("Authorization", "")
    scheme, _, token = header.partition(" ")
    if scheme.lower() != "bearer" or not token.strip():
        raise Unauthorized("Se requiere autenticación", code="missing_token")
    return token.strip()


def authenticate() -> CurrentUser:
    payload = decode_access_token(_bearer_token())
    try:
        usuarioid = int(payload["sub"])
    except (TypeError, ValueError):
        raise Unauthorized("Token no válido", code="invalid_token")
    user = load_user(usuarioid)
    # La versión de credenciales permite revocar tokens en vigor (cambio de
    # contraseña, baja de personal, cambio de rol).
    if user is None or user.token_version != payload["ver"]:
        raise Unauthorized("La sesión ya no es válida", code="token_revoked")
    if user.is_staff and not user.activo:
        raise Unauthorized("Tu cuenta de personal está desactivada", code="account_inactive")
    return user


def current_user() -> CurrentUser:
    user = getattr(g, "current_user", None)
    if user is None:
        raise Unauthorized("Se requiere autenticación", code="missing_token")
    return user


def require_auth(*roles: str) -> Callable:
    """Decorador: exige un access token válido y, opcionalmente, un rol."""
    allowed = frozenset(roles)

    def decorator(view):
        @wraps(view)
        def wrapper(*args, **kwargs):
            user = authenticate()
            if allowed and user.rol not in allowed and not user.is_superadmin:
                raise Forbidden("No tienes permiso para acceder a este recurso")
            g.current_user = user
            return view(*args, **kwargs)
        return wrapper
    return decorator


def require_client() -> CurrentUser:
    user = current_user()
    if user.clienteid is None:
        raise Forbidden("Esta acción solo está disponible para clientes")
    return user


def get_barber_or_404(peluqueroid: int) -> dict:
    row = fetch_one(
        """
        SELECT p.peluqueroid, p.usuarioid, p.localid, p.nombre, COALESCE(p.activo, TRUE) AS activo,
               u.rol, u.email
        FROM peluqueros p JOIN usuarios u ON u.usuarioid = p.usuarioid
        WHERE p.peluqueroid = %s
        """,
        (peluqueroid,),
    )
    if row is None:
        raise NotFound("Peluquero no encontrado")
    return row


def require_barber_access(peluqueroid: int, *, manager_roles: Iterable[str] = LOCAL_MANAGERS) -> dict:
    """El propio peluquero o un responsable de su local."""
    user = current_user()
    barber = get_barber_or_404(peluqueroid)
    if user.peluqueroid == peluqueroid or user.has_local_role(barber["localid"], manager_roles):
        return barber
    raise Forbidden("No tienes permiso sobre este peluquero")
