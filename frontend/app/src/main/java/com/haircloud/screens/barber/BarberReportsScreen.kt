package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.BarberStatsResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.formatCurrency
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberStatsEmailState
import com.haircloud.viewmodel.BarberStatsState
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.GetBarberState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarberReportsScreen(navController: NavController, userId: Int?, isAdmin: Boolean?, isSemiAdmin: Boolean?) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val barberViewModel = remember { BarberViewModel() }
    val barberState by barberViewModel.barberState.collectAsState()
    val barberStatsState by barberViewModel.barberStatsState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val opcionesFecha = listOf("Hoy", "Esta semana", "Este mes", "Este año", "Personalizado")

    var peluqueroId by remember { mutableIntStateOf(0) }
    var localId by remember { mutableIntStateOf(0) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)

    var startDate by remember { mutableStateOf(startOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var isCalendarVisible by remember { mutableStateOf(false) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

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
            localId = (barberState as GetBarberState.Success).barber.localid

            barberViewModel.getBarberStats(
                peluqueroId,
                localId,
                startDate.format(dateFormatter),
                endDate.format(dateFormatter)
            )
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
                        text = "Estadísticas",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 45.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x88171717)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalDias = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
                        Text(
                            text = "${startDate.format(displayDateFormatter)} - ${endDate.format(displayDateFormatter)} ($totalDias días)",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 20.sp
                        )

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Cambiar fechas",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color(0xFF444444))
                            ) {
                                opcionesFecha.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion, color = Color.White) },
                                        onClick = {
                                            showMenu = false
                                            isCalendarVisible = false
                                            when (opcion) {
                                                "Hoy" -> {
                                                    startDate = today
                                                    endDate = today
                                                }
                                                "Esta semana" -> {
                                                    startDate = today.minusDays((today.dayOfWeek.value - 1).toLong())
                                                    endDate = today
                                                }
                                                "Este mes" -> {
                                                    startDate = today.withDayOfMonth(1)
                                                    endDate = today
                                                }
                                                "Este año" -> {
                                                    startDate = today.withDayOfYear(1)
                                                    endDate = today
                                                }
                                                "Personalizado" -> {
                                                    isCalendarVisible = true
                                                }
                                            }

                                            if (opcion != "Personalizado" && peluqueroId != 0 && localId != 0) {
                                                barberViewModel.getBarberStats(
                                                    peluqueroId,
                                                    localId,
                                                    startDate.format(dateFormatter),
                                                    endDate.format(dateFormatter)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isCalendarVisible) {
                    StatsDateCalendar(
                        show = true,
                        onCancel = { isCalendarVisible = false },
                        onConfirm = { start, end ->
                            startDate = start
                            endDate = end
                            isCalendarVisible = false

                            if (peluqueroId != 0 && localId != 0) {
                                barberViewModel.getBarberStats(
                                    peluqueroId,
                                    localId,
                                    start.format(dateFormatter),
                                    end.format(dateFormatter)
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = !isCalendarVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 515.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            when (barberStatsState) {
                                is BarberStatsState.Success -> {
                                    val stats = (barberStatsState as BarberStatsState.Success).stats
                                    StatsContent(
                                        stats = stats,
                                        defaultFont = defaultFont,
                                        peluqeroId = peluqueroId,
                                        start = startDate.format(dateFormatter),
                                        end = endDate.format(dateFormatter),
                                        barberViewModel = barberViewModel,
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is BarberStatsState.Error -> {
                                    Text(
                                        text = "Error al cargar estadísticas: ${(barberStatsState as BarberStatsState.Error).message}",
                                        color = Color.White,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                is BarberStatsState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Cargando información...",
                                        color = Color.White,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }


            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
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
                            .size(75.dp)
                            .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                            .clickable {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("barber_home/$userId")
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = "Citas",
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
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Reportes",
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
}

@Composable
fun StatsContent(
    stats: BarberStatsResponse,
    defaultFont: FontFamily,
    peluqeroId: Int,
    start: String,
    end: String,
    barberViewModel: BarberViewModel,
    snackbarHostState: SnackbarHostState
) {
    val emailState by barberViewModel.barberStatsEmailState.collectAsState()

    LaunchedEffect(emailState) {
        when (emailState) {
            is BarberStatsEmailState.Success -> {
                snackbarHostState.showTypedSnackbar(
                    (emailState as BarberStatsEmailState.Success).message,
                    type = SnackbarType.SUCCESS
                )
                barberViewModel.resetBarberStatsEmailState()
            }
            is BarberStatsEmailState.Error -> {
                snackbarHostState.showTypedSnackbar(
                    (emailState as BarberStatsEmailState.Error).message,
                    type = SnackbarType.ERROR
                )
                barberViewModel.resetBarberStatsEmailState()
            }
            else -> Unit
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Person,
                title = "Clientes atendidos",
                value = "${stats.total_clientes_atendidos}",
                defaultFont = defaultFont
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarMonth,
                title = "Citas completadas",
                value = "${stats.total_citas}",
                defaultFont = defaultFont,
                color = Color(0x66797979)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Euro,
                title = "Ingresos totales",
                value = stats.ingresos_totales.formatCurrency(),
                defaultFont = defaultFont,
                color = Color(0x66797979)
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timeline,
                title = "Citas/día",
                value = "%.1f".format(stats.promedio_citas_por_dia),
                defaultFont = defaultFont
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Close,
                title = "Canceladas",
                value = "${stats.total_canceladas}",
                defaultFont = defaultFont
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ContentPasteOff,
                title = "No completadas",
                value = "${stats.total_no_completadas}",
                defaultFont = defaultFont,
                color = Color(0x66797979)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        stats.cliente_mas_frecuente?.let { cliente ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x66FFFFFF)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Cliente más frecuente",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = cliente.nombre,
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Total de citas: ${cliente.total_citas}",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        stats.servicio_mas_solicitado?.let { servicio ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x66FFFFFF)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Servicio más solicitado",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = servicio.nombre,
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Reservado ${servicio.cantidad} veces",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable {
                    barberViewModel.sendBarberStatsEmail(
                        peluqueroId = peluqeroId,
                        stats = stats,
                        startDate = start,
                        endDate = end
                    )
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2D2D)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (emailState is BarberStatsEmailState.Sending) "Enviando..." else "Enviar a mi correo",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = defaultFont
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    defaultFont: FontFamily,
    color: Color = Color(0x66BBBBBB)
) {
    Card(
        modifier = modifier
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}