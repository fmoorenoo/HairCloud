package com.haircloud.data.model

data class BarbershopResponse(
    val localid: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val horarioapertura: String,
    val horariocierre: String,
    val descripcion: String?,
    val puntos_habilitados: Boolean,
    val localidad: String,
    val rating: Float?,
    val cantidad_resenas: Int,
    val es_favorito: Boolean,
    val cantidad_puntos: Int?,
)

data class ServiceResponse(
    val servicioid: Int,
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double,
    val localid: Int
)
