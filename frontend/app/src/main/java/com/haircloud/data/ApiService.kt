package com.haircloud.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

data class LoginRequest(val nombreusuario: String, val password: String)
data class LoginResponse(val message: String, val usuarioid: Int, val nombreusuario: String, val rol: String)

data class RegisterRequest(val nombre_completo: String, val email: String, val nombreusuario: String, val password: String)
data class RegisterResponse(val message: String, val usuarioid: Int)

data class ApiResponse(val message: String, val username: String?)

interface ApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/auth/send_verification_code")
    suspend fun sendVerificationCode(@Body requestBody: Map<String, String>): ApiResponse

    @POST("/api/auth/verify_code")
    suspend fun verifyCode(@Body requestBody: Map<String, String>): ApiResponse

    @POST("/api/auth/reset_password")
    suspend fun resetPassword(@Body requestBody: Map<String, String>): ApiResponse
}