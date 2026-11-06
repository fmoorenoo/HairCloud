package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.apiCall
import com.haircloud.data.model.ClientResponse
import com.haircloud.data.model.ClientStatsResponse
import com.haircloud.data.model.Date

/**
 * Datos del cliente autenticado. Los parámetros de id se mantienen por
 * compatibilidad con las pantallas, pero el servidor identifica al cliente por
 * su token: no es posible consultar ni modificar otra cuenta.
 */
class ClientRepository {
    private val api = ApiClient.instance

    @Suppress("UNUSED_PARAMETER")
    suspend fun getClient(usuarioId: Int): Result<ClientResponse> =
        apiCall("Error al obtener el cliente") { api.getClient() }

    @Suppress("UNUSED_PARAMETER")
    suspend fun updateClient(usuarioId: Int, updateData: Map<String, String?>): Result<ApiResponse> =
        apiCall("Error al actualizar el cliente") { api.updateClient(updateData) }

    @Suppress("UNUSED_PARAMETER")
    suspend fun getClientDates(clienteId: Int): Result<List<Date>> =
        apiCall("Error al obtener las citas") { api.getClientDates() }.map { it.appointments }

    @Suppress("UNUSED_PARAMETER")
    suspend fun getClientStats(clienteId: Int): Result<ClientStatsResponse> =
        apiCall("Error al obtener estadísticas del cliente") { api.getClientStats() }
}
