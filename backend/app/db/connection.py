import os
import psycopg2
from psycopg2.extras import DictCursor
from config import Config

# Conectarse a la base de datos
def get_connection():
    return psycopg2.connect(
        host=Config.DB_HOST,
        port=Config.DB_PORT,
        dbname=Config.DB_NAME,
        user=Config.DB_USER,
        password=Config.DB_PASSWORD,
        options="-c search_path=public",
        cursor_factory=DictCursor
    )