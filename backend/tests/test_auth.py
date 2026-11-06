"""Autenticación, sesiones y recuperación de cuenta."""
import re

from app.services.mailer import outbox

from .conftest import PASSWORD, auth, login

API = "/api/v1"


def _code_from_outbox(app):
    with app.app_context():
        mail = outbox()[-1]
    return re.search(r"Código: (\d{6})", mail.text).group(1)


def test_protected_endpoints_require_token(client, world):
    for method, url in [("get", "/me/client"), ("get", "/barbershops"), ("get", "/me/barber"),
                        ("patch", f"/barbershops/{world['localid']}"),
                        ("delete", "/services/1"), ("patch", "/appointments/1/status")]:
        resp = getattr(client, method)(API + url, json={})
        assert resp.status_code == 401, (method, url, resp.get_json())


def test_login_generic_error_does_not_reveal_accounts(client, world):
    unknown = client.post(f"{API}/auth/login", json={"nombreusuario": "noexiste", "password": "x"})
    wrong = client.post(f"{API}/auth/login", json={"nombreusuario": world["client"]["username"], "password": "x"})
    assert unknown.status_code == wrong.status_code == 401
    assert unknown.get_json()["error"] == wrong.get_json()["error"]


def test_login_returns_tokens_and_allows_access(client, world):
    tokens = login(client, world["client"]["username"])
    assert tokens["rol"] == "cliente" and tokens["refresh_token"] and tokens["access_token"]
    resp = client.get(f"{API}/me/client", headers={"Authorization": f"Bearer {tokens['access_token']}"})
    assert resp.status_code == 200
    assert resp.get_json()["nombreusuario"] == world["client"]["username"]


def test_tampered_or_foreign_token_rejected(client, world):
    tokens = login(client, world["client"]["username"])
    bad = tokens["access_token"][:-3] + "abc"
    assert client.get(f"{API}/me/client", headers={"Authorization": f"Bearer {bad}"}).status_code == 401
    import jwt
    forged = jwt.encode({"sub": str(world["admin"]["usuarioid"]), "ver": 0, "typ": "access",
                         "iat": 1, "exp": 4102444800}, "otra-clave" * 5, algorithm="HS256")
    assert client.get(f"{API}/me/barber", headers={"Authorization": f"Bearer {forged}"}).status_code == 401


def test_account_locks_after_repeated_failures(client, world):
    user = world["client"]["username"]
    for _ in range(5):
        client.post(f"{API}/auth/login", json={"nombreusuario": user, "password": "malamala1"})
    resp = client.post(f"{API}/auth/login", json={"nombreusuario": user, "password": PASSWORD})
    assert resp.status_code == 429
    assert resp.get_json()["code"] == "account_locked"


def test_refresh_rotation_and_reuse_detection(client, world):
    tokens = login(client, world["client"]["username"])
    first = client.post(f"{API}/auth/refresh", json={"refresh_token": tokens["refresh_token"]})
    assert first.status_code == 200
    rotated = first.get_json()["refresh_token"]
    assert rotated != tokens["refresh_token"]

    # Reutilizar el token antiguo revoca toda la familia (incluido el rotado).
    reuse = client.post(f"{API}/auth/refresh", json={"refresh_token": tokens["refresh_token"]})
    assert reuse.status_code == 401 and reuse.get_json()["code"] == "refresh_token_reused"
    again = client.post(f"{API}/auth/refresh", json={"refresh_token": rotated})
    assert again.status_code == 401


def test_logout_revokes_refresh_token(client, world):
    tokens = login(client, world["client"]["username"])
    assert client.post(f"{API}/auth/logout", json={"refresh_token": tokens["refresh_token"]}).status_code == 200
    resp = client.post(f"{API}/auth/refresh", json={"refresh_token": tokens["refresh_token"]})
    assert resp.status_code == 401


