package com.haircloud.data.model

data class BarbershopResponse(
    val localid: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val horarioapertura: String,
    val horariocierre: String,
    val descripcion: String?,
    val localidad: String,
    val rating: Float?,
    val cantidad_resenas: Int,
    val es_favorito: Boolean,
    val imagen_url: String?
)

data class ServiceResponse(
    val servicioid: Int,
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double,
    val localid: Int
)

data class ReviewResponse(
    val resenaid: Int,
    val clienteid: Int,
    val peluqueroid: Int?,
    val calificacion: Int,
    val comentario: String?,
    val fecharesena: String,
    val localid: Int,
    val cliente_nombre: String?
)

data class ReviewRequest(
    val clienteid: Int,
    val localid: Int,
    val calificacion: Double,
    val comentario: String
)

data class BarberResponse(
    val peluqueroid: Int,
    val usuarioid: Int,
    val nombre: String,
    val telefono: String?,
    val especialidad: String?,
    val fechacontratacion: String,
    val localid: Int,
    val rol: String,
)