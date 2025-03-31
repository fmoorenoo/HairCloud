package com.haircloud.data.model

data class LoginRequest(val nombreusuario: String, val password: String)
data class LoginResponse(val message: String, val usuarioid: Int, val nombreusuario: String, val rol: String)

data class RegisterRequest(val nombre_completo: String, val email: String, val nombreusuario: String, val password: String)
data class RegisterResponse(val message: String, val usuarioid: Int)