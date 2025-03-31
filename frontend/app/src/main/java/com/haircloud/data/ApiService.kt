package com.haircloud.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import com.haircloud.data.model.*


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

    @GET("/api/get_client/{client_id}")
    fun getClient(@Path("client_id") clientId: Int): Call<ClientResponse>
}