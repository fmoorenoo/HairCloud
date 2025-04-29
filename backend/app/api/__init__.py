from flask import Blueprint

# Blueprints
auth_bp = Blueprint('auth', __name__)
clients_bp = Blueprint('clients', __name__)
barbershops_bp = Blueprint('barbershops', __name__)
calendar_bp = Blueprint('calendar', __name__)
dates_bp = Blueprint('dates', __name__)
barbers_bp = Blueprint('barbers', __name__)

from . import auth, clients, barbershops, calendar, dates, barbers