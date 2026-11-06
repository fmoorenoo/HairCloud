# Auditoría de HairCloud y consolidación v2.0

Revisión completa del backend Flask, la app Android y la base de datos de la
versión académica (v1), y resumen de los cambios de la v2.0. Se ha mantenido
la interfaz: los cambios afectan a la lógica, la seguridad y los datos.

## 1. Hallazgos críticos (seguridad)

| # | Hallazgo en v1 | Impacto | Solución en v2 |
|---|---|---|---|
| C1 | **Ningún endpoint comprobaba la autenticación.** El JWT se emitía en el login pero ni la app lo enviaba ni el servidor lo verificaba. | Cualquiera con acceso a la API podía leer y modificar todos los datos. | Access token JWT obligatorio en todas las rutas (`require_auth`), verificado con algoritmo, emisor, audiencia y versión de credenciales. |
| C2 | **IDOR generalizado**: identidad tomada de la URL (`/clients/get_client/<id>`, `/update_client/<id>`, `get_all_barbershops/<clienteid>`...). | Leer emails/teléfonos de cualquier usuario, cambiar el email de otra cuenta (y robarla vía "recuperar contraseña"). | La identidad sale del token (`/me/...`). Ningún endpoint acepta `clienteid`/`usuarioid` del cliente. |
| C3 | Sin autorización por rol: `toggle_barber_role`, `deactivate_barber`, `create_barber`, `update_barbershop`, `delete_service`, `delete_review` abiertos a todos. | Escalada de privilegios, sabotaje de locales, borrado de reseñas ajenas. | Matriz de permisos por rol **y por local** (admin, semiadmin, peluquero, cliente), probada con tests. |
| C4 | Registro sin verificación real del email: `/auth/register` no comprobaba que el código se hubiese validado. | Cuentas con emails ajenos. | El registro exige un token de verificación de un solo uso emitido al validar el código. |
| C5 | Código de recuperación de 6 dígitos sin límite de intentos ni rate limiting; generado con `random`; guardado en claro; reutilizable. | Fuerza bruta del código → toma de cuentas. | `secrets`, HMAC en BD, máximo 5 intentos por código, rate limiting por IP, token de reset de un solo uso, revocación de todas las sesiones al cambiar la contraseña y aviso por email. |
| C6 | Enumeración de usuarios: "Usuario no encontrado" vs "Contraseña incorrecta"; `send_verification_code` devolvía el **nombre de usuario** de cualquier email. | Recolección de cuentas y datos. | Mensajes genéricos, tiempo de respuesta igualado (bcrypt siempre), el nombre de usuario solo se revela tras verificar el código. |
| C7 | `/mail/send_info_date` y `/mail/send_barber_stats` permitían enviar correos a cualquier cliente con contenido arbitrario, interpolado sin escapar en HTML. | Spam/phishing desde la cuenta de la empresa, inyección HTML. | Endpoints eliminados. Los correos los genera el servidor tras confirmar la operación, con plantillas Jinja2 autoescapadas. |
| C8 | Volcado de base de datos (`haircloud_backup.sql`: emails, nombres, teléfonos, hashes) versionado en un repositorio público. En este proyecto son **datos de prueba**, pero en un proyecto real supondría una exposición de datos personales (RGPD). | Mala práctica: los volcados no deben versionarse. | Retirado del control de versiones y añadido a `.gitignore`; se sustituye por migraciones + `seed-demo`. La copia del historial de git se conserva a propósito al tratarse de datos de ejemplo (ver §6). |
| C9 | Token de sesión de 7 días sin revocación, guardado en claro en DataStore e incluido en las copias de seguridad de Android. | Robo de sesión persistente. | Access token de 15 min + refresh token rotatorio (30 días) con detección de reutilización; logout que revoca en servidor; tokens cifrados con AES-GCM (Android Keystore); `allowBackup=false`. |
| C10 | `usesCleartextTraffic=true` y log de OkHttp nivel `BODY` (contraseñas y tokens en Logcat). | Credenciales en claro en la red y en logs. | Network Security Config: HTTPS obligatorio en release, HTTP solo a hosts de desarrollo en debug; log solo en debug, nivel BASIC y `Authorization` oculto. |
| C11 | `debug=True` por defecto y `SECRET_KEY` con valor por defecto; el manejador 500 y `/auth/test_connection` devolvían el texto del error interno. | Ejecución remota de código vía depurador de Werkzeug si se expone; filtrado de información. | Entorno `production` por defecto, la app no arranca sin secretos fuertes, errores homogéneos con id de incidencia, sondas `/health` sin detalles. |

