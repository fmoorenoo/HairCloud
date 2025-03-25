from flask import Blueprint

# Blueprints
auth_bp = Blueprint('auth', __name__)
users_bp = Blueprint('users', __name__)
clients_bp = Blueprint('clients', __name__)

from . import auth, clients