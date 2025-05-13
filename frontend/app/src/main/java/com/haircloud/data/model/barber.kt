package com.haircloud.data.model

data class GetBarberResponse(
    val peluqueroid: Int,
    val usuarioid: Int,
    val nombre: String,
    val telefono: String?,
    val especialidad: String?,
    val fechacontratacion: String?,
    val localid: Int,
    val email: String,
    val nombreusuario: String,
    val rol: String
)

data class BarberDate(
    val citaid: Int,
    val clienteid: Int,
    val peluqueroid: Int,
    val servicioid: Int,
    val fechainicio: String,
    val fechafin: String,
    val estado: String?,
    val localid: Int,
    val servicio_nombre: String,
    val duracion: Int,
    val precio: Double,
    val cliente_nombre: String,
    val cliente_telefono: String?,
    val finalizada: Boolean
)

data class BarberDatesResponse(
    val dates: List<BarberDate>
)

data class WorkDaySchedule(
    val dia: String,
    val inicio: String,
    val fin: String
)

data class CreateBarberRequest(
    val nombreusuario: String,
    val contrasena: String,
    val email: String,
    val nombre: String,
    val especialidad: String?,
    val localid: Int,
    val horario: List<WorkDaySchedule>
)

