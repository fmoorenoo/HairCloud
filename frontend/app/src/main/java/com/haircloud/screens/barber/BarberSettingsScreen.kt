package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.GetBarberState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarberSettingsScreen(navController: NavController, userId: Int?, isAdmin: Boolean?, isSemiAdmin: Boolean?) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val barberViewModel = remember { BarberViewModel() }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barberState by barberViewModel.barberState.collectAsState()
    val scrollState = rememberScrollState()

    var peluqueroId by remember { mutableIntStateOf(0) }
    var localId by remember { mutableIntStateOf(0) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

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
            barbershopViewModel.getBarbershopById(userId ?: 0, localId)
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
                    .verticalScroll(scrollState)
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
                                        navController.navigate("user_manual")
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
                        text = "Ajustes",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 45.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                SettingsOption(
                    icon = Icons.Default.Store,
                    title = "Información del Local",
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            navController.navigate("barbershop_info/$localId/$userId/$isAdmin/$isSemiAdmin")
                        }
                    },
                    defaultFont = defaultFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsOption(
                    icon = Icons.Default.ContentCut,
                    title = "Servicios",
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            navController.navigate("barbershop_services/$localId/$isAdmin/$isSemiAdmin")
                        }
                    },
                    defaultFont = defaultFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsOption(
                    icon = Icons.Default.People,
                    title = "Peluqueros",
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            navController.navigate("barbershop_barbers/$localId/$userId/$isAdmin/$isSemiAdmin")
                        }
                    },
                    defaultFont = defaultFont
                )
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
                        .size(90.dp)
                        .align(Alignment.Top)
                        .background(Color(0xA9FFFFFF), shape = RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
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

@Composable
fun SettingsOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    defaultFont: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.White
            )
        }
    }
}