package com.haircloud.data.model

data class LoginRequest(val nombreusuario: String, val password: String)

data class LoginResponse(
    val message: String,
    val usuarioid: Int,
    val nombreusuario: String,
    val rol: String,
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int
)

data class RefreshRequest(val refresh_token: String)

data class RegisterRequest(
    val nombre_completo: String,
    val email: String,
    val nombreusuario: String,
    val password: String,
    /** Token devuelto al verificar el código del email: sin él no hay registro. */
    val verification_token: String
)

data class RegisterResponse(val message: String, val usuarioid: Int)

data class SendCodeRequest(val email: String, val purpose: String)

data class VerifyCodeRequest(val email: String, val codigo: String, val purpose: String)

data class VerifyCodeResponse(
    val message: String,
    val verification_token: String,
    val username: String?
)

data class ResetPasswordRequest(val reset_token: String, val password: String)
