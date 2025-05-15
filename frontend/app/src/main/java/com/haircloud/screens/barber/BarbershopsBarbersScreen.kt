package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.BarberResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.formatHour
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.BarbersState
import com.haircloud.viewmodel.CalendarViewModel
import com.haircloud.viewmodel.WeeklyScheduleState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarbershopBarbersScreen(navController: NavController, localId: Int, userId: Int?, isAdmin: Boolean = false) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barbersState by barbershopViewModel.barbersState.collectAsState()

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF1E1E1E)
    val barberViewModel: BarberViewModel = viewModel()



    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(localId) {
        barbershopViewModel.getBarbersByLocalId(localId)
    }

    Scaffold(
        modifier = Modifier
            .background(brush = blackWhiteGradient)
            .fillMaxSize(),
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 30.dp),
                defaultFont = defaultFont
            )
        },
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkSurface.copy(alpha = 0.8f))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = if (isAdmin) "Administrar peluqueros" else "Peluqueros",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAdmin) {
                        IconButton(
                            onClick = {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("barbershop_add_barber/$localId/$userId")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir peluquero",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (barbersState) {
                    is BarbersState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is BarbersState.Error -> {
                        val errorMessage = (barbersState as BarbersState.Error).message
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }
                    }
                    is BarbersState.Success -> {
                        var searchQuery by remember { mutableStateOf("") }

                        val query = searchQuery.lowercase()

                        val barbers = (barbersState as BarbersState.Success).barbers

                        val youFiltered = barbers.find { it.usuarioid == userId }?.takeIf {
                            it.nombre.lowercase().contains(query) ||
                                    it.telefono?.contains(query) == true ||
                                    it.fechacontratacion.lowercase().contains(query)
                        }
                        val othersFiltered = barbers.filter {
                            it.usuarioid != userId && (
                                    it.nombre.lowercase().contains(query) ||
                                            it.telefono?.contains(query) == true ||
                                            it.fechacontratacion.lowercase().contains(query)
                                    )
                        }

                        val filteredBarbers = listOfNotNull(youFiltered) + othersFiltered

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscador (nombre, teléfono o fecha)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF444444),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedLabelColor = Color(0xFFAAAAAA),
                                unfocusedLabelColor = Color(0xFF888888),
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        if (filteredBarbers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = "Sin resultados",
                                        tint = Color.Gray.copy(alpha = 0.6f),
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se encontraron peluqueros",
                                        color = Color.Gray.copy(alpha = 0.8f),
                                        fontSize = 19.sp,
                                        fontFamily = defaultFont
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                items(filteredBarbers) { barber ->
                                    BarberCard(
                                        barber = barber,
                                        defaultFont = defaultFont,
                                        userId = userId,
                                        isAdmin = isAdmin,
                                        onToggleRole = { usuarioId, nuevoEstado ->
                                            barberViewModel.toggleBarberRole(usuarioId)
                                            val mensaje = if (nuevoEstado) {
                                                "${barbers.find { it.usuarioid == usuarioId }?.nombre ?: "El peluquero"} ahora puede administrar los servicios y las reseñas"
                                            } else {
                                                "${barbers.find { it.usuarioid == usuarioId }?.nombre ?: "El peluquero"} ya no tiene permisos de gestión"
                                            }
                                            snackbarType = SnackbarType.INFO
                                            snackbarMessage = mensaje
                                        },
                                        onDeleteBarber = { usuarioId ->
                                            barberViewModel.deactivateBarber(usuarioId)
                                            snackbarType = SnackbarType.INFO
                                            snackbarMessage = "Barbero eliminado correctamente"
                                            barbershopViewModel.getBarbersByLocalId(localId)
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(40.dp)) }
                            }
                        }
                    }
                    else -> {  }
                }
            }
        }
    }
}

