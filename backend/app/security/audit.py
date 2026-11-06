"""Registro de auditoría de eventos de seguridad.

Se guardan inicios de sesión fallidos, bloqueos, restablecimientos de
contraseña, cambios de rol, altas/bajas de personal, etc. Nunca se guardan
contraseñas, códigos ni tokens.
"""
from __future__ import annotations

import json
import logging

from flask import has_request_context, request

from ..db import transaction
from ..timeutils import now_local

log = logging.getLogger("haircloud.audit")


def client_ip() -> str | None:
    if not has_request_context():
        return None
    return request.remote_addr


def audit(evento: str, *, usuarioid: int | None = None, actor: int | None = None,
          detalles: dict | None = None, cur=None) -> None:
    params = (evento[:50], usuarioid, actor, client_ip(),
              json.dumps(detalles or {}, ensure_ascii=False, default=str), now_local())
    sql = """
        INSERT INTO audit_log (evento, usuarioid, actor_id, ip, detalles, fecha)
        VALUES (%s, %s, %s, %s, %s::jsonb, %s)
    """
    log.info("audit evento=%s usuario=%s actor=%s", evento, usuarioid, actor)
    try:
        if cur is not None:
            cur.execute(sql, params)
        else:
            with transaction() as own:
                own.execute(sql, params)
    except Exception:  # la auditoría nunca debe romper la operación principal
        log.exception("No se pudo registrar el evento de auditoría %s", evento)
