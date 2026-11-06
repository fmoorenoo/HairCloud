package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.SessionManager
import com.haircloud.data.apiCall
import com.haircloud.data.model.*
import com.haircloud.data.storage.Session

/** Capa intermedia entre los ViewModels de autenticación y la API. */
class AuthRepository {
    private val api = ApiClient.instance

    /** Inicia sesión y persiste los tokens cifrados. */
    suspend fun login(username: String, password: String): Result<LoginResponse> =
        apiCall("No se pudo iniciar sesión") { api.login(LoginRequest(username.trim(), password)) }
            .onSuccess { r ->
                SessionManager.save(Session(r.access_token, r.refresh_token, r.usuarioid, r.rol))
            }

    /** Borra la sesión local de inmediato y la revoca en el servidor (si hay red). */
    suspend fun logout() {
        val session = SessionManager.current()
        SessionManager.clear()
        session?.let { apiCall("") { api.logout(RefreshRequest(it.refreshToken)) } }
    }

    suspend fun register(
        nombre: String,
        email: String,
        username: String,
        password: String,
        verificationToken: String
    ): Result<RegisterResponse> = apiCall("Error en el registro") {
        api.register(RegisterRequest(nombre.trim(), email.trim(), username.trim(), password, verificationToken))
    }

    /** Solicita un código de verificación (recuperar contraseña o verificar email). */
    suspend fun sendVerificationCode(email: String, purpose: String): Result<ApiResponse> =
        apiCall("No se pudo enviar el código") { api.sendVerificationCode(SendCodeRequest(email.trim(), purpose)) }

    /** Verifica el código y devuelve un token de un solo uso para registrarse o cambiar la contraseña. */
    suspend fun verifyCode(email: String, code: String, purpose: String): Result<VerifyCodeResponse> =
        apiCall("Código incorrecto o caducado") {
            api.verifyCode(VerifyCodeRequest(email.trim(), code.trim(), purpose))
        }

    /** Restablece la contraseña usando el token obtenido al verificar el código. */
    suspend fun resetPassword(resetToken: String, newPassword: String): Result<ApiResponse> =
        apiCall("No se pudo restablecer la contraseña") {
            api.resetPassword(ResetPasswordRequest(resetToken, newPassword))
        }
}
