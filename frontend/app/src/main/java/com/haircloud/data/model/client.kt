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

data class ClientStatsResponse(
    val total_citas_finalizadas: Int,
    val local_mas_frecuentado: String?,
    val local_mas_frecuentado_visitas: Int,
    val proxima_cita: DateDetails?,
    val servicio_favorito: String?,
    val servicio_favorito_local: String?
)

data class DateDetails(
    val citaid: Int,
    val fechainicio: String,
    val fechafin: String?,
    val servicio_nombre: String,
    val peluquero_nombre: String,
    val local_nombre: String
)
