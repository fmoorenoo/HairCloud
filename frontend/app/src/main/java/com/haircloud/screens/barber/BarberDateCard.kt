package com.haircloud.screens.barber

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.haircloud.data.model.BarberDate
import com.haircloud.utils.formatTime
import com.haircloud.viewmodel.BarberDatesState
import java.text.NumberFormat
import java.util.*

@Composable
fun BarberAppointmentsList(
    barberDatesState: BarberDatesState,
    defaultFont: FontFamily
) {
    when (barberDatesState) {
        is BarberDatesState.Loading -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is BarberDatesState.Success -> {
            val citas = barberDatesState.citas
            if (citas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay citas para este día",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(citas) { cita ->
                        BarberAppointmentCard(cita = cita, defaultFont = defaultFont)
                    }
                }
            }
        }
        is BarberDatesState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error al cargar citas",
                    color = Color.Red,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp
                )
            }
        }
        else -> {}
    }
}

@Composable
fun BarberAppointmentCard(
    cita: BarberDate,
    defaultFont: FontFamily
) {
    var showDialog by remember { mutableStateOf(false) }

    val timeRange = remember(cita.fechainicio, cita.fechafin) {
        val startTime = cita.fechainicio.substringAfterLast(" ", "")
        val endTime = cita.fechafin.substringAfterLast(" ", "")
        "$startTime - $endTime"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = timeRange,
                    fontFamily = defaultFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = cita.servicio_nombre.uppercase(),
                    fontFamily = defaultFont,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = cita.cliente_nombre,
                    fontFamily = defaultFont,
                    fontSize = 14.sp
                )
            }

            cita.estado?.let { estado ->
                Box(
                    modifier = Modifier
                        .background(
                            when (estado.lowercase()) {
                                "confirmado" -> Color(0xFF4CAF50)
                                "pendiente" -> Color(0xFFFFC107)
                                "cancelado" -> Color(0xFFF44336)
                                else -> Color(0xFF9E9E9E)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = estado,
                        fontFamily = defaultFont,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showDialog) {
        AppointmentDetailDialog(
            cita = cita,
            onDismiss = { showDialog = false },
            defaultFont = defaultFont
        )
    }
}

@Composable
fun AppointmentDetailDialog(
    cita: BarberDate,
    onDismiss: () -> Unit,
    defaultFont: FontFamily
) {
    val priceFormat = remember {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("EUR")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF212121)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles de la cita",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                DetailItem(
                    icon = Icons.Default.AccessTime,
                    title = "Horario",
                    value = "${formatTime(cita.fechainicio)} - ${formatTime(cita.fechafin)}",
                    defaultFont = defaultFont
                )

                DetailSection(
                    title = "Servicio",
                    defaultFont = defaultFont
                ) {
                    Text(
                        text = cita.servicio_nombre,
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Duración: ${cita.duracion} min",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 14.sp
                        )

                        Text(
                            text = priceFormat.format(cita.precio),
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                DetailSection(
                    title = "Cliente",
                    defaultFont = defaultFont
                ) {
                    DetailItem(
                        icon = Icons.Default.Person,
                        title = "Nombre",
                        value = cita.cliente_nombre,
                        defaultFont = defaultFont
                    )

                    DetailItem(
                        icon = Icons.Default.Smartphone,
                        title = "Teléfono",
                        value = (cita.cliente_telefono)?: "",
                        defaultFont = defaultFont
                    )
                }

                DetailSection(
                    title = "Estado",
                    defaultFont = defaultFont
                ) {
                    val estadoColor = when (cita.estado?.lowercase()) {
                        "completada" -> Color(0xFF4CAF50)
                        "cancelada" -> Color(0xFFFF5252)
                        "pendiente" -> Color(0xFFFFB74D)
                        else -> Color.Gray
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(estadoColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cita.estado ?: "Desconocido",
                            color = estadoColor,
                            style = TextStyle(fontFamily = defaultFont),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Acción de cancelar */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cancelar cita",
                            style = TextStyle(fontFamily = defaultFont)
                        )
                    }

                    Button(
                        onClick = { /* Acción de completar */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Completar",
                            style = TextStyle(fontFamily = defaultFont)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    defaultFont: FontFamily,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.Gray,
            style = TextStyle(fontFamily = defaultFont),
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    defaultFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = Color.Gray,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 12.sp
                )
            }

            Text(
                text = value,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp
            )
        }
    }
}