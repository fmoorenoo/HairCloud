package com.haircloud.screens.client

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.haircloud.data.model.BarberResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarbersState
import com.haircloud.viewmodel.BarbershopViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookingScreen(navController: NavController, userId: Int?, localId: Int?, serviceId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barbersState by barbershopViewModel.barbersState.collectAsState()

    var selectedBarber by remember { mutableStateOf<BarberResponse?>(null) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.INFO) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackWhiteGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
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

                Spacer(modifier = Modifier.height(24.dp))

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
                                    .background(Color(0XFF797979), shape = RoundedCornerShape(5.dp))
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
                                            snackbarMessage = "Seleccionaste a ${barber.nombre}"
                                            snackbarType = SnackbarType.INFO
                                        },
                                        defaultFont = defaultFont
                                    )
                                }
                            }

                            selectedBarber?.let { barber ->
                                Spacer(modifier = Modifier.height(24.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0x33FFFFFF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.user_profile_1),
                                                contentDescription = "Barber Avatar",
                                                tint = Color.Black,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(
                                                text = barber.nombre,
                                                color = Color.White,
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = barber.especialidad ?: "Sin especialidad",
                                                color = Color(0xCCFFFFFF),
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
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
            .width(110.dp * boxSize)
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
                    modifier = Modifier.size(45.dp * boxSize)
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