@Composable
fun BarberCard(
    barber: BarberResponse,
    defaultFont: FontFamily,
    userId: Int?,
    isAdmin: Boolean = false,
    onToggleRole: (usuarioId: Int, nuevoEstado: Boolean) -> Unit,
    onDeleteBarber: (usuarioId: Int) -> Unit
) {
    val isYou = userId == barber.usuarioid
    val backgroundColor = if (isYou) Color(0xFF4F4F4F) else Color(0xFF2C2C2C)
    val borderColor = if (isYou) Color(0xFFFFFFFF) else Color.Transparent
    val textColor = Color.White
    var isSemiadmin by remember { mutableStateOf(barber.rol == "semiadmin") }
    val coroutineScope = rememberCoroutineScope()
    var isToggleEnabled by remember { mutableStateOf(true) }

    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val calendarViewModel: CalendarViewModel = viewModel()
    val weeklyScheduleState by calendarViewModel.weeklyScheduleState.collectAsState()
    var showScheduleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showScheduleDialog) {
        if (showScheduleDialog) {
            calendarViewModel.getWeeklySchedule(barber.peluqueroid)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user_profile_1),
                            contentDescription = "Imagen del peluquero",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isYou) "${barber.nombre} (Tú)" else barber.nombre,
                                color = textColor,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = barber.especialidad ?: "Sin especialidad",
                                color = textColor.copy(alpha = 0.8f),
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }

                        if (isYou) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                            ) {
                                IconButton(
                                    onClick = { showScheduleDialog = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Ver tu horario",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Teléfono",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = barber.telefono ?: "No disponible",
                            color = textColor,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Fecha de contratación",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = barber.fechacontratacion,
                            color = textColor,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 16.sp
                        )
                    }
                }

                if (isAdmin && !isYou) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Horario",
                                color = textColor,
                                fontSize = 17.sp,
                                fontFamily = defaultFont
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                            ) {
                                IconButton(
                                    onClick = { showScheduleDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Ver horario semanal",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Permisos",
                                color = textColor,
                                fontSize = 17.sp,
                                fontFamily = defaultFont
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = isSemiadmin,
                                onCheckedChange = { nuevoEstado ->
                                    if (isToggleEnabled) {
                                        isToggleEnabled = false
                                        isSemiadmin = nuevoEstado
                                        coroutineScope.launch {
                                            delay(500)
                                            onToggleRole(barber.usuarioid, nuevoEstado)
                                            isToggleEnabled = true
                                        }
                                    }
                                },
                                enabled = isToggleEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    uncheckedThumbColor = Color.Gray,
                                    checkedTrackColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }

            if (isAdmin && !isYou) {
                val darkSurface = Color(0xFF1E1E1E)
                val darkError = Color(0xFFCF6679)
                val darkSecondary = Color(0xFFFFFFFF)

                Box(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp))
                {

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2D2D2D))
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = darkSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(darkSurface)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = darkError
                                )
                            },
                            text = {
                                Text(
                                    "Eliminar peluquero",
                                    fontFamily = FontFamily.Default,
                                    color = Color.White
                                )
                            },
                            onClick = {
                                showMenu = false
                                showConfirmDialog = true
                            }
                        )
                    }
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        containerColor = darkSurface,
                        titleContentColor = Color.White,
                        textContentColor = Color.LightGray,
                        title = {
                            Text(
                                "Confirmar eliminación",
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                "¿Estás seguro de que deseas eliminar a ${barber.nombre}?\nPodrás volver a añadirlo si lo necesitas",
                                fontFamily = FontFamily.Default,
                                lineHeight = 20.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showConfirmDialog = false
                                    onDeleteBarber(barber.usuarioid)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = darkError,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "Eliminar",
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showConfirmDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = darkSecondary
                                )
                            ) {
                                Text(
                                    "Cancelar",
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }
                    )
                }
            }
        }
        if (showScheduleDialog) {
            val darkSurface = Color(0xFF1E1E1E)
            val accentColor = Color(0xFF7EDADA)
            val lightGray = Color(0xFFAAAAAA)

            AlertDialog(
                onDismissRequest = { showScheduleDialog = false },
                containerColor = darkSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = {
                    Text(
                        if (isYou) "Tu horario" else "Horario de ${barber.nombre}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        fontFamily = defaultFont
                    )
                },
                text = {
                    when (weeklyScheduleState) {
                        is WeeklyScheduleState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = accentColor)
                            }
                        }
                        is WeeklyScheduleState.Error -> {
                            Text(
                                "Error: ${(weeklyScheduleState as WeeklyScheduleState.Error).message}",
                                fontSize = 16.sp
                            )
                        }
                        is WeeklyScheduleState.Success -> {
                            val schedule = (weeklyScheduleState as WeeklyScheduleState.Success).schedule
                            val dayOrder = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

                            val sortedSchedule = schedule.sortedBy { entry ->
                                dayOrder.indexOf(entry.diasemana).takeIf { it >= 0 } ?: Int.MAX_VALUE
                            }

                            if (sortedSchedule.isEmpty()) {
                                Text(
                                    "Este peluquero aún no tiene horario asignado.",
                                    fontSize = 16.sp,
                                    fontFamily = defaultFont,
                                    color = Color.LightGray
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Día",
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = accentColor,
                                            fontFamily = defaultFont
                                        )
                                        Text(
                                            text = "Inicio",
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = accentColor,
                                            fontFamily = defaultFont
                                        )
                                        Text(
                                            text = "Fin",
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = accentColor,
                                            fontFamily = defaultFont
                                        )
                                    }

                                    HorizontalDivider(color = lightGray.copy(alpha = 0.3f))

                                    sortedSchedule.forEach { entry ->
                                        val dayName = entry.diasemana
                                        val startTime = formatHour(entry.horainicio)
                                        val endTime = formatHour(entry.horafin)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = dayName,
                                                modifier = Modifier.weight(1f),
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = defaultFont
                                            )
                                            Text(
                                                text = startTime,
                                                modifier = Modifier.weight(1f),
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = defaultFont
                                            )
                                            Text(
                                                text = endTime,
                                                modifier = Modifier.weight(1f),
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = defaultFont
                                            )
                                        }

                                        if (entry != sortedSchedule.last()) {
                                            HorizontalDivider(color = lightGray.copy(alpha = 0.2f))
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            Text("No hay datos disponibles.", fontSize = 16.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showScheduleDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                    ) {
                        Text("Cerrar", fontSize = 17.sp)
                    }
                }
            )
        }
    }
}


