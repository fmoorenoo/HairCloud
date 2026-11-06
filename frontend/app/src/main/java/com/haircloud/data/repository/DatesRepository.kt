package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.apiCall
import com.haircloud.data.model.AddDateRequest
import com.haircloud.data.model.UpdateDateStatusRequest

class DatesRepository {
    private val api = ApiClient.instance

    /** Reserva una cita. El servidor valida horario y solapes y envía la confirmación por email. */
    suspend fun addDate(request: AddDateRequest): Result<ApiResponse> =
        apiCall("Error al añadir la cita") { api.addDate(request) }
            .map { ApiResponse(it.message) }

    /** Cancela una cita (las citas ya no se borran: se conserva el historial). */
    suspend fun deleteDate(citaId: Int): Result<ApiResponse> =
        updateDateEstado(citaId, "Cancelada", null)

    suspend fun updateDateEstado(citaId: Int, estado: String, motivo: String? = null): Result<ApiResponse> =
        apiCall("Error al actualizar estado") {
            api.updateDateEstado(citaId, UpdateDateStatusRequest(estado, motivo?.trim()?.ifBlank { null }))
        }
}
