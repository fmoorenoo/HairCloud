package com.haircloud.data.model

data class ServiceRequest(
    val nombre: String,
    val descripcion: String?,
    val duracion: Int,
    val precio: Double,
    val localId: Int
)
