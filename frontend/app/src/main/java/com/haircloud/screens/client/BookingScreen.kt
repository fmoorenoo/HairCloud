package com.haircloud.screens.client

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.AvailableSlot
import com.haircloud.data.model.BarberResponse
import com.haircloud.utils.AvailableSlotsGrid
import com.haircloud.utils.CalendarMonth
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.AvailableSlotsState
import com.haircloud.viewmodel.BarbersState
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.CalendarViewModel
import com.haircloud.viewmodel.SingleServiceState
import com.haircloud.viewmodel.WeeklyScheduleState
import java.time.LocalDate
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookingScreen(navController: NavController, userId: Int?, localId: Int?, serviceId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val calendarViewModel = remember { CalendarViewModel() }
    val barbersState by barbershopViewModel.barbersState.collectAsState()
    val singleServiceState by barbershopViewModel.singleServiceState.collectAsState()
    val weeklyScheduleState by calendarViewModel.weeklyScheduleState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    var selectedBarber by remember { mutableStateOf<BarberResponse?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var availableSlots by remember { mutableStateOf<List<AvailableSlot>>(emptyList()) }
    var selectedSlot by remember { mutableStateOf<AvailableSlot?>(null) }
    var duracionServicio by remember { mutableIntStateOf(30) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.INFO) }
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(serviceId) {
        serviceId?.let {
            barbershopViewModel.getService(it)
        }
    }

    LaunchedEffect(singleServiceState) {
        if (singleServiceState is SingleServiceState.Success) {
            val servicio = (singleServiceState as SingleServiceState.Success).service
            duracionServicio = servicio.duracion
        }
    }

    LaunchedEffect(selectedBarber, selectedDate) {
        if (selectedBarber != null && selectedDate != null) {
            val fecha = selectedDate.toString()
            val duracion = duracionServicio
            calendarViewModel.getAvailableSlots(selectedBarber!!.peluqueroid, fecha, duracion)
        }
    }

    LaunchedEffect(barbersState) {
        if (barbersState is BarbersState.Success) {
            val barbers = (barbersState as BarbersState.Success).barbers
            if (barbers.isNotEmpty() && selectedBarber == null) {
                selectedBarber = barbers[0]
                calendarViewModel.getWeeklySchedule(barbers[0].peluqueroid)
            }
        }
    }

    val slotState by calendarViewModel.availableSlotsState.collectAsState()

    LaunchedEffect(localId) {
        localId?.let {
            barbershopViewModel.getBarbersByLocalId(it)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackWhiteGradient)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                IconButton(
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = "Confirmación cita",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        lineHeight = 38.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Peluqueros disponibles",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Filled.Swipe,
                            contentDescription = "Swipe",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                item {
                    when (barbersState) {
                        is BarbersState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }

                        is BarbersState.Success -> {
                            val barbers = (barbersState as BarbersState.Success).barbers
                            if (barbers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay peluqueros disponibles",
                                        color = Color.White,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 18.sp
                                    )
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(Color(0xFF5D5D5D), shape = RoundedCornerShape(5.dp))
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    items(barbers) { barber ->
                                        BarberCard(
                                            barber = barber,
                                            isSelected = selectedBarber?.peluqueroid == barber.peluqueroid,
                                            onClick = {
                                                selectedBarber = barber
                                                selectedDate = null
                                                selectedSlot = null
                                                availableSlots = emptyList()
                                                calendarViewModel.getWeeklySchedule(barber.peluqueroid)
                                                snackbarMessage = "Seleccionaste a ${barber.nombre}"
                                                snackbarType = SnackbarType.INFO
                                            },
                                            defaultFont = defaultFont
                                        )
                                    }
                                }
                            }
                        }

                        is BarbersState.Error -> {
                            val errorMsg = (barbersState as BarbersState.Error).message
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = Color.Red,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 18.sp
                                )
                            }
                        }

                        else -> {}
                    }
                }
                if (selectedBarber != null) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Selecciona un día",
                                color = Color.White,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Calendario",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }

                    item {
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

                        CalendarMonth(
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                selectedDate = date
                                selectedSlot = null
                            },
                            workingDays = workingDays,
                            onMonthChanged = { selectedDate = null }
                        )
                    }
                }

                if (selectedDate != null) {
                    item {
                        val nombreDia = selectedDate?.dayOfWeek?.getDisplayName(JavaTextStyle.FULL, Locale("es"))
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        val nombreMes = selectedDate?.month?.getDisplayName(JavaTextStyle.FULL, Locale("es"))
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        val dia = selectedDate?.dayOfMonth ?: 0

                        Column {
                            Text(
                                text = "$nombreDia $dia de $nombreMes",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                                style = TextStyle(fontFamily = defaultFont)
                            )

                            Text(
                                text = "Horas disponibles:",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp, start = 8.dp),
                                style = TextStyle(fontFamily = defaultFont)
                            )
                        }
                    }

                    item {
                        when (slotState) {
                            is AvailableSlotsState.Success -> {
                                availableSlots = (slotState as AvailableSlotsState.Success).slots
                                AvailableSlotsGrid(
                                    slots = availableSlots,
                                    onSlotSelected = { selectedSlot = it },
                                    selectedSlot = selectedSlot
                                )
                            }
                            is AvailableSlotsState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                            is AvailableSlotsState.Error -> {
                                Text(
                                    text = "Error al cargar horas",
                                    color = Color.Red,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
fun BarberCard(
    barber: BarberResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    defaultFont: FontFamily
) {
    val boxSize by animateFloatAsState(
        targetValue = if (isSelected) 1.11f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardSize"
    )

    Box(
        modifier = Modifier
            .width(100.dp * boxSize)
            .height(95.dp * boxSize)
            .padding(4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(5.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (isSelected) 12.dp else 8.dp,
                pressedElevation = 12.dp,
                focusedElevation = 10.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFF1F1F1) else Color(0xFF2D2D2D)
            ),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user_profile_1),
                    contentDescription = "Barber Icon",
                    tint = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.size(40.dp * boxSize)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = barber.nombre,
                    color = if (isSelected) Color.Black else Color.White,
                    style = TextStyle(
                        fontFamily = defaultFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (17 * boxSize).sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}