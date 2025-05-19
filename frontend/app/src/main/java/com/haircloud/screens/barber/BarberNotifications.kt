package com.haircloud.screens.barber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.BarberActivityResponse
import com.haircloud.utils.formatTime
import com.haircloud.viewmodel.BarberActivityState
import com.haircloud.viewmodel.BarberViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFBBBBBB)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarberNotifications(
    navController: NavController,
    peluqueroId: Int,
) {
    val barberViewModel: BarberViewModel = viewModel()
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    val activityState by barberViewModel.barberActivityState.collectAsState()
    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))

    LaunchedEffect(peluqueroId) {
        barberViewModel.getBarberActivity(peluqueroId)
    }

    Scaffold(
        modifier = Modifier
            .background(brush = blackWhiteGradient)
            .fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = blackWhiteGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(55.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(45.dp)
                        )
                    }

                    Text(
                        text = "Mi Actividad",
                        color = TextPrimary,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Chat",
                        tint = TextPrimary,
                        modifier = Modifier.size(45.dp)
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    VerticalDivider(
                        color = Color(0xFF3D8EE6),
                        modifier = Modifier
                            .width(8.dp)
                            .height(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Últimos 7 días",
                        color = TextSecondary,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activityState) {
                    is BarberActivityState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is BarberActivityState.Error -> {
                        val errorMessage = (activityState as BarberActivityState.Error).message
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Error al cargar actividad",
                                    color = TextPrimary,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = errorMessage,
                                    color = TextSecondary,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { barberViewModel.getBarberActivity(peluqueroId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3D8EE6)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Reintentar",
                                        style = TextStyle(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                    is BarberActivityState.Success -> {
                        val activities = (activityState as BarberActivityState.Success).actividades

                        if (activities.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay actividad reciente", color = TextPrimary)
                            }
                        } else {
                            val activitiesByDate = mutableMapOf<LocalDate, MutableList<Pair<BarberActivityResponse, LocalDateTime>>>()

                            activities.forEach { activity ->
                                val dateTime = parseFechaActividad(activity.fecha)
                                if (dateTime != null) {
                                    val date = dateTime.toLocalDate()
                                    activitiesByDate.getOrPut(date) { mutableListOf() }.add(activity to dateTime)
                                }
                            }

                            val sortedDates = activitiesByDate.keys.sortedBy { it }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (date in sortedDates) {
                                    val dateActivities = activitiesByDate[date] ?: continue

                                    item {
                                        DateHeader(date)
                                    }

                                    val sortedActivities = dateActivities.sortedBy { it.second }

                                    items(sortedActivities) { (activity, _) ->
                                        ActivityItem(activity = activity)
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    else -> { }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM")
    val locale = Locale("es", "ES")
    val formattedDate = date.format(formatter.withLocale(locale)).replaceFirstChar { it.uppercase() }

    val isToday = date == LocalDate.now()
    val headerColor = if (isToday) Color(0xFF3D8EE6) else TextSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        HorizontalDivider(
            color = headerColor.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isToday) Color(0xFF3D8EE6).copy(alpha = 0.2f) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isToday) "Hoy, $formattedDate" else formattedDate,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(
            color = headerColor.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActivityItem(activity: BarberActivityResponse) {
    val isReserva = activity.tipo == "Reserva"

    val backgroundColor = Color(0xFF2A2A2A)

    val borderColor = when {
        isReserva -> Color(0xFF3DB43F).copy(alpha = 0.3f)
        else -> Color(0xFF914243).copy(alpha = 0.3f)
    }

    val iconTint = when {
        isReserva -> Color(0xFF3DB43F)
        else -> Color(0xFF914243)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 340.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isReserva) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.HighlightOff,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = if (isReserva) "Nueva Reserva" else "Cita Cancelada",
                        color = iconTint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatTime(activity.fecha),
                        color = TextSecondary,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    color = borderColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val formattedDate = formatDate(activity.fechainicio)

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val texto = when {
                        isReserva -> "${activity.cliente_nombre} ha reservado una cita para ${activity.servicio_nombre} el $formattedDate"
                        else -> "${activity.cliente_nombre} ha cancelado su cita para ${activity.servicio_nombre} el $formattedDate"
                    }

                    Text(
                        text = texto,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun formatDate(dateTimeString: String): String {
    val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (_: Exception) {
        try {
            val alternateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(dateTimeString, alternateFormatter)
            dateTime.format(outputFormatter)
        } catch (_: Exception) {
            dateTimeString
        }
    }
}


private fun parseFechaActividad(fecha: String): LocalDateTime? {
    val formatos = listOf(
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    )

    for (formato in formatos) {
        try {
            return LocalDateTime.parse(fecha, formato)
        } catch (_: Exception) {
            continue
        }
    }
    return null
}