## 2. Hallazgos graves (lógica de negocio y datos)

| # | Hallazgo en v1 | Solución en v2 |
|---|---|---|
| G1 | **Doble reserva**: no se comprobaba solape, horario del peluquero ni bloqueos al crear una cita. | Validación en servidor bajo bloqueo de fila del peluquero + restricción `EXCLUDE USING gist` en BD. Test de concurrencia: dos reservas simultáneas → una 201 y una 409. |
| G2 | El cliente enviaba `clienteid`, `localid` y `fechafin` de la cita. | El servidor deriva cliente (token), local (peluquero) y fin (duración del servicio); valida que el servicio pertenece al local. |
| G3 | `get_available_slots` con `duracion=0` (la app lo enviaba antes de cargar el servicio) entraba en un **bucle infinito** que bloqueaba un hilo del servidor. Además fallaba a partir de las 23:50 (`hour=24`), dependía del locale del servidor (`strftime('%A')`) y solo leía un tramo horario por día. | La duración sale del servicio; algoritmo puro y testeado; soporte de jornada partida. |
| G4 | Borrar un servicio hacía `ON DELETE CASCADE` sobre **todas las citas** (historial y estadísticas perdidos). | Baja lógica (`servicios.activo`) y FK `RESTRICT`. |
| G5 | Cambio de estado de citas sin reglas (cualquiera, cualquier transición). El fallo al enviar el email de cancelación deshacía la cancelación (envío dentro de la transacción) y con motivo nulo se producía un error. | Máquina de estados con permisos (cliente: solo cancelar con 30 min de antelación; personal: completar, no completada, cancelar con motivo), email enviado tras el COMMIT y en segundo plano. |
| G6 | Peticiones GET con efectos secundarios (`UPDATE citas` en `get_barber_dates`, `DELETE` de actividad en `get_barber_activity`). | Estado efectivo calculado en la vista `v_citas`; persistencia y purgas en `flask maintenance` (tarea programada). |
| G7 | `ON CONFLICT DO NOTHING` en favoritos sin clave única → duplicados. | Restricción `UNIQUE (clienteid, localid)` (deduplicando antes). |
| G8 | `actividad_peluquero` sin claves foráneas (4 registros huérfanos en los datos reales). | FKs añadidas tras limpiar huérfanos. |
| G9 | `/calendar/get_barber_dates` consultaba una columna inexistente (`fechahora`) → siempre 500. | Eliminado (no se usaba). |
| G10 | Límite de 3 reseñas por local evitable con peticiones concurrentes; `edit_service` permitía mover un servicio a otro local; `get_inactive_barbers` listaba el personal inactivo (con emails) de **todos** los locales. | Bloqueo por cliente, local inmutable, listados limitados al local del admin. |
| G11 | Conexiones a BD sin pool y sin `try/finally` (se filtraban en cada error); fechas de Python y de PostgreSQL mezcladas sin zona horaria. | Pool `psycopg_pool`, transacciones con rollback garantizado, zona horaria del negocio (`APP_TIMEZONE`) en app y sesión de BD. |
| G12 | Sin validación de entrada en servidor (longitudes, formatos, rangos): errores 500 al superar los `varchar`, precios negativos, horarios invertidos. | Esquemas Pydantic con `extra="forbid"` en todas las entradas; restricciones `CHECK` en BD. |
| G13 | Política de contraseñas débil (4 letras + 1 número) y solo en la app. | Mínimo 8 caracteres con letras y números, validada en servidor y en la app con el mismo criterio. |
| G14 | Bloqueo de cuenta inexistente. | 5 intentos fallidos → bloqueo de 15 min, rate limiting por IP y por usuario, auditoría de eventos. |

