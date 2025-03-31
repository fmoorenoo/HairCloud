from flask import Blueprint

# Blueprints
auth_bp = Blueprint('auth', __name__)
clients_bp = Blueprint('clients', __name__)

from . import auth, clients