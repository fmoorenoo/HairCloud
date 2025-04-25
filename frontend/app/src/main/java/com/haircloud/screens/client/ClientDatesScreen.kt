package com.haircloud.screens.client

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.Date
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.ClientState
import com.haircloud.viewmodel.ClientViewModel
import com.haircloud.viewmodel.DateOperationState
import com.haircloud.viewmodel.DatesViewModel
import com.haircloud.viewmodel.GetDatesState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ClientDatesScreen(navController: NavController, userId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clientViewModel = remember { ClientViewModel() }
    val datesViewModel = remember { DatesViewModel() }
    val deleteState by datesViewModel.deleteDateState.collectAsState()
    val clientState by clientViewModel.clientState.collectAsState()
    val appointmentsState by clientViewModel.appointmentsState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }

    var clientId by remember { mutableStateOf<Int?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    var selectedTab by remember { mutableIntStateOf(0) }

    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(userId) {
        userId?.let {
            clientViewModel.getClient(it)
        }
    }

    LaunchedEffect(clientState) {
        if (clientState is ClientState.Success) {
            val client = (clientState as ClientState.Success).client
            clientId = client.clienteid
            clientViewModel.getDates(client.clienteid)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(selectedTab) {
        clientId?.let {
            clientViewModel.getDates(it)
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DateOperationState.Success -> {
                snackbarMessage = state.message
                snackbarType = SnackbarType.SUCCESS
                datesViewModel.resetDeleteDateState()
                clientId?.let { clientViewModel.getDates(it) }
            }

            is DateOperationState.Error -> {
                snackbarMessage = state.message
                snackbarType = SnackbarType.ERROR
                datesViewModel.resetDeleteDateState()
                clientId?.let { clientViewModel.getDates(it) }
            }

            else -> {}
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
                            .wrapContentHeight()
                            .clickable(
                                onClick = {
                                    if (!isNavigating) {
                                        isNavigating = true
                                        navController.navigate("user_manual/$userId")
                                    }
                                }
                            ),
                        contentScale = ContentScale.Inside
                    )
                    Image(
                        painter = painterResource(id = R.drawable.user_profile_1),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .clickable(onClick = {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("profile/$userId")
                                }
                            }),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tus citas",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 45.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabButton(
                        text = "Citas próximas",
                        icon = Icons.Filled.EventAvailable,
                        isSelected = selectedTab == 0,
                        iconTint = Color(0xFF4A90E2),
                        defaultFont = defaultFont,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )

                    TabButton(
                        text = "Historial",
                        icon = Icons.Filled.EventBusy,
                        isSelected = selectedTab == 1,
                        iconTint = Color(0xFF4A90E2),
                        defaultFont = defaultFont,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 150.dp)
                ) {
                    when (appointmentsState) {
                        is GetDatesState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }

                        is GetDatesState.Success -> {
                            val allAppointments = (appointmentsState as GetDatesState.Success).citas
                            val activeAppointments = allAppointments.filter { !it.finalizada }
                            val finishedAppointments = allAppointments.filter { it.finalizada }

                            if (selectedTab == 0) {
                                if (activeAppointments.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        itemsIndexed(activeAppointments) { index, appointment ->
                                            DateCard(
                                                appointment = appointment,
                                                isActive = true,
                                                defaultFont = defaultFont,
                                                shadeIndex = index,
                                                onCancelAppointment = { citaId ->
                                                    datesViewModel.deleteDate(citaId)
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EmptyAppointmentsMessage(
                                            message = "No tienes citas próximas. Ve a la pestaña de barberías para hacer una reserva",
                                            defaultFont = defaultFont
                                        )
                                    }
                                }
                            } else {
                                if (finishedAppointments.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(finishedAppointments) { appointment ->
                                            DateCard(
                                                appointment = appointment,
                                                isActive = false,
                                                defaultFont = defaultFont,
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EmptyAppointmentsMessage(
                                            message = "No tienes citas finalizadas",
                                            defaultFont = defaultFont
                                        )
                                    }
                                }
                            }
                        }

                        is GetDatesState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (appointmentsState as GetDatesState.Error).message,
                                    color = Color.Red,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        else -> { }
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                        .clickable {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("client_favs/$userId")
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmarks,
                        contentDescription = "Favoritos",
                        tint = Color(0xFF282828),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(45.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                        .clickable {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("client_home/$userId")
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.scissors_icon),
                        contentDescription = "Barberías",
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
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Calendario",
                        tint = Color(0xFF282828),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(55.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    iconTint: Color,
    defaultFont: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF1E1E1E) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color(0xFFAAAAAA)

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) iconTint else Color(0xFFAAAAAA),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun DateCard(
    appointment: Date,
    isActive: Boolean,
    defaultFont: FontFamily,
    shadeIndex: Int = 0,
    onCancelAppointment: (Int) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    val shadeColors = listOf(
        Color(0xFF1E1E1E),
        Color(0xFF333333),
    )

    val cardBackground = if (isActive) {
        shadeColors.getOrElse(shadeIndex.coerceAtMost(1)) { shadeColors.last() }
    } else {
        Color(0xFF3A3A3A)
    }
    val accentColor = Color(0xFF4A90E2)
    val secondaryTextColor = if (isActive) Color(0xFF999999) else Color(0xFF666666)
    val statusCircleColor = if (isActive) accentColor else Color(0xFF666666)

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val outputDateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("es", "ES")) }
    val outputTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val startDateTime = try {
        LocalDateTime.parse(appointment.fechainicio, dateFormatter)
    } catch (_: Exception) {
        LocalDateTime.now()
    }

    val endDateTime = try {
        LocalDateTime.parse(appointment.fechafin, dateFormatter)
    } catch (_: Exception) {
        LocalDateTime.now().plusMinutes(30)
    }

    val formattedDate = startDateTime.format(outputDateFormatter).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    val formattedStartTime = startDateTime.format(outputTimeFormatter)
    val formattedEndTime = endDateTime.format(outputTimeFormatter)

    val precio = appointment.servicio_precio
    val duracionServicio = "${appointment.servicio_duracion} min"
    val isToday = startDateTime.toLocalDate() == LocalDate.now()

    Box {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = appointment.local_nombre ?: "Barbería no especificada",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontFamily = defaultFont),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formattedDate,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(fontFamily = defaultFont),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_profile_1),
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = appointment.barber_nombre ?: "Peluquero no especificado",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            style = TextStyle(fontFamily = defaultFont),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .background(statusCircleColor, CircleShape)
                        )

                        if (isToday && isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HOY",
                                color = accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(fontFamily = defaultFont)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$formattedStartTime - $formattedEndTime",
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontFamily = defaultFont)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFF444444))

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = appointment.servicio_nombre ?: "Servicio no especificado",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(fontFamily = defaultFont),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = duracionServicio,
                            color = secondaryTextColor,
                            fontSize = 17.sp,
                            style = TextStyle(fontFamily = defaultFont)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$precio€",
                            color = secondaryTextColor,
                            fontSize = 17.sp,
                            style = TextStyle(fontFamily = defaultFont)
                        )
                    }
                }
            }
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .padding(bottom = 3.dp)
                    .size(48.dp)
                    .clickable { showMenu = !showMenu },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancelar cita",
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(45.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF363636))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Cancelar cita",
                                style = TextStyle(
                                    fontFamily = defaultFont,
                                    fontSize = 16.sp,
                                    color = Color(0xFFFF6B6B)
                                )
                            )
                        },
                        onClick = {
                            onCancelAppointment(appointment.citaid)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cancelar cita",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyAppointmentsMessage(
    message: String,
    defaultFont: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(160.dp)
            .background(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (message.contains("próximas")) {
                Icon(
                    imageVector = Icons.Filled.EventAvailable,
                    contentDescription = null,
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.EventBusy,
                    contentDescription = null,
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = message,
                color = Color(0xFFB8B8B8),
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}