## 3. Calidad y mantenibilidad

- **Backend**: separación en capas (`api` → `services` → `db`), serializadores explícitos (antes `SELECT *` con tipos convertidos de forma distinta en cada endpoint), API versionada `/api/v1`, `requirements.txt` con versiones fijadas, `.env.example`, servidor WSGI de producción (Waitress), CLI de migraciones y mantenimiento.
- **Android**: helper `apiCall` que sustituye ~40 bloques try/catch duplicados (con `body()!!` que podía cerrar la app), llamadas `suspend` en lugar de `execute()` bloqueante, URL de la API por tipo de build, dependencias alineadas (OkHttp 3/4 y coroutines 1.6/1.7 mezcladas en v1), R8 activado en release (APK de 19,7 MB a 2,3 MB), cierre de sesión que no se cancela al destruir la pantalla, sesión caducada → vuelta automática al login.
- **Base de datos**: migraciones versionadas (`0001` esquema original, `0002` consolidación) aplicables tanto a una BD nueva como a la existente; índices para las consultas de agenda, citas y reseñas; columna de hash renombrada (`contraseña` → `password_hash`).
- **Tests**: 41 tests de backend contra PostgreSQL real (autenticación, sesiones, permisos, reservas, concurrencia, disponibilidad, mantenimiento), tests JVM de la app y CI en GitHub Actions.

## 4. Verificación realizada

- Migración aplicada sobre una restauración del volcado real (15 usuarios, 85 citas, 16 locales) sin errores; los hashes bcrypt existentes siguen siendo válidos.
- `pytest`: 41/41. Tests JVM Android: 6/6. `assembleDebug` y `assembleRelease` (R8) correctos.
- Test de contrato app↔API sobre Waitress: 73 validaciones de las respuestas frente a 21 data classes Kotlin, flujos completos de cliente, peluquero y admin, rate limiting y rotación de sesiones.
- **No verificado**: la app en un dispositivo o emulador real (no disponible en el entorno de la revisión) y el envío SMTP real.

## 5. Cambios de comportamiento visibles

- Las sesiones abiertas con la v1 se cierran al actualizar: hay que volver a iniciar sesión.
- Las contraseñas nuevas deben cumplir la nueva política (las existentes siguen funcionando).
- Al dar de baja a un peluquero se cierran sus sesiones al instante; la respuesta indica cuántas citas pendientes tiene.
- El teléfono del personal ya no se muestra a los clientes.
- Se puede iniciar sesión con el nombre de usuario o con el email.

## 6. Pendiente / recomendaciones

1. `haircloud_backup.sql` sigue en el historial de git. Al ser un proyecto de ejemplo con datos de prueba **se ha decidido no reescribir el historial**. En un proyecto real habría que purgarlo (`git filter-repo --path haircloud_backup.sql --invert-paths` + `push --force`) y valorar la notificación a los afectados.
2. Rotar la contraseña de aplicación de Gmail y cualquier secreto que haya estado en el `.env` de la v1.
3. Desplegar detrás de un proxy HTTPS (`TRUSTED_PROXIES=1`) y usar Redis para el rate limiting si hay varios procesos.
4. Programar `flask --app wsgi maintenance` a diario.
5. Las tablas `pagos`, `notificaciones`, `reportes`, `historial_citas` y `configuracion` no se usan; conviene decidir si se implementan o se eliminan.
6. Probar la app en dispositivo real antes de publicar, y firmar el build release.
