package com.haircloud.data.model

data class ClientResponse(
    val clienteid: Int,
    val usuarioid: Int,
    val nombre: String,
    val telefono: String?,
    val fecharegistro: String,
    val ultimacita: String?,
    val email: String,
    val nombreusuario: String
)