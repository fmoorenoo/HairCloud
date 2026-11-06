package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.apiCall
import com.haircloud.data.model.ServiceRequest
import com.haircloud.data.model.ServiceResponse
import com.haircloud.data.model.toBody

class ServicesRepository {
    private val api = ApiClient.instance

    suspend fun getServicesByLocalId(localId: Int): Result<List<ServiceResponse>> =
        apiCall("Error al cargar servicios") { api.getServicesByLocalId(localId) }

    suspend fun getService(servicioId: Int): Result<ServiceResponse> =
        apiCall("Error al cargar el servicio") { api.getService(servicioId) }

    suspend fun createService(service: ServiceRequest): Result<String> =
        apiCall("Error al crear servicio") { api.createService(service.localId, service.toBody()) }
            .map { it.message }

    suspend fun editService(servicioId: Int, service: ServiceRequest): Result<String> =
        apiCall("Error al editar servicio") { api.editService(servicioId, service.toBody()) }
            .map { it.message }

    suspend fun deleteService(servicioId: Int): Result<String> =
        apiCall("Error al eliminar servicio") { api.deleteService(servicioId) }.map { it.message }
}
