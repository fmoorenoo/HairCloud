package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.apiCall
import com.haircloud.data.model.BarberResponse
import com.haircloud.data.model.BarbershopResponse
import com.haircloud.data.model.ReviewBody
import com.haircloud.data.model.ReviewResponse
import com.haircloud.data.model.ServiceResponse
import kotlin.math.roundToInt

/**
 * Locales, favoritos, servicios y reseñas. El `clienteId` que reciben algunas
 * funciones ya no se envía: el servidor lo obtiene del token.
 */
@Suppress("UNUSED_PARAMETER")
class BarbershopRepository {
    private val api = ApiClient.instance

    suspend fun getAllBarbershops(clienteId: Int): Result<List<BarbershopResponse>> =
        apiCall("Error al cargar barberías") { api.getAllBarbershops() }

    suspend fun getBarbershopById(clienteId: Int, localId: Int): Result<BarbershopResponse> =
        apiCall("Error al obtener la barbería") { api.getBarbershopById(localId) }

    suspend fun updateBarbershop(localId: Int, updateData: Map<String, String?>): Result<String> =
        apiCall("Error al actualizar la barbería") { api.updateBarbershop(localId, updateData) }
            .map { it.message }

    suspend fun getFavoriteBarbershops(clienteId: Int): Result<List<BarbershopResponse>> =
        apiCall("Error al cargar favoritos") { api.getFavoriteBarbershops() }

    suspend fun addFavorite(clienteId: Int, localId: Int): Result<String> =
        apiCall("Error al agregar favorito") { api.addFavorite(localId) }.map { it.message }

    suspend fun removeFavorite(clienteId: Int, localId: Int): Result<String> =
        apiCall("Error al eliminar favorito") { api.removeFavorite(localId) }.map { it.message }

    suspend fun getServicesById(localId: Int): Result<List<ServiceResponse>> =
        apiCall("Error al cargar servicios") { api.getServicesByLocalId(localId) }

    suspend fun getService(servicioId: Int): Result<ServiceResponse> =
        apiCall("Error al cargar el servicio") { api.getService(servicioId) }

    suspend fun getBarbershopReviews(localId: Int): Result<List<ReviewResponse>> =
        apiCall("Error al cargar reseñas") { api.getBarbershopReviews(localId) }

    suspend fun addReview(clienteId: Int, localId: Int, calificacion: Double, comentario: String): Result<String> =
        apiCall("No se pudo añadir la reseña") {
            api.addReview(localId, ReviewBody(calificacion.roundToInt(), comentario.trim().ifBlank { null }))
        }.map { it.message }

    suspend fun deleteReview(resenaId: Int): Result<String> =
        apiCall("No se pudo eliminar la reseña") { api.deleteReview(resenaId) }.map { it.message }

    suspend fun getBarbersById(localId: Int): Result<List<BarberResponse>> =
        apiCall("Error al cargar peluqueros") { api.getBarbersByLocalId(localId) }
}
