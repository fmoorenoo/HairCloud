package com.haircloud.data

import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import com.haircloud.data.model.*
import retrofit2.http.HTTP


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

    @GET("/api/clients/get_client/{client_id}")
    fun getClient(@Path("client_id") clientId: Int): Call<ClientResponse>

    @PUT("/api/clients/update_client/{user_id}")
    fun updateClient(
        @Path("user_id") userId: Int,
        @Body updateData: Map<String, String?>
    ): Call<ApiResponse>

    @GET("/api/barbershops/get_all_barbershops/{client_id}")
    fun getAllBarbershops(@Path("client_id") clientId: Int): Call<List<BarbershopResponse>>

    @GET("/api/barbershops/get_barbershop/{client_id}/{local_id}")
    fun getBarbershopById(
        @Path("client_id") clientId: Int,
        @Path("local_id") localId: Int
    ): Call<BarbershopResponse>

    @GET("/api/barbershops/get_favorite_barbershops/{client_id}")
    fun getFavoriteBarbershops(@Path("client_id") clientId: Int): Call<List<BarbershopResponse>>

    @POST("/api/barbershops/add_favorite")
    fun addFavorite(@Body body: Map<String, Int>): Call<ApiResponse>

    @HTTP(method = "DELETE", path = "/api/barbershops/remove_favorite", hasBody = true)
    fun removeFavorite(@Body body: Map<String, Int>): Call<ApiResponse>

    @GET("/api/barbershops/get_services/{local_id}")
    fun getServicesByLocalId(@Path("local_id") localId: Int): Call<List<ServiceResponse>>

    @GET("/api/barbershops/get_barbershop_reviews/{local_id}")
    fun getBarbershopReviews(@Path("local_id") localId: Int): Call<List<ReviewResponse>>
}