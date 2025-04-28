package com.haircloud.data.model

data class AddDateRequest(
    val clienteid: Int,
    val peluqueroid: Int,
    val servicioid: Int,
    val localid: Int,
    val fechainicio: String,
    val fechafin: String
)

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