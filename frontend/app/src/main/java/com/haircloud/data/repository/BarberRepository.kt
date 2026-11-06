package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.apiCall
import com.haircloud.data.model.BarberActiveBody
import com.haircloud.data.model.BarberActivityResponse
import com.haircloud.data.model.BarberDate
import com.haircloud.data.model.BarberRoleBody
import com.haircloud.data.model.BarberStatsResponse
import com.haircloud.data.model.CreateBarberRequest
import com.haircloud.data.model.GetBarberResponse
import com.haircloud.data.model.InactiveBarberResponse
import com.haircloud.data.model.ScheduleBody
import com.haircloud.data.model.SendBarberStatsRequest
import com.haircloud.data.model.WorkDaySchedule

class BarberRepository {
    private val api = ApiClient.instance

    /** Perfil del peluquero autenticado (el servidor lo identifica por el token). */
    @Suppress("UNUSED_PARAMETER")
    suspend fun getBarber(usuarioId: Int): Result<GetBarberResponse> =
        apiCall("Error al obtener el barbero") { api.getBarber() }

    suspend fun getBarberDates(barberId: Int, date: String): Result<List<BarberDate>> =
        apiCall("Error al obtener las citas del barbero") { api.getBarberDates(barberId, date) }
            .map { it.dates }

    suspend fun getBarberDatesInRange(barberId: Int, startDate: String, endDate: String): Result<List<BarberDate>> =
        apiCall("Error al obtener las citas por rango") { api.getBarberDatesInRange(barberId, startDate, endDate) }
            .map { it.dates }

    @Suppress("UNUSED_PARAMETER")
    suspend fun updateBarber(usuarioId: Int, updateData: Map<String, String?>): Result<ApiResponse> =
        apiCall("Error al actualizar el barbero") { api.updateBarber(updateData) }

    suspend fun setBarberRole(peluqueroId: Int, rol: String): Result<ApiResponse> =
        apiCall("Error al cambiar el rol") { api.setBarberRole(peluqueroId, BarberRoleBody(rol)) }

    suspend fun deactivateBarber(peluqueroId: Int): Result<ApiResponse> =
        apiCall("Error al desactivar el barbero") { api.setBarberActive(peluqueroId, BarberActiveBody(false)) }

    suspend fun activateBarber(peluqueroId: Int): Result<ApiResponse> =
        apiCall("Error al activar el barbero") { api.setBarberActive(peluqueroId, BarberActiveBody(true)) }

    suspend fun getInactiveBarbers(localId: Int): Result<List<InactiveBarberResponse>> =
        apiCall("Error al obtener barberos inactivos") { api.getInactiveBarbers(localId) }

    suspend fun createBarber(localId: Int, request: CreateBarberRequest): Result<ApiResponse> =
        apiCall("Error al crear barbero") { api.createBarber(localId, request) }

    suspend fun updateBarberSchedule(peluqueroId: Int, schedule: List<WorkDaySchedule>): Result<ApiResponse> =
        apiCall("Error al actualizar horario") { api.updateBarberSchedule(peluqueroId, ScheduleBody(schedule)) }

    suspend fun getBarberActivity(peluqueroId: Int): Result<List<BarberActivityResponse>> =
        apiCall("Error al obtener la actividad del peluquero") { api.getBarberActivity(peluqueroId) }

    /** El local se deduce en el servidor; [localId] se ignora. */
    @Suppress("UNUSED_PARAMETER")
    suspend fun getBarberStats(peluqueroId: Int, localId: Int, start: String, end: String): Result<BarberStatsResponse> =
        apiCall("Error al obtener estadísticas") { api.getBarberStats(peluqueroId, start, end) }

    /** Las estadísticas se recalculan en el servidor; solo se envía el rango de fechas. */
    suspend fun sendBarberStatsEmail(peluqueroid: Int, startDate: String, endDate: String): Result<ApiResponse> =
        apiCall("Error al enviar estadísticas") {
            api.sendBarberStats(peluqueroid, SendBarberStatsRequest(startDate, endDate))
        }
}
