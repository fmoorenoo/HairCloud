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
    val cantidad_resenas: Int
)
