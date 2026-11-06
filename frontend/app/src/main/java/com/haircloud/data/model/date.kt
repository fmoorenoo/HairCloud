package com.haircloud.data.model

/**
 * Petición de reserva. El cliente, el local y la hora de fin los determina el
 * servidor (antes los enviaba la app y se podían falsificar).
 */
data class AddDateRequest(
    val peluqueroid: Int,
    val servicioid: Int,
    val fechainicio: String
)

data class AddDateResponse(
    val message: String,
    val citaid: Int,
    val fechainicio: String,
    val fechafin: String,
    val localid: Int,
    val estado: String
)

data class UpdateDateStatusRequest(val estado: String, val motivo: String?)

data class Date(
    val citaid: Int,
    val clienteid: Int,
    val peluqueroid: Int,
    val servicioid: Int,
    val fechainicio: String,
    val fechafin: String,
    val estado: String?,
    val localid: Int,
    val servicio_nombre: String?,
    val servicio_precio: Double,
    val servicio_duracion: Int,
    val barber_nombre: String?,
    val local_nombre: String?,
    val local_direccion: String?,
    val finalizada: Boolean
)

data class DatesResponse(
    val appointments: List<Date>
)
