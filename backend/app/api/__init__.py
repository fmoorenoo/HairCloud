from flask import Blueprint

# Blueprints
auth_bp = Blueprint('auth', __name__)
clients_bp = Blueprint('clients', __name__)
barbershops_bp = Blueprint('barbershops', __name__)

from . import auth, clients, barbers