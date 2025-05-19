package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.haircloud.utils.CalendarMonth
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.formatDateToLong
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberDatesState
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.CalendarViewModel
import com.haircloud.viewmodel.DateOperationState
import com.haircloud.viewmodel.DatesViewModel
import com.haircloud.viewmodel.GetBarberState
import com.haircloud.viewmodel.WeeklyScheduleState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class FilterState(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas"),
    CANCELLED("Canceladas"),
    NOT_COMPLETED("No completadas")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarberHomeScreen(navController: NavController, userId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isCalendarVisible by remember { mutableStateOf(false) }
    var selectedFilterState by remember { mutableStateOf(FilterState.ALL) }
    val barberViewModel = remember { BarberViewModel() }
    val calendarViewModel = remember { CalendarViewModel() }
    val barberState by barberViewModel.barberState.collectAsState()
    val weeklyScheduleState by calendarViewModel.weeklyScheduleState.collectAsState()
    val barberDatesState by barberViewModel.barberDatesState.collectAsState()
    val datesViewModel: DatesViewModel = viewModel()
    val updateEstadoState by datesViewModel.updateEstadoState.collectAsState()

    var peluqueroId by remember { mutableIntStateOf(0) }
    var peluqueroRol by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var isSemiAdmin by remember { mutableStateOf(false) }
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    fun reloadDates() {
        val start = LocalDate.now().minusMonths(2).withDayOfMonth(1)
        val end = LocalDate.now().plusMonths(5).withDayOfMonth(1).plusMonths(1).minusDays(1)
        barberViewModel.getBarberDatesInRange(peluqueroId, start.toString(), end.toString())
    }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isCalendarVisible) 180f else 0f,
        label = "arrowRotation"
    )

    val citasPendientes = remember(barberDatesState, selectedDate) {
        when (barberDatesState) {
            is BarberDatesState.Success -> {
                (barberDatesState as BarberDatesState.Success).citas.count { cita ->
                    val citaDate = try {
                        LocalDate.parse(cita.fechainicio.substring(0, 10))
                    } catch (_: Exception) {
                        null
                    }
                    citaDate == selectedDate && cita.estado.equals("Pendiente", ignoreCase = true)
                }
            }
            else -> 0
        }
    }

    val citasCompletadas = remember(barberDatesState, selectedDate) {
        when (barberDatesState) {
            is BarberDatesState.Success -> {
                (barberDatesState as BarberDatesState.Success).citas.count { cita ->
                    val citaDate = try {
                        LocalDate.parse(cita.fechainicio.substring(0, 10))
                    } catch (_: Exception) {
                        null
                    }
                    citaDate == selectedDate && cita.estado.equals("Completada", ignoreCase = true)
                }
            }
            else -> 0
        }
    }

    val citasCanceladas = remember(barberDatesState, selectedDate) {
        when (barberDatesState) {
            is BarberDatesState.Success -> {
                (barberDatesState as BarberDatesState.Success).citas.count { cita ->
                    val citaDate = try {
                        LocalDate.parse(cita.fechainicio.substring(0, 10))
                    } catch (_: Exception) {
                        null
                    }
                    citaDate == selectedDate && cita.estado.equals("Cancelada", ignoreCase = true)
                }
            }
            else -> 0
        }
    }

    val citasNoCompletadas = remember(barberDatesState, selectedDate) {
        when (barberDatesState) {
            is BarberDatesState.Success -> {
                (barberDatesState as BarberDatesState.Success).citas.count { cita ->
                    val citaDate = try {
                        LocalDate.parse(cita.fechainicio.substring(0, 10))
                    } catch (_: Exception) {
                        null
                    }
                    citaDate == selectedDate && cita.estado.equals("No completada", ignoreCase = true)
                }
            }
            else -> 0
        }
    }

    val totalCitas = citasPendientes + citasCompletadas + citasCanceladas + citasNoCompletadas

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(userId) {
        userId?.let {
            barberViewModel.getBarber(it)
        }
    }

    LaunchedEffect(barberState) {
        if (barberState is GetBarberState.Success) {
            peluqueroId = (barberState as GetBarberState.Success).barber.peluqueroid
            peluqueroRol = (barberState as GetBarberState.Success).barber.rol
            isAdmin = peluqueroRol == "admin"
            isSemiAdmin = peluqueroRol == "semiadmin"
            calendarViewModel.getWeeklySchedule(peluqueroId)
            reloadDates()
        }
    }

    LaunchedEffect(updateEstadoState) {
        if (updateEstadoState is DateOperationState.Success) {
            reloadDates()
            val message = (updateEstadoState as DateOperationState.Success).message
            snackbarMessage = message
            snackbarType = SnackbarType.SUCCESS
            datesViewModel.resetUpdateEstadoState()
        }
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
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_lightlogo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(50.dp)
                            .clickable(
                                onClick = {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("barber_user_manual")
                                    }
                                }
                            ),
                        contentScale = ContentScale.Inside
                    )

                    IconButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("barber_notifications/$peluqueroId")
                            }
                        },
                        modifier = Modifier.size(55.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chat",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(45.dp)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.user_profile_1),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClick = {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        userId?.let {
                                            navController.navigate("barber_profile/$it")
                                        }
                                    }
                                }
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mis Citas",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 45.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isCalendarVisible = !isCalendarVisible
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFD9D9D9),
                            contentColor = Color(0xFF3B3B3B)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = Color(0xFF3B3B3B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Calendar",
                            tint = Color(0xFF3B3B3B),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = arrowRotation }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isCalendarVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 15.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            val dayNameToCalendarIndex = mapOf(
                                "Domingo" to 0,
                                "Lunes" to 1,
                                "Martes" to 2,
                                "Miércoles" to 3,
                                "Jueves" to 4,
                                "Viernes" to 5,
                                "Sábado" to 6
                            )

                            val workingDays = when (weeklyScheduleState) {
                                is WeeklyScheduleState.Success -> {
                                    (weeklyScheduleState as WeeklyScheduleState.Success).schedule.mapNotNull {
                                        dayNameToCalendarIndex[it.diasemana]
                                    }
                                }
                                else -> listOf()
                            }
                            val diasConCitas by remember(barberDatesState) {
                                derivedStateOf {
                                    when (barberDatesState) {
                                        is BarberDatesState.Success -> {
                                            (barberDatesState as BarberDatesState.Success).citas.mapNotNull {
                                                try {
                                                    LocalDate.parse(it.fechainicio.substring(0, 10))
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            }.distinct()
                                        }

                                        else -> emptyList()
                                    }
                                }
                            }

                            CalendarMonth(
                                selectedDate = selectedDate,
                                onDateSelected = { date ->
                                    selectedDate = date
                                    isCalendarVisible = false
                                    snackbarMessage = "Mostrando citas para: ${
                                        date.format(
                                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                        )
                                    }"
                                    snackbarType = SnackbarType.INFO
                                    selectedFilterState = FilterState.ALL
                                },
                                workingDays = workingDays,
                                initialMonth = YearMonth.from(selectedDate),
                                diasConCitas = diasConCitas,
                                onMonthChanged = {},
                                allowPreviousMonth = true
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility(visible = !isCalendarVisible) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateToLong(selectedDate),
                                color = Color.White,
                                style = TextStyle(
                                    fontFamily = defaultFont,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Box {
                                OutlinedButton(
                                    onClick = { isFilterDropdownExpanded = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = when(selectedFilterState) {
                                            FilterState.ALL -> Color(0xFF444444)
                                            FilterState.PENDING -> Color(0xFF946B11)
                                            FilterState.COMPLETED -> Color(0xFF3A863D)
                                            FilterState.CANCELLED -> Color(0xFFAD2F26)
                                            FilterState.NOT_COMPLETED -> Color(0xFF9E9E9E)
                                        },
                                        contentColor = Color.White
                                    )
                                ) {
                                    val filterLabel = when(selectedFilterState) {
                                        FilterState.ALL -> "Todas ($totalCitas)"
                                        FilterState.PENDING -> "Pendi... ($citasPendientes)"
                                        FilterState.COMPLETED -> "Comple... ($citasCompletadas)"
                                        FilterState.CANCELLED -> "Cancel... ($citasCanceladas)"
                                        FilterState.NOT_COMPLETED -> "No com... ($citasNoCompletadas)"
                                    }
                                    Text(
                                        text = filterLabel,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Filtrar"
                                    )
                                }

                                DropdownMenu(
                                    expanded = isFilterDropdownExpanded,
                                    onDismissRequest = { isFilterDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF2A2A2A))
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Todas ($totalCitas)",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp,
                                                color = if (selectedFilterState == FilterState.ALL) Color.White else Color.LightGray
                                            )
                                        },
                                        onClick = {
                                            selectedFilterState = FilterState.ALL
                                            isFilterDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilterState == FilterState.ALL) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        },
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Pendientes ($citasPendientes)",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp,
                                                color = if (selectedFilterState == FilterState.PENDING) Color.White else Color.LightGray
                                            )
                                        },
                                        onClick = {
                                            selectedFilterState = FilterState.PENDING
                                            isFilterDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilterState == FilterState.PENDING) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        },

                                        modifier = Modifier.background(
                                            if (selectedFilterState == FilterState.PENDING) Color(0xFFCB9217) else Color(0xFF2A2A2A)
                                        )
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Completadas ($citasCompletadas)",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp,
                                                color = if (selectedFilterState == FilterState.COMPLETED) Color.White else Color.LightGray
                                            )
                                        },
                                        onClick = {
                                            selectedFilterState = FilterState.COMPLETED
                                            isFilterDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilterState == FilterState.COMPLETED) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        },

                                        modifier = Modifier.background(
                                            if (selectedFilterState == FilterState.COMPLETED) Color(0xFF4CAF50) else Color(0xFF2A2A2A)
                                        )
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "No completadas ($citasNoCompletadas)",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp,
                                                color = if (selectedFilterState == FilterState.NOT_COMPLETED) Color.White else Color.LightGray
                                            )
                                        },
                                        onClick = {
                                            selectedFilterState = FilterState.NOT_COMPLETED
                                            isFilterDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilterState == FilterState.NOT_COMPLETED) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        },
                                        modifier = Modifier.background(
                                            if (selectedFilterState == FilterState.NOT_COMPLETED) Color(0xFF9E9E9E) else Color(0xFF2A2A2A)
                                        )
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Canceladas ($citasCanceladas)",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp,
                                                color = if (selectedFilterState == FilterState.CANCELLED) Color.White else Color.LightGray
                                            )
                                        },
                                        onClick = {
                                            selectedFilterState = FilterState.CANCELLED
                                            isFilterDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilterState == FilterState.CANCELLED) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        },
                                        modifier = Modifier.background(
                                            if (selectedFilterState == FilterState.CANCELLED) Color(0xFFF44336) else Color(0xFF2A2A2A)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        BarberAppointmentsList(
                            barberDatesState = barberDatesState,
                            defaultFont = defaultFont,
                            selectedDate = selectedDate,
                            filterState = selectedFilterState
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                        .clickable {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("barber_settings/$userId/$isAdmin/$isSemiAdmin")
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = Color(0xFF282828),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(45.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Top)
                        .background(Color(0xA9FFFFFF), shape = RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = "Citas",
                        tint = Color(0xFF282828),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(55.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                        .clickable {
                            if (!isNavigating) {
                                isNavigating = true
                                // navController.navigate("barber_reports/$userId")
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Reportes",
                        tint = Color(0xFF282828),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(45.dp)
                    )
                }
            }
        }
    }
}