def test_register_requires_verified_email(app, client):
    email = "nuevo@test.haircloud"
    body = {"nombre_completo": "Nuevo Cliente", "email": email, "nombreusuario": "nuevo_user",
            "password": "Clave1234", "verification_token": "x" * 43}
    resp = client.post(f"{API}/auth/register", json=body)
    assert resp.status_code == 400 and resp.get_json()["code"] == "invalid_verification_token"

    assert client.post(f"{API}/auth/verification-codes",
                       json={"email": email, "purpose": "email_verification"}).status_code == 200
    code = _code_from_outbox(app)
    verified = client.post(f"{API}/auth/verification-codes/verify",
                           json={"email": email, "codigo": code, "purpose": "email_verification"})
    assert verified.status_code == 200
    body["verification_token"] = verified.get_json()["verification_token"]

    assert client.post(f"{API}/auth/register", json=body).status_code == 201
    # El token es de un solo uso.
    body["nombreusuario"] = "otro_user"
    assert client.post(f"{API}/auth/register", json=body).status_code == 400
    login(client, "nuevo_user", "Clave1234")


def test_register_validates_password_policy(client):
    resp = client.post(f"{API}/auth/register", json={
        "nombre_completo": "X Y", "email": "a@test.haircloud", "nombreusuario": "usuario_ok",
        "password": "abc1", "verification_token": "x" * 43})
    assert resp.status_code == 422


def test_verification_code_attempts_are_limited(app, client, world):
    email = world["client"]["email"]
    client.post(f"{API}/auth/verification-codes", json={"email": email, "purpose": "password_reset"})
    code = _code_from_outbox(app)
    wrong = "000000" if code != "000000" else "111111"
    for _ in range(5):
        resp = client.post(f"{API}/auth/verification-codes/verify",
                           json={"email": email, "codigo": wrong, "purpose": "password_reset"})
        assert resp.status_code == 400
    # Tras agotar los intentos ni siquiera el código correcto sirve.
    resp = client.post(f"{API}/auth/verification-codes/verify",
                       json={"email": email, "codigo": code, "purpose": "password_reset"})
    assert resp.status_code == 400


def test_password_reset_does_not_enumerate_and_revokes_sessions(app, client, world):
    unknown = client.post(f"{API}/auth/verification-codes",
                          json={"email": "nadie@test.haircloud", "purpose": "password_reset"})
    known = client.post(f"{API}/auth/verification-codes",
                        json={"email": world["client"]["email"], "purpose": "password_reset"})
    assert unknown.status_code == known.status_code == 200
    assert unknown.get_json() == known.get_json()
    assert known.get_json()["username"] is None  # antes se devolvía el nombre de usuario

    old = login(client, world["client"]["username"])
    code = _code_from_outbox(app)
    verified = client.post(f"{API}/auth/verification-codes/verify",
                           json={"email": world["client"]["email"], "codigo": code, "purpose": "password_reset"})
    assert verified.get_json()["username"] == world["client"]["username"]
    token = verified.get_json()["verification_token"]
    resp = client.post(f"{API}/auth/password-reset", json={"reset_token": token, "password": "NuevaClave9"})
    assert resp.status_code == 200

    # Sesiones previas invalidadas y token de reset consumido.
    assert client.get(f"{API}/me/client",
                      headers={"Authorization": f"Bearer {old['access_token']}"}).status_code == 401
    assert client.post(f"{API}/auth/refresh", json={"refresh_token": old["refresh_token"]}).status_code == 401
    assert client.post(f"{API}/auth/password-reset",
                       json={"reset_token": token, "password": "OtraClave9"}).status_code == 400
    login(client, world["client"]["username"], "NuevaClave9")


def test_inactive_staff_cannot_login_and_sessions_are_revoked(client, world):
    barber_headers = auth(client, world["barber"])
    admin_headers = auth(client, world["admin"])
    resp = client.patch(f"{API}/barbers/{world['barber']['peluqueroid']}/active",
                        json={"activo": False}, headers=admin_headers)
    assert resp.status_code == 200
    assert client.get(f"{API}/me/barber", headers=barber_headers).status_code == 401
    resp = client.post(f"{API}/auth/login", json={"nombreusuario": world["barber"]["username"],
                                                    "password": PASSWORD})
    assert resp.status_code == 403


def test_errors_do_not_leak_internals(client):
    resp = client.post(f"{API}/auth/login", data="no es json", content_type="application/json")
    body = resp.get_json()
    assert resp.status_code == 400
    assert set(body) <= {"error", "code", "details"}
    assert client.get("/auth/test_connection").status_code == 404
    assert client.get("/health/ready").get_json() == {"status": "ok"}


def test_security_headers(client):
    resp = client.get("/health")
    assert resp.headers["X-Content-Type-Options"] == "nosniff"
    assert resp.headers["Cache-Control"] == "no-store"
