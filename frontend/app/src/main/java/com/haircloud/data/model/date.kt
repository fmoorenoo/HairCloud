package com.haircloud.data.model

data class AddDateRequest(
    val clienteid: Int,
    val peluqueroid: Int,
    val servicioid: Int,
    val localid: Int,
    val fechainicio: String,
    val fechafin: String
)