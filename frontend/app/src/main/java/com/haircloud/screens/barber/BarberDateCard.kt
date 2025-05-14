package com.haircloud.screens.barber

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haircloud.data.model.BarberDate
import com.haircloud.utils.formatTime
import com.haircloud.viewmodel.BarberDatesState
import com.haircloud.viewmodel.DateOperationState
import com.haircloud.viewmodel.DatesViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@Composable
fun BarberAppointmentsList(
    barberDatesState: BarberDatesState,
    defaultFont: FontFamily,
    selectedDate: LocalDate,
    filterState: FilterState
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
            val citas = barberDatesState.citas.filter {
                val matchesDate = it.fechainicio.startsWith(selectedDate.toString())
                val matchesFilter = when (filterState) {
                    FilterState.ALL -> true
                    FilterState.PENDING -> it.estado.equals("Pendiente", ignoreCase = true)
                    FilterState.COMPLETED -> it.estado.equals("Completada", ignoreCase = true)
                    FilterState.CANCELLED -> it.estado.equals("Cancelada", ignoreCase = true)
                }
                matchesDate && matchesFilter
            }
            if (citas.isEmpty()) {
                var word = ""
                when (filterState) {
                    FilterState.ALL -> word = ""
                    FilterState.PENDING -> word = "pendientes"
                    FilterState.COMPLETED -> word = "completadas"
                    FilterState.CANCELLED -> word = "canceladas"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Icon(
                            imageVector = Icons.Filled.EventBusy,
                            contentDescription = "Calendario vacío",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(13.dp))
                        Text(
                            text = "No hay citas $word para este día",
                            color = Color(0xFFCCCCCC),
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 19.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
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

    val startTime = remember(cita.fechainicio) { formatTime(cita.fechainicio) }
    val endTime = remember(cita.fechafin) { formatTime(cita.fechafin) }
    val timeRange = "$startTime - $endTime"

    val estadoColor = when (cita.estado?.lowercase()) {
        "pendiente" -> Color(0xFFCB9217)
        "completada" -> Color(0xFF4CAF50)
        "cancelada" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val estadoTexto = when (cita.estado?.lowercase()) {
        "pendiente" -> "Pendiente"
        "completada" -> "Completada"
        "cancelada" -> "Cancelada"
        else -> cita.estado ?: "Desconocido"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(estadoColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Hora",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeRange,
                            fontFamily = defaultFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(estadoColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = estadoTexto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = defaultFont,
                            color = estadoColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Servicio",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cita.servicio_nombre,
                        fontFamily = defaultFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Cliente",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cliente: ${cita.cliente_nombre}",
                        fontFamily = defaultFont,
                        fontSize = 15.sp,
                        color = Color.LightGray.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    val datesViewModel: DatesViewModel = viewModel()
    val updateEstadoState by datesViewModel.updateEstadoState.collectAsState()

    var currentEstado by remember { mutableStateOf(cita.estado ?: "Pendiente") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val estadoColor = when (currentEstado.lowercase()) {
        "completada" -> Color(0xFF4CAF50)
        "cancelada" -> Color(0xFFFF5252)
        "pendiente" -> Color(0xFFFFB74D)
        else -> Color.Gray
    }

    val estadosDisponibles = remember(cita.finalizada) {
        if (cita.finalizada) {
            listOf("Completada", "Cancelada")
        } else {
            listOf("Pendiente", "Completada", "Cancelada")
        }
    }


    LaunchedEffect(updateEstadoState) {
        if (updateEstadoState is DateOperationState.Success) {
            datesViewModel.resetUpdateEstadoState()
            onDismiss()
        }
    }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF212121)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Detalles de la cita",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        DetailItem(
                            icon = Icons.Default.AccessTime,
                            title = "Horario",
                            value = "${formatTime(cita.fechainicio)} - ${formatTime(cita.fechafin)}",
                            defaultFont = defaultFont
                        )
                    }

                    item {
                        DetailSection(
                            title = "Servicio",
                            defaultFont = defaultFont
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = cita.servicio_nombre,
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Duración: ${cita.duracion} min",
                                        color = Color.LightGray,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = priceFormat.format(cita.precio),
                                        color = Color(0xFF4CAF50),
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        DetailSection(
                            title = "Cliente",
                            defaultFont = defaultFont
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    value = cita.cliente_telefono ?: "No disponible",
                                    defaultFont = defaultFont,
                                    onClick = cita.cliente_telefono?.takeIf { it.isNotBlank() }?.let { telefono ->
                                        {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:+34$telefono")
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        DetailSection(
                            title = "Estado",
                            defaultFont = defaultFont
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clickable { isDropdownExpanded = true }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(estadoColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = currentEstado,
                                            color = estadoColor,
                                            style = TextStyle(fontFamily = defaultFont),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Cambiar estado",
                                            tint = estadoColor
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Color(0xFF2C2C2C))
                                        .width(200.dp)
                                ) {
                                    estadosDisponibles.forEach { estado ->
                                        val itemColor = when (estado.lowercase()) {
                                            "completada" -> Color(0xFF4CAF50)
                                            "cancelada" -> Color(0xFFFF5252)
                                            "pendiente" -> Color(0xFFFFB74D)
                                            else -> Color.Gray
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = estado,
                                                    color = if (estado == currentEstado) itemColor else Color.White,
                                                    fontWeight = if (estado == currentEstado) FontWeight.Bold else FontWeight.Normal,
                                                    style = TextStyle(fontFamily = defaultFont)
                                                )
                                            },
                                            onClick = {
                                                currentEstado = estado
                                                isDropdownExpanded = false
                                            },
                                            leadingIcon = {
                                                if (estado == currentEstado) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = itemColor
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = currentEstado.lowercase() != (cita.estado ?: "pendiente").lowercase(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1D1D1D))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                datesViewModel.updateDateEstado(cita.citaid, currentEstado)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (updateEstadoState is DateOperationState.Loading)
                                    Color.Gray else Color(0xFF1976D2)
                            ),
                            enabled = updateEstadoState !is DateOperationState.Loading
                        ) {
                            if (updateEstadoState is DateOperationState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Guardar cambios",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
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
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.Gray,
            style = TextStyle(fontFamily = defaultFont),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
    defaultFont: FontFamily,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(24.dp)
                .padding(2.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = Color.Gray,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 13.sp
                )
            }

            Text(
                text = value,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}