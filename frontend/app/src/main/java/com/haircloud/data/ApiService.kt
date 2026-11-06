package com.haircloud.data

import com.haircloud.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Respuesta genérica de las operaciones de escritura. */
data class ApiResponse(val message: String, val username: String? = null)

private const val NO_AUTH = "${ApiClient.NO_AUTH_HEADER}: true"

/**
 * API REST v1. Las rutas son relativas a `BuildConfig.API_BASE_URL` (…/api/v1/).
 *
 * La identidad del usuario la determina el servidor a partir del token: ya
 * no se envían `clienteid`/`usuarioid` en URLs ni cuerpos.
 */
interface ApiService {
    // ---- Autenticación ----------------------------------------------------
    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<ApiResponse>

    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/verification-codes")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): Response<ApiResponse>

    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/verification-codes/verify")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<VerifyCodeResponse>

    @retrofit2.http.Headers(NO_AUTH)
    @POST("auth/password-reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    // ---- Cliente autenticado ---------------------------------------------
    @GET("me/client")
    suspend fun getClient(): Response<ClientResponse>

    @PATCH("me/client")
    suspend fun updateClient(@Body updateData: Map<String, String?>): Response<ApiResponse>

    @GET("me/appointments")
    suspend fun getClientDates(): Response<DatesResponse>

    @GET("me/stats")
    suspend fun getClientStats(): Response<ClientStatsResponse>

    @GET("me/favorites")
    suspend fun getFavoriteBarbershops(): Response<List<BarbershopResponse>>

    @PUT("me/favorites/{local_id}")
    suspend fun addFavorite(@Path("local_id") localId: Int): Response<ApiResponse>

    @DELETE("me/favorites/{local_id}")
    suspend fun removeFavorite(@Path("local_id") localId: Int): Response<ApiResponse>

    // ---- Personal autenticado --------------------------------------------
    @GET("me/barber")
    suspend fun getBarber(): Response<GetBarberResponse>

    @PATCH("me/barber")
    suspend fun updateBarber(@Body updateData: Map<String, String?>): Response<ApiResponse>

    // ---- Locales ---------------------------------------------------------
    @GET("barbershops")
    suspend fun getAllBarbershops(): Response<List<BarbershopResponse>>

    @GET("barbershops/{local_id}")
    suspend fun getBarbershopById(@Path("local_id") localId: Int): Response<BarbershopResponse>

    @PATCH("barbershops/{local_id}")
    suspend fun updateBarbershop(
        @Path("local_id") localId: Int,
        @Body updateData: Map<String, String?>
    ): Response<ApiResponse>

    @GET("barbershops/{local_id}/services")
    suspend fun getServicesByLocalId(@Path("local_id") localId: Int): Response<List<ServiceResponse>>

    @POST("barbershops/{local_id}/services")
    suspend fun createService(@Path("local_id") localId: Int, @Body service: ServiceBody): Response<ApiResponse>

    @GET("barbershops/{local_id}/reviews")
    suspend fun getBarbershopReviews(@Path("local_id") localId: Int): Response<List<ReviewResponse>>

    @POST("barbershops/{local_id}/reviews")
    suspend fun addReview(@Path("local_id") localId: Int, @Body body: ReviewBody): Response<ReviewCreatedResponse>

    @GET("barbershops/{local_id}/barbers")
    suspend fun getBarbersByLocalId(@Path("local_id") localId: Int): Response<List<BarberResponse>>

    @GET("barbershops/{local_id}/barbers/inactive")
    suspend fun getInactiveBarbers(@Path("local_id") localId: Int): Response<List<InactiveBarberResponse>>

    @POST("barbershops/{local_id}/barbers")
    suspend fun createBarber(@Path("local_id") localId: Int, @Body request: CreateBarberRequest): Response<ApiResponse>

    // ---- Servicios y reseñas ---------------------------------------------
    @GET("services/{servicio_id}")
    suspend fun getService(@Path("servicio_id") servicioId: Int): Response<ServiceResponse>

    @PUT("services/{servicio_id}")
    suspend fun editService(@Path("servicio_id") servicioId: Int, @Body service: ServiceBody): Response<ApiResponse>

    @DELETE("services/{servicio_id}")
    suspend fun deleteService(@Path("servicio_id") servicioId: Int): Response<ApiResponse>

    @DELETE("reviews/{resena_id}")
    suspend fun deleteReview(@Path("resena_id") resenaId: Int): Response<ApiResponse>

    // ---- Peluqueros ------------------------------------------------------
    @GET("barbers/{peluquero_id}/schedule")
    suspend fun getWeeklySchedule(@Path("peluquero_id") peluqueroId: Int): Response<List<WeeklyScheduleResponse>>

    @PUT("barbers/{peluquero_id}/schedule")
    suspend fun updateBarberSchedule(
        @Path("peluquero_id") peluqueroId: Int,
        @Body schedule: ScheduleBody
    ): Response<ApiResponse>

    @GET("barbers/{peluquero_id}/blocked-hours")
    suspend fun getBlockedHours(@Path("peluquero_id") peluqueroId: Int): Response<List<BlockedHoursResponse>>

    @GET("barbers/{peluquero_id}/availability")
    suspend fun getAvailableSlots(
        @Path("peluquero_id") peluqueroId: Int,
        @Query("fecha") fecha: String,
        @Query("servicioid") servicioId: Int
    ): Response<List<AvailableSlot>>

    @GET("barbers/{peluquero_id}/appointments")
    suspend fun getBarberDates(
        @Path("peluquero_id") barberId: Int,
        @Query("date") date: String
    ): Response<BarberDatesResponse>

    @GET("barbers/{peluquero_id}/appointments")
    suspend fun getBarberDatesInRange(
        @Path("peluquero_id") barberId: Int,
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): Response<BarberDatesResponse>

    @GET("barbers/{peluquero_id}/activity")
    suspend fun getBarberActivity(@Path("peluquero_id") peluqueroId: Int): Response<List<BarberActivityResponse>>

    @GET("barbers/{peluquero_id}/stats")
    suspend fun getBarberStats(
        @Path("peluquero_id") peluqueroId: Int,
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): Response<BarberStatsResponse>

    @POST("barbers/{peluquero_id}/stats/email")
    suspend fun sendBarberStats(
        @Path("peluquero_id") peluqueroId: Int,
        @Body request: SendBarberStatsRequest
    ): Response<ApiResponse>

    @PATCH("barbers/{peluquero_id}/role")
    suspend fun setBarberRole(@Path("peluquero_id") peluqueroId: Int, @Body body: BarberRoleBody): Response<ApiResponse>

    @PATCH("barbers/{peluquero_id}/active")
    suspend fun setBarberActive(@Path("peluquero_id") peluqueroId: Int, @Body body: BarberActiveBody): Response<ApiResponse>

    // ---- Citas -----------------------------------------------------------
    @POST("appointments")
    suspend fun addDate(@Body cita: AddDateRequest): Response<AddDateResponse>

    @PATCH("appointments/{cita_id}/status")
    suspend fun updateDateEstado(
        @Path("cita_id") citaId: Int,
        @Body body: UpdateDateStatusRequest
    ): Response<ApiResponse>
}
