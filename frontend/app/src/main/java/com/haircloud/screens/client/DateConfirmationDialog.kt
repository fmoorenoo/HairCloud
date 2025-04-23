package com.haircloud.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.haircloud.data.model.AvailableSlot
import com.haircloud.data.model.BarberResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.*

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    barber: BarberResponse?,
    serviceName: String,
    servicePrice: Double,
    selectedDate: LocalDate?,
    selectedSlot: AvailableSlot?,
    serviceDuration: Int,
    defaultFont: FontFamily
) {
    if (!show || barber == null || selectedDate == null || selectedSlot == null) return

    val startTime = try {
        LocalTime.parse(selectedSlot.desde, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        LocalTime.of(0, 0)
    }

    val endTime = startTime.plusMinutes(serviceDuration.toLong())
    val endTimeFormatted = endTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    val dayName = selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }
    val monthName = selectedDate.month.getDisplayName(JavaTextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }
    val dayOfMonth = selectedDate.dayOfMonth

    val dialogGradient = Brush.verticalGradient(colors = listOf(Color(0xFF2A2A2A), Color(0xFF3F474D)))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = dialogGradient)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Confirmar Cita",
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                InfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Peluquero:",
                    value = barber.nombre,
                    defaultFont = defaultFont
                )

                InfoRow(
                    icon = Icons.Filled.ContentCut,
                    label = "Servicio:",
                    value = serviceName,
                    defaultFont = defaultFont
                )

                InfoRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Fecha:",
                    value = "$dayName $dayOfMonth de $monthName",
                    defaultFont = defaultFont
                )

                InfoRow(
                    icon = Icons.Outlined.AccessTime,
                    label = "Horario:",
                    value = "${selectedSlot.desde} - $endTimeFormatted",
                    defaultFont = defaultFont
                )

                InfoRow(
                    icon = Icons.Filled.Wallet,
                    label = "Precio:",
                    value = "$servicePrice €",
                    defaultFont = defaultFont
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color.LightGray)
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Cancelar",
                            style = TextStyle(fontFamily = defaultFont),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5AB641)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Confirmar",
                            style = TextStyle(fontFamily = defaultFont),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    defaultFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            style = TextStyle(fontFamily = defaultFont),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = value,
            color = Color.White,
            style = TextStyle(fontFamily = defaultFont),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}