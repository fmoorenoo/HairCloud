"""Envío de correo.

* Las plantillas son Jinja2 con autoescapado HTML: nombres, motivos de
  cancelación o descripciones introducidas por usuarios ya no pueden inyectar
  HTML en los correos (antes se interpolaban con f-strings sin escapar).
* El envío se hace después de confirmar la transacción y, por defecto, en un
  hilo aparte: un fallo SMTP ya no deshace una cita ni bloquea la respuesta.
* Backends: ``smtp`` (producción), ``console`` (desarrollo) y ``memory`` (tests).
"""
from __future__ import annotations

import logging
import smtplib
import ssl
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from email.message import EmailMessage
from email.utils import formataddr, make_msgid

from flask import Flask, current_app, render_template

from ..timeutils import now_local

log = logging.getLogger(__name__)

_executor = ThreadPoolExecutor(max_workers=2, thread_name_prefix="mailer")


@dataclass
class OutgoingMail:
    to: str
    subject: str
    text: str
    html: str


def init_mailer(app: Flask) -> None:
    app.extensions["mail_outbox"] = []


def outbox() -> list[OutgoingMail]:
    return current_app.extensions["mail_outbox"]


def _send_smtp(cfg: dict, mail: OutgoingMail) -> None:
    msg = EmailMessage()
    msg["Subject"] = mail.subject
    msg["From"] = formataddr((cfg["MAIL_SENDER_NAME"], cfg["MAIL_SENDER_ADDRESS"]))
    msg["To"] = mail.to
    msg["Message-ID"] = make_msgid(domain="haircloud.app")
    msg.set_content(mail.text)
    msg.add_alternative(mail.html, subtype="html")

    with smtplib.SMTP(cfg["MAIL_SERVER"], cfg["MAIL_PORT"], timeout=20) as smtp:
        if cfg["MAIL_USE_TLS"]:
            smtp.starttls(context=ssl.create_default_context())
        if cfg["MAIL_USERNAME"]:
            smtp.login(cfg["MAIL_USERNAME"], cfg["MAIL_PASSWORD"])
        smtp.send_message(msg)


def _deliver(cfg: dict, mail: OutgoingMail) -> None:
    try:
        _send_smtp(cfg, mail)
        log.info("Correo '%s' enviado", mail.subject)
    except Exception:
        log.exception("Fallo al enviar el correo '%s'", mail.subject)


def send_mail(to: str, subject: str, template: str, **context) -> None:
    context.setdefault("year", now_local().year)
    html = render_template(f"email/{template}.html", **context)
    text = render_template(f"email/{template}.txt", **context)
    mail = OutgoingMail(to=to, subject=subject, text=text, html=html)

    cfg = current_app.config
    backend = cfg["MAIL_BACKEND"]
    if backend == "memory":
        outbox().append(mail)
    elif backend == "console":
        log.warning("[MAIL console] Para: %s | Asunto: %s\n%s", to, subject, text)
    elif backend == "smtp":
        snapshot = {k: cfg[k] for k in ("MAIL_SERVER", "MAIL_PORT", "MAIL_USE_TLS", "MAIL_USERNAME",
                                        "MAIL_PASSWORD", "MAIL_SENDER_NAME", "MAIL_SENDER_ADDRESS")}
        if cfg["MAIL_ASYNC"]:
            _executor.submit(_deliver, snapshot, mail)
        else:
            _deliver(snapshot, mail)
    else:
        raise RuntimeError(f"MAIL_BACKEND desconocido: {backend}")
