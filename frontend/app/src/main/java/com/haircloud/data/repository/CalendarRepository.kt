package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarRepository {
    private val api = ApiClient.instance

    suspend fun getWeeklyCalendar(peluqueroId: Int): Result<List<WeeklyScheduleResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getWeeklySchedule(peluqueroId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener el calendario semanal"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getBlockedHours(peluqueroId: Int): Result<List<BlockedHoursResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBlockedHours(peluqueroId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las horas bloqueadas"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getBarberDates(peluqueroId: Int): Result<List<DateResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarberDates(peluqueroId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las citas del peluquero"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getAvailableSlots(peluqueroId: Int, fecha: String, duracion: Int): Result<List<AvailableSlot>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getAvailableSlots(peluqueroId, fecha, duracion).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener huecos disponibles"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }
}
