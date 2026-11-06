package com.haircloud.data.model

/** Modelo que construye la UI. [localId] solo se usa para la URL al crear. */
data class ServiceRequest(
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double,
    val localId: Int
)

/** Cuerpo que se envía a la API (el local no se puede cambiar desde el cliente). */
data class ServiceBody(
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double
)

fun ServiceRequest.toBody() = ServiceBody(nombre, descripcion?.ifBlank { null }, duracion, precio)
