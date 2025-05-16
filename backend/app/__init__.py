import os
from flask import Flask, jsonify, request
from app.extensions import mail, bcrypt
from config import config


def create_app(config_name=None):
    if config_name is None:
        config_name = os.getenv('FLASK_ENV', 'default')

    app = Flask(__name__)
    app.config.from_object(config[config_name])

    # Extensiones
    mail.init_app(app)
    bcrypt.init_app(app)

    # Registrar blueprints
    from app.api import auth_bp, clients_bp, barbershops_bp, calendar_bp, dates_bp, barbers_bp, services_bp, mail_bp
    app.register_blueprint(auth_bp, url_prefix='/auth')
    app.register_blueprint(clients_bp, url_prefix='/clients')
    app.register_blueprint(barbershops_bp, url_prefix='/barbershops')
    app.register_blueprint(calendar_bp, url_prefix='/calendar')
    app.register_blueprint(dates_bp, url_prefix='/dates')
    app.register_blueprint(barbers_bp, url_prefix='/barbers')
    app.register_blueprint(services_bp, url_prefix='/services')
    app.register_blueprint(mail_bp, url_prefix='/mail')


    # Manejar errores
    @app.errorhandler(404)
    def not_found(error):
        return jsonify({"error": "Not found", "path": request.path}), 404

    @app.errorhandler(500)
    def server_error(error):
        return jsonify({"error": "Internal server error", "details": str(error)}), 500

    return app