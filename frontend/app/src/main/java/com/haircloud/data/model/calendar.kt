package com.haircloud.data.model

data class WeeklyScheduleResponse(
    val diasemana: String,
    val horainicio: String,
    val horafin: String
)

data class BlockedHoursResponse(
    val fecha: String,
    val horainicio: String,
    val horafin: String,
    val motivo: String?
)

data class DateResponse(
    val citaid: Int,
    val fechainicio: String,
    val fechafin: String,
    val estado: String
)

data class AvailableSlot(
    val desde: String,
    val hasta: String
)