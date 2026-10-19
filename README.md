<div align="center">

<img src="https://github.com/user-attachments/assets/91ba639c-f58e-429b-9c2c-7ab21275f54d" alt="Logotipo HairCloud" width="115">

# HairCloud

### Gestión de citas y operaciones para peluquerías desde Android

**Proyecto Full Stack · Aplicación Android + API REST + PostgreSQL**

![Kotlin](https://img.shields.io/badge/Kotlin-1A1B26?style=for-the-badge&logo=kotlin&logoColor=E46B79)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-1A1B26?style=for-the-badge&logo=jetpackcompose&logoColor=E46B79)
![Python](https://img.shields.io/badge/Python-1A1B26?style=for-the-badge&logo=python&logoColor=E46B79)
![Flask](https://img.shields.io/badge/Flask-1A1B26?style=for-the-badge&logo=flask&logoColor=E46B79)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-1A1B26?style=for-the-badge&logo=postgresql&logoColor=E46B79)

[![Estado](https://img.shields.io/badge/ESTADO-En_desarrollo-712B39?style=flat-square)](#estado-del-proyecto)
[![Android](https://img.shields.io/badge/PLATAFORMA-Android-22252C?style=flat-square)](#tecnologías)
[![Código](https://img.shields.io/badge/REPOSITORIO-Código_fuente-22252C?style=flat-square)](https://github.com/fmoorenoo/HairCloud)

</div>

---

## Sobre el proyecto

**HairCloud** es una aplicación Android desarrollada como proyecto final de formación en desarrollo de aplicaciones. Su objetivo es facilitar la gestión de citas de peluquerías y ofrecer experiencias diferenciadas para **clientes** y **profesionales**.

El repositorio reúne una interfaz móvil construida con **Kotlin y Jetpack Compose**, un backend **Python/Flask** con endpoints REST y una base de datos **PostgreSQL**.

> **Estado:** proyecto académico en desarrollo. La presencia de pantallas y rutas en el código no implica que todas las funciones estén verificadas mediante pruebas de extremo a extremo.

## Funcionalidades

| Para clientes | Para peluquerías y profesionales |
| --- | --- |
| Registro, acceso y recuperación de cuenta | Perfil y administración de la peluquería |
| Consulta de peluquerías y servicios | Configuración de servicios y profesionales |
| Reserva y consulta de citas | Calendario y gestión de citas |
| Favoritos y perfiles | Pantallas de informes y estadísticas |
| Consulta de reseñas | Gestión y consulta de reseñas |

El código también incluye utilidades para notificaciones y correos relacionados con el proceso de reserva.

## Tecnologías

| Capa | Herramientas |
| --- | --- |
| **Aplicación móvil** | Kotlin, Jetpack Compose, Material 3 |
| **Arquitectura Android** | ViewModel, Repository Pattern, Navigation Compose |
| **Comunicación** | Retrofit, OkHttp, API REST |
| **Preferencias y asincronía** | DataStore, Kotlin Coroutines |
| **Backend** | Python, Flask |
| **Autenticación** | JWT, Flask-Bcrypt |
| **Base de datos** | PostgreSQL, psycopg2 |
| **Correo** | Flask-Mail |
| **Entornos de desarrollo** | Android Studio, Python y PostgreSQL |

## Arquitectura

```text
HairCloud/
├── frontend/                  # Proyecto Android
│   └── app/src/main/java/com/haircloud/
│       ├── data/              # Cliente API, modelos y repositorios
│       ├── navigation/        # Navegación entre pantallas
│       ├── screens/           # Interfaces de clientes y profesionales
│       ├── viewmodel/         # Estado y lógica de presentación
│       └── utils/             # Utilidades compartidas
├── backend/
│   ├── app/api/               # Rutas REST por dominio
│   ├── app/db/                # Conexión PostgreSQL
│   ├── app/utils/             # Utilidades de correo
│   ├── app.py                 # Punto de entrada Flask
│   └── config.py              # Configuración por entorno
└── anteproyecto/              # Análisis, diagramas y diseños
```

La aplicación Android se comunica con el backend mediante **Retrofit**. Flask organiza los endpoints de autenticación, clientes, profesionales, peluquerías, servicios, calendario y citas.

## Diseño y capturas

Las imágenes disponibles en el repositorio corresponden a **propuestas de interfaz y material de diseño** del anteproyecto, no necesariamente a capturas de la aplicación en ejecución.

<div align="center">
  <img src="anteproyecto/media/figma_main_screens.png" alt="Propuesta de pantallas principales de HairCloud" width="85%">
</div>

| Vista para clientes | Flujo de reserva |
| :---: | :---: |
| <img src="anteproyecto/media/figma_client_view.png" alt="Diseño de vista de cliente" width="100%"> | <img src="anteproyecto/media/figma_client_reserve.png" alt="Diseño del flujo de reserva" width="100%"> |

**Documentación adicional:** [Anteproyecto](anteproyecto/anteproyectoHairCloud.pdf) · [Diseños en PDF](anteproyecto/figmaHairCloud.pdf) · [Diagrama de datos](anteproyecto/media/diagram_image.png)

## Instalación y configuración

> Se requiere configurar una base de datos PostgreSQL y el servicio Flask; no se proporciona un despliegue público listo para usar. Las instrucciones siguientes son orientativas y no se han validado mediante una instalación completa en esta revisión.

### Backend

1. Prepara un entorno virtual de Python en `backend/`.
2. Instala las dependencias necesarias para los imports del proyecto, entre ellas Flask, Flask-Mail, Flask-Bcrypt, psycopg2, PyJWT y python-dotenv. Este repositorio no incluye un archivo de dependencias Python reproducible.
3. Configura las variables de entorno requeridas por `backend/config.py`:

```dotenv
SECRET_KEY=replace-with-a-long-random-secret
DB_HOST=localhost
DB_PORT=5432
DB_NAME=HairCloud
DB_USER=your_database_user
DB_PASSWORD=your_database_password
MAIL_USERNAME=your_email_account
MAIL_PASSWORD=your_email_app_password
```

4. Configura una base de datos de desarrollo de acuerdo con el esquema del proyecto.
5. Ejecuta `python app.py` desde `backend/` únicamente en tu entorno de desarrollo.

**No utilices credenciales de producción en archivos versionados.** El respaldo `haircloud_backup.sql` no ha sido validado como un conjunto de datos anónimos o adecuado para redistribución.

### Android

1. Abre la carpeta `frontend/` en Android Studio.
2. Sincroniza el proyecto Gradle.
3. Revisa `frontend/app/src/main/java/com/haircloud/data/ApiClient.kt` para configurar la URL del backend.
4. Para el emulador Android, el proyecto define `http://10.0.2.2:5000/`, que apunta al equipo anfitrión.
5. Inicia el backend local y ejecuta la app en un emulador de desarrollo.

La configuración actual permite **tráfico HTTP sin cifrar** y registra cuerpos de solicitudes y respuestas mediante OkHttp. **No es una configuración adecuada para producción**, especialmente con información de autenticación o datos personales. Antes de desplegar, se debe revisar la autenticación, habilitar HTTPS y limitar los logs sensibles.

## Estado del proyecto

**En desarrollo / proyecto académico.** Se incluyen implementaciones de frontend y backend, además de documentación del diseño. No se han verificado en esta revisión una compilación reproducible, tests completos ni un despliegue de producción.

## Autor

**Fernando Montelongo Moreno** · Full Stack Developer · DAM + DAW

[![GitHub](https://img.shields.io/badge/GitHub-fmoorenoo-22252C?style=flat-square&logo=github)](https://github.com/fmoorenoo)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Perfil_profesional-712B39?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/fernando-montelongo/)

---

<div align="center">
  <sub>HairCloud · Proyecto de desarrollo multiplataforma · Fernando Montelongo Moreno</sub>
</div>
