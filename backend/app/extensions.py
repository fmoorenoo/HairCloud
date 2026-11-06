from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# Límite global por IP; los endpoints sensibles (login, códigos, registro)
# añaden límites más estrictos. En producción con varios procesos conviene
# usar Redis: RATELIMIT_STORAGE_URI=redis://...
limiter = Limiter(key_func=get_remote_address)
