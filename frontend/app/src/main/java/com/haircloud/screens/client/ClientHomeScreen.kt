package com.haircloud.screens.client

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.haircloud.viewmodel.BarbershopState
import com.haircloud.viewmodel.BarbershopViewModel
import kotlin.math.roundToInt

@Composable
fun BarbershopCard(
    name: String,
    address: String,
    rating: Float,
    totalRating: Int,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false
) {
    var isMarkedFavorite by remember { mutableStateOf(isFavorite) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAEAEA)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFCCCCCC), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.scissors_icon),
                    contentDescription = "Barbería",
                    tint = Color(0xFF282828),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF1C1C1C)
                )
                Text(
                    text = address,
                    fontSize = 16.sp,
                    color = Color(0xFF4D4D4D)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val roundedRating = (rating * 2).roundToInt() / 2f

                    repeat(5) { index ->
                        val starFill = when {
                            index < roundedRating.toInt() -> Icons.Filled.Star
                            index == roundedRating.toInt() && roundedRating % 1 == 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                            else -> Icons.Filled.StarBorder
                        }
                        Icon(
                            imageVector = starFill,
                            contentDescription = "Star $index",
                            tint = Color(0xFF282828),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "($totalRating)",
                        fontSize = 16.sp,
                        color = Color(0xFF4D4D4D),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Botón de favorito
            IconButton(
                onClick = {
                    isMarkedFavorite = !isMarkedFavorite
                    onFavoriteClick()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x22000000), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMarkedFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Favorito",
                    tint = Color(0xFF282828),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ClientHomeScreen(navController: NavController, userId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barbershopState by barbershopViewModel.barbershopState.collectAsState()

    // Recordar favoritos
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(Unit) {
        barbershopViewModel.getBarbershops()
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
                            .wrapContentHeight(),
                        contentScale = ContentScale.Inside
                    )
                    Image(
                        painter = painterResource(id = R.drawable.user_profile_1),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .clickable(onClick = {
                                navController.navigate("profile/$userId")
                            }),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Barberías",
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f),
                        placeholder = { Text("Buscar por nombre", color = Color(0xFF4D4D4D),
                            style = TextStyle(fontFamily = defaultFont), fontSize = 26.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color(0xFF4D4D4D)
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFD9D9D9),
                            focusedContainerColor = Color(0xFFD9D9D9),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = Color.DarkGray,
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = defaultFont,
                            fontSize = 26.sp
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(67.dp)
                            .background(
                                color = Color(0xFFD9D9D9),
                                shape = RoundedCornerShape(22.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color(0xFF3B3B3B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (barbershopState) {
                        is BarbershopState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        is BarbershopState.Success -> {
                            val barberias = (barbershopState as BarbershopState.Success).barbershops

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                            ) {
                                items(barberias.size) { index ->
                                    val barbershop = barberias[index]
                                    val isFavorite = favorites.contains(barbershop.localid)

                                    BarbershopCard(
                                        name = barbershop.nombre,
                                        address = barbershop.direccion,
                                        rating = barbershop.rating ?: 0f,
                                        totalRating = barbershop.cantidad_resenas,
                                        isFavorite = isFavorite,
                                        onFavoriteClick = {
                                            favorites = if (isFavorite) {
                                                favorites - barbershop.localid
                                            } else {
                                                favorites + barbershop.localid
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        is BarbershopState.Error -> {
                            val errorMsg = (barbershopState as BarbershopState.Error).message
                            Text(
                                text = errorMsg,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        else -> {}
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
                        .size(90.dp)
                        .align(Alignment.Top)
                        .background(Color(0xA9FFFFFF), shape = RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.scissors_icon),
                        contentDescription = "Barberías",
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
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Calendario",
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