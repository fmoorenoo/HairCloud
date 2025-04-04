package com.haircloud.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale

@Composable
fun BarberInfoScreen(navController: NavController, userId: Int?, localId: Int?) {
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val barbershopViewModel = remember { BarbershopViewModel() }
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(userId, localId) {
        if (userId != null && localId != null) {
            barbershopViewModel.getBarbershopById(userId, localId)
        }
    }

    LaunchedEffect(singleBarbershopState) {
        if (singleBarbershopState is SingleBarbershopState.Success) {
            isFavorite = (singleBarbershopState as SingleBarbershopState.Success).barbershop.es_favorito
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
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = if (singleBarbershopState is SingleBarbershopState.Success)
                            (singleBarbershopState as SingleBarbershopState.Success).barbershop.nombre
                        else "Cargando...",
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

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Favorito",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (singleBarbershopState) {
                is SingleBarbershopState.Success -> {
                    val barbershop = (singleBarbershopState as SingleBarbershopState.Success).barbershop
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRow(
                            label = "Horario",
                            value = "${
                                barbershop.horarioapertura.substring(
                                    0,
                                    5
                                )
                            } - ${barbershop.horariocierre.substring(0, 5)}",
                            icon = Icons.Default.Schedule
                        )
                        InfoRow(
                            label = "Teléfono",
                            value = barbershop.telefono,
                            icon = Icons.Default.Phone
                        )
                        InfoRow(
                            label = "Dirección",
                            value = "${barbershop.direccion}, ${barbershop.localidad}",
                            icon = Icons.Default.LocationOn
                        )
                        ExpandableInfoRow(
                            label = "Descripción",
                            value = barbershop.descripcion ?: "Sin descripción",
                            icon = Icons.Default.ChatBubble
                        )
                        InfoRow(
                            label = "HairCloud Points",
                            value = if (barbershop.puntos_habilitados) "Habilitado" else "No habilitado",
                            icon = Icons.Filled.CheckCircle
                        )
                        InfoRow(
                            label = "Valoración",
                            value = "(${barbershop.cantidad_resenas} ${if (barbershop.cantidad_resenas == 1) "reseña" else "reseñas"})",
                            icon = Icons.Filled.Star,
                            rating = barbershop.rating ?: 0f
                        )
                    }
                }
                is SingleBarbershopState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cargando información...",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is SingleBarbershopState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Error al cargar la información",
                            color = Color.Red,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                if (userId != null && localId != null) {
                                    barbershopViewModel.getBarbershopById(userId, localId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD9D9D9),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "Reintentar",
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector, rating: Float? = null) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF3D8EE6),
            modifier = Modifier
                .padding(end = 8.dp, top = 2.dp)
                .size(25.dp)
        )
        Column {
            Text(
                text = label,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 20.sp,
                color = Color(0xFFAAAAAA)
            )

            if (label == "Valoración" && rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.1f", rating),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StarRating(rating, value)
                }
            } else {
                Text(
                    text = value,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun ExpandableInfoRow(label: String, value: String, icon: ImageVector) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF3D8EE6),
            modifier = Modifier
                .padding(end = 8.dp, top = 2.dp)
                .size(25.dp)
        )
        Column {
            Text(
                text = label,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 20.sp,
                color = Color(0xFFAAAAAA)
            )
            Text(
                text = value,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 22.sp,
                color = Color.White,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun StarRating(rating: Float, value: String) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val roundedRating = (rating * 2).toInt() / 2f
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val star = when {
                index < roundedRating.toInt() -> Icons.Filled.Star
                index == roundedRating.toInt() && roundedRating % 1 == 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = star,
                contentDescription = "Star $index",
                tint = Color(0xFF3D8EE6),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = TextStyle(fontFamily = defaultFont),
            fontSize = 16.sp,
            color = Color(0xFFD9D9D9)
        )
    }
}
