import psycopg2
from flask import Flask

app = Flask(__name__)


# Conexión a la base de datos
connection = psycopg2.connect(
    host="localhost",
    port="5432",
    dbname="HairCloud",
    user="postgres",
    password="profesor1234",
    options="-c search_path=public"
)
cursor = connection.cursor()



if __name__ == '__main__':
    app.run(debug=True)