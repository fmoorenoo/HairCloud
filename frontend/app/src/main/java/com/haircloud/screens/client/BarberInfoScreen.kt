package com.haircloud.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.SingleBarbershopState

@Composable
fun BarberInfoScreen(navController: NavController, userId: Int?, localId: Int?) {
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val barbershopViewModel = remember { BarbershopViewModel() }
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()

    LaunchedEffect(userId, localId) {
        if (userId != null && localId != null) {
            barbershopViewModel.getBarbershopById(userId, localId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blackWhiteGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                when (singleBarbershopState) {
                    is SingleBarbershopState.Success -> {
                        val barbershop = (singleBarbershopState as SingleBarbershopState.Success).barbershop
                        Text(
                            text = barbershop.nombre,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            lineHeight = 44.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(start = 40.dp, end = 40.dp)
                        )
                    }
                    is SingleBarbershopState.Loading -> {
                        Text(
                            text = "Cargando...",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 30.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is SingleBarbershopState.Error -> {
                        Text(
                            text = "Error al cargar",
                            color = Color.Red,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 30.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
