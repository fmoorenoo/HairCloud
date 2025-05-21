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

data class BarberActivityResponse(
    val actividadid: Int,
    val tipo: String,
    val fecha: String,
    val cliente_nombre: String,
    val fechainicio: String,
    val fechafin: String,
    val estado: String,
    val servicio_nombre: String,
    val local_nombre: String
)

data class BarberStatsResponse(
    val total_clientes_atendidos: Int,
    val total_citas: Int,
    val total_canceladas: Int,
    val total_no_completadas: Int,
    val servicio_mas_solicitado: ServicioMasSolicitado?,
    val cliente_mas_frecuente: ClienteFrecuente?,
    val ingresos_totales: Double,
    val promedio_citas_por_dia: Double
)

data class ServicioMasSolicitado(
    val servicioid: Int,
    val nombre: String,
    val cantidad: Int
)

data class ClienteFrecuente(
    val clienteid: Int,
    val nombre: String,
    val total_citas: Int
)


data class SendBarberStatsRequest(
    val peluqueroid: Int,
    val stats: BarberStatsResponse,
    val start_date: String,
    val end_date: String
)

