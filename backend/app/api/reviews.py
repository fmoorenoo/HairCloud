"""Moderación de reseñas."""
from __future__ import annotations

from flask import Blueprint, jsonify

from ..db import transaction
from ..errors import NotFound
from ..security.audit import audit
from ..security.auth import REVIEW_MODERATORS, current_user, require_auth

bp = Blueprint("reviews", __name__, url_prefix="/reviews")


@bp.delete("/<int:resenaid>")
@require_auth()
def delete_review(resenaid: int):
    user = current_user()
    with transaction() as cur:
        cur.execute("SELECT resenaid, clienteid, localid FROM resenas WHERE resenaid = %s FOR UPDATE", (resenaid,))
        review = cur.fetchone()
        is_author = review is not None and user.clienteid == review["clienteid"]
        is_moderator = review is not None and user.has_local_role(review["localid"], REVIEW_MODERATORS)
        if review is None or not (is_author or is_moderator):
            # Antes cualquiera podía borrar cualquier reseña. 404 para no
            # confirmar la existencia de reseñas que no puede gestionar.
            raise NotFound("Reseña no encontrada")
        cur.execute("DELETE FROM resenas WHERE resenaid = %s", (resenaid,))
    if is_moderator and not is_author:
        audit("review_moderated", actor=user.usuarioid, detalles={"resenaid": resenaid,
                                                                    "localid": review["localid"]})
    return jsonify({"message": "Reseña eliminada", "username": None}), 200
