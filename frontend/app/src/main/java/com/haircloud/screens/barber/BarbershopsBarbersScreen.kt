package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.BarberResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.BarbersState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarbershopBarbersScreen(navController: NavController, localId: Int, userId: Int?, isAdmin: Boolean = false) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val barbersState by barbershopViewModel.barbersState.collectAsState()

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

    LaunchedEffect(localId) {
        barbershopViewModel.getBarbersByLocalId(localId)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isAdmin) "Administrar peluqueros" else "Peluqueros",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (barbersState) {
                    is BarbersState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is BarbersState.Error -> {
                        val errorMessage = (barbersState as BarbersState.Error).message
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }
                    }
                    is BarbersState.Success -> {
                        var searchQuery by remember { mutableStateOf("") }

                        val query = searchQuery.lowercase()

                        val barbers = (barbersState as BarbersState.Success).barbers

                        val youFiltered = barbers.find { it.usuarioid == userId }?.takeIf {
                            it.nombre.lowercase().contains(query) ||
                                    it.telefono?.contains(query) == true ||
                                    it.fechacontratacion.lowercase().contains(query)
                        }
                        val othersFiltered = barbers.filter {
                            it.usuarioid != userId && (
                                    it.nombre.lowercase().contains(query) ||
                                            it.telefono?.contains(query) == true ||
                                            it.fechacontratacion.lowercase().contains(query)
                                    )
                        }

                        val filteredBarbers = listOfNotNull(youFiltered) + othersFiltered

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscador (nombre, teléfono o fecha)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF444444),
                                unfocusedBorderColor = Color(0xFF666666),
                                focusedLabelColor = Color(0xFFAAAAAA),
                                unfocusedLabelColor = Color(0xFF888888),
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        if (filteredBarbers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = "Sin resultados",
                                        tint = Color.Gray.copy(alpha = 0.6f),
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se encontraron peluqueros",
                                        color = Color.Gray.copy(alpha = 0.8f),
                                        fontSize = 19.sp,
                                        fontFamily = defaultFont
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredBarbers) { barber ->
                                    BarberCard(barber = barber, defaultFont = defaultFont, userId = userId)
                                }
                                item { Spacer(modifier = Modifier.height(40.dp)) }
                            }
                        }
                    }
                    else -> {  }
                }
            }
        }
    }
}

@Composable
fun BarberCard(barber: BarberResponse, defaultFont: FontFamily, userId: Int?) {
    val isYou = userId == barber.usuarioid
    val backgroundColor = if (isYou) Color(0xFF4F4F4F) else Color(0xFF2C2C2C)
    val borderColor = if (isYou) Color(0xFFFFFFFF) else Color.Transparent
    val textColor = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user_profile_1),
                        contentDescription = "Imagen del peluquero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isYou) "${barber.nombre} (Tú)" else barber.nombre,
                        color = textColor,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = barber.especialidad ?: "Sin especialidad",
                        color = textColor.copy(alpha = 0.8f),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Teléfono",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = barber.telefono ?: "No disponible",
                        color = textColor,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha de contratación",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = barber.fechacontratacion,
                        color = textColor,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

