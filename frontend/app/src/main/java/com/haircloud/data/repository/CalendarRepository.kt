package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.apiCall
import com.haircloud.data.model.AvailableSlot
import com.haircloud.data.model.BlockedHoursResponse
import com.haircloud.data.model.WeeklyScheduleResponse

class CalendarRepository {
    private val api = ApiClient.instance

    suspend fun getWeeklyCalendar(peluqueroId: Int): Result<List<WeeklyScheduleResponse>> =
        apiCall("Error al cargar el horario") { api.getWeeklySchedule(peluqueroId) }

    suspend fun getBlockedHours(peluqueroId: Int): Result<List<BlockedHoursResponse>> =
        apiCall("Error al cargar los bloqueos") { api.getBlockedHours(peluqueroId) }

    /** La duración la determina el servidor a partir del servicio. */
    suspend fun getAvailableSlots(peluqueroId: Int, fecha: String, servicioId: Int): Result<List<AvailableSlot>> =
        apiCall("Error al cargar la disponibilidad") { api.getAvailableSlots(peluqueroId, fecha, servicioId) }
}
