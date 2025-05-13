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

    @PUT("/api/barbershops/update_barbershop/{local_id}")
    fun updateBarbershop(
        @Path("local_id") localId: Int,
        @Body updateData: Map<String, String?>
    ): Call<ApiResponse>

    @GET("/api/barbershops/get_favorite_barbershops/{client_id}")
    fun getFavoriteBarbershops(@Path("client_id") clientId: Int): Call<List<BarbershopResponse>>

    @POST("/api/barbershops/add_favorite")
    fun addFavorite(@Body body: Map<String, Int>): Call<ApiResponse>

    @HTTP(method = "DELETE", path = "/api/barbershops/remove_favorite", hasBody = true)
    fun removeFavorite(@Body body: Map<String, Int>): Call<ApiResponse>

    @GET("/api/services/get_services/{local_id}")
    fun getServicesByLocalId(@Path("local_id") localId: Int): Call<List<ServiceResponse>>

    @GET("/api/services/get_service/{servicioid}")
    fun getService(@Path("servicioid") servicioId: Int): Call<ServiceResponse>

    @POST("/api/services/create_service")
    fun createService(@Body service: ServiceRequest): Call<ApiResponse>

    @PUT("/api/services/edit_service/{servicioid}")
    fun editService(
        @Path("servicioid") servicioId: Int,
        @Body service: ServiceRequest
    ): Call<ApiResponse>

    @HTTP(method = "DELETE", path = "/api/services/delete_service/{servicioid}", hasBody = false)
    fun deleteService(@Path("servicioid") servicioId: Int): Call<ApiResponse>

    @GET("/api/barbershops/get_barbershop_reviews/{local_id}")
    fun getBarbershopReviews(@Path("local_id") localId: Int): Call<List<ReviewResponse>>

    @POST("/api/barbershops/add_review")
    fun addReview(@Body body: ReviewRequest): Call<ApiResponse>

    @HTTP(method = "DELETE", path = "/api/barbershops/delete_review/{resenaid}", hasBody = false)
    fun deleteReview(@Path("resenaid") resenaId: Int): Call<ApiResponse>

    @GET("/api/barbershops/get_barbers/{local_id}")
    fun getBarbersByLocalId(@Path("local_id") localId: Int): Call<List<BarberResponse>>

    @GET("/api/calendar/get_weekly_schedule/{peluqueroid}")
    fun getWeeklySchedule(@Path("peluqueroid") peluqueroId: Int): Call<List<WeeklyScheduleResponse>>

    @GET("/api/calendar/get_blocked_hours/{peluqueroid}")
    fun getBlockedHours(@Path("peluqueroid") peluqueroId: Int): Call<List<BlockedHoursResponse>>

    @GET("/api/calendar/get_barber_dates/{peluqueroid}")
    fun getBarberDates(@Path("peluqueroid") peluqueroId: Int): Call<List<DateResponse>>

    @GET("/api/calendar/get_available_slots/{peluqueroid}")
    fun getAvailableSlots(
        @Path("peluqueroid") peluqueroId: Int,
        @retrofit2.http.Query("fecha") fecha: String,
        @retrofit2.http.Query("duracion") duracion: Int
    ): Call<List<AvailableSlot>>

    @POST("/api/dates/add_date")
    fun addDate(@Body cita: AddDateRequest): Call<ApiResponse>

    @HTTP(method = "DELETE", path = "/api/dates/delete_date/{citaid}", hasBody = false)
    fun deleteDate(@Path("citaid") citaId: Int): Call<ApiResponse>

    @PUT("/api/dates/update_date/{citaid}")
    fun updateDateEstado(
        @Path("citaid") citaId: Int,
        @Body estado: Map<String, String>
    ): Call<ApiResponse>

    @GET("/api/clients/get_client_dates/{client_id}")
    fun getClientDates(@Path("client_id") clientId: Int): Call<DatesResponse>

    @GET("/api/clients/get_client_stats/{client_id}")
    fun getClientStats(@Path("client_id") clientId: Int): Call<ClientStatsResponse>

    @GET("/api/barbers/get_barber/{user_id}")
    fun getBarber(@Path("user_id") userId: Int): Call<GetBarberResponse>

    @GET("/api/barbers/get_barber_dates/{barber_id}")
    fun getBarberDates(
        @Path("barber_id") barberId: Int,
        @retrofit2.http.Query("date") date: String
    ): Call<BarberDatesResponse>

    @GET("/api/barbers/get_barber_dates/{barber_id}")
    fun getBarberDatesInRange(
        @Path("barber_id") barberId: Int,
        @retrofit2.http.Query("start") startDate: String,
        @retrofit2.http.Query("end") endDate: String
    ): Call<BarberDatesResponse>

    @PUT("/api/barbers/update_barber/{user_id}")
    fun updateBarber(
        @Path("user_id") userId: Int,
        @Body updateData: Map<String, String?>
    ): Call<ApiResponse>

    @PUT("/api/barbers/toggle_barber_role/{user_id}")
    fun toggleBarberRole(@Path("user_id") userId: Int): Call<ApiResponse>

    @PUT("/api/barbers/deactivate_barber/{user_id}")
    fun deactivateBarber(@Path("user_id") userId: Int): Call<ApiResponse>

    @PUT("/api/barbers/activate_barber/{user_id}")
    fun activateBarber(@Path("user_id") userId: Int): Call<ApiResponse>

    @GET("/api/barbers/get_inactive_barbers")
    fun getInactiveBarbers(): Call<List<InactiveBarberResponse>>

    @POST("/api/barbers/create_barber")
    fun createBarber(@Body request: CreateBarberRequest): Call<ApiResponse>

    @PUT("/api/barbers/update_barber_schedule/{peluquero_id}")
    fun updateBarberSchedule(
        @Path("peluquero_id") peluqueroId: Int,
        @Body schedule: List<WorkDaySchedule>
    ): Call<ApiResponse>
}