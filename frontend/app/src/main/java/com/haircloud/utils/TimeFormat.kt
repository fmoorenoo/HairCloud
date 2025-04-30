package com.haircloud.utils


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Cadena de fecha y hora en formato ISO (2023-05-20T14:30:00) a formato de hora (14:30)
fun formatTime(dateTimeString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        try {
            val dateTime = LocalDateTime.parse(dateTimeString)
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            dateTimeString
        }
    }
}