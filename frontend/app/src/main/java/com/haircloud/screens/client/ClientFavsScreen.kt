package com.haircloud.screens.client

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarbershopState
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.ClientState
import com.haircloud.viewmodel.ClientViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ClientFavsScreen(navController: NavController, userId: Int?) {
    val snackbarHostState = remember { SnackbarHostState() }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barbershopState by barbershopViewModel.barbershopState.collectAsState()
    val clientViewModel = remember { ClientViewModel() }
    val clientState by clientViewModel.clientState.collectAsState()
    var favoriteButtonsEnabled by remember { mutableStateOf(true) }
    var isNavigating by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(SortType.NONE) }
    var showSortMenu by remember { mutableStateOf(false) }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

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
            barbershopViewModel.getFavoriteBarbershops(client.clienteid)
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
                        text = "Tus favoritas",
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
                        onClick = { showSortMenu = true },
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, Color(0xFF3B3B3B))
                            .background(Color(0xFFD9D9D9))
                            .width(150.dp)
                    ) {
                        Text(
                            text = "Ordenar por:",
                            color = Color.Black,
                            style = TextStyle(fontFamily = defaultFont, fontWeight = FontWeight.Bold),
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider(color = Color(0xFF3B3B3B), thickness = 2.dp)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Sin filtro",
                                    color = Color.Black,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                                sortType = SortType.NONE
                                showSortMenu = false
                                snackbarMessage = "Mostrando barberías sin filtrar"
                                snackbarType = SnackbarType.INFO
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (sortType == SortType.NONE) Color(0xFFB0BEC5) else Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "A-Z",
                                    color = Color.Black,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                                sortType = SortType.ALPHABETICAL
                                showSortMenu = false
                                snackbarMessage = "Barberías ordenadas de A-Z"
                                snackbarType = SnackbarType.INFO
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (sortType == SortType.ALPHABETICAL) Color(0xFFB0BEC5) else Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Calificación",
                                    color = Color.Black,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                                sortType = SortType.RATING
                                showSortMenu = false
                                snackbarMessage = "Barberías ordenadas por calificación"
                                snackbarType = SnackbarType.INFO
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (sortType == SortType.RATING) Color(0xFFB0BEC5) else Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 130.dp)
                ) {
                    when (barbershopState) {
                        is BarbershopState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .align(Alignment.Center),
                                color = Color(0xFFEAEAEA)
                            )
                        }
                        is BarbershopState.Success -> {
                            val allBarberias = (barbershopState as BarbershopState.Success).barbershops
                            val client = (clientState as? ClientState.Success)?.client

                            val filteredBarberias = if (searchQuery.isEmpty()) {
                                allBarberias
                            } else {
                                allBarberias.filter { barbershop ->
                                    barbershop.nombre.contains(searchQuery, ignoreCase = true)
                                }
                            }

                            val sortedBarberias = when (sortType) {
                                SortType.NONE -> filteredBarberias
                                SortType.ALPHABETICAL -> filteredBarberias.sortedBy { it.nombre }
                                SortType.RATING -> filteredBarberias.sortedByDescending { it.rating ?: 0f }
                            }

                            Box {
                                if (sortedBarberias.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "No results",
                                            tint = Color.White,
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No se encontraron barberías",
                                            color = Color.White,
                                            style = TextStyle(fontFamily = defaultFont),
                                            fontSize = 24.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        if (searchQuery.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Intenta con otro nombre",
                                                color = Color(0xFFD9D9D9),
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                    ) {
                                        items(sortedBarberias.size) { index ->
                                            val barbershop = sortedBarberias[index]
                                            val isFavorite = barbershop.es_favorito

                                            BarbershopCard(
                                                name = barbershop.nombre,
                                                address = barbershop.direccion,
                                                rating = barbershop.rating ?: 0f,
                                                totalRating = barbershop.cantidad_resenas,
                                                imageUrl = barbershop.imagen_url,
                                                isFavorite = isFavorite,
                                                onFavoriteClick = {
                                                    client?.let {
                                                        favoriteButtonsEnabled = false

                                                        val action = if (isFavorite) {
                                                            barbershopViewModel.removeFavorite(it.clienteid, barbershop.localid, show = "Favorites")
                                                            "eliminada de favoritos"
                                                        } else {
                                                            barbershopViewModel.addFavorite(it.clienteid, barbershop.localid, show = "Favorites")
                                                            "añadida a favoritos"
                                                        }

                                                        snackbarMessage = "\"${barbershop.nombre}\" $action"
                                                        snackbarType = SnackbarType.INFO

                                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                            favoriteButtonsEnabled = true
                                                        }, 4000)
                                                    }
                                                },
                                                favoriteButtonEnabled = favoriteButtonsEnabled
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFF5B6269))
                                            )
                                        )
                                )
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
                        .size(90.dp)
                        .align(Alignment.Top)
                        .background(Color(0xA9FFFFFF), shape = RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmarks,
                        contentDescription = "Favoritos",
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
                        .size(75.dp)
                        .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
                        .clickable {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("client_dates/$userId")
                            }
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