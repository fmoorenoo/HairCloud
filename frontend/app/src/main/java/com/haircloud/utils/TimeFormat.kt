package com.haircloud.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Convertir fecha a formato de hora (12:30)
fun formatTime(dateTimeString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) {
                dateTimeString
            }
        }
    }
}

// Convertir hora a formato sin segundos (12:30)
fun formatHour(time: String): String {
    return time.trim().take(5)
}

// Convertir fecha a formato largo (Lunes 20 de Mayo)
fun formatDateToLong(date: LocalDate): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    val dayOfMonth = date.dayOfMonth
    val month = date.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    return "${dayOfWeek.replaceFirstChar { it.uppercase() }} $dayOfMonth de ${month.lowercase()}"
}

fun Double.formatCurrency(locale: Locale = Locale("es", "ES")): String {
    val format = NumberFormat.getCurrencyInstance(locale)
    return format.format(this)
}