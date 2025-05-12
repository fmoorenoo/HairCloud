package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.InactiveBarbersState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddBarberScreen(navController: NavController, localId: Int, userId: Int?, isAdmin: Boolean = false) {
    val barberViewModel: BarberViewModel = viewModel()
    val inactiveBarbersState by barberViewModel.inactiveBarbersState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF1E1E1E)

    var isNavigating by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    data class BarberToActivate(val usuarioId: Int, val nombre: String, val especialidad: String?)

    var showActivationDialog by remember { mutableStateOf<BarberToActivate?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }


    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(Unit) {
        barberViewModel.getInactiveBarbers()
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkSurface.copy(alpha = 0.8f))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
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
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Añadir peluquero",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = inactiveBarbersState) {
                    is InactiveBarbersState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is InactiveBarbersState.Error -> {
                        val message = state.message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message,
                                color = Color.Red,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }
                    }
                    is InactiveBarbersState.Success -> {
                        val barbers = state.barbers

                        val filteredBarbers = barbers.filter {
                            searchQuery.isEmpty() ||
                                    it.nombre.lowercase().contains(searchQuery.lowercase())
                        }

                        Column {
                            Text(
                                text = "Peluqueros anteriores",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = defaultFont,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar peluquero") },
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
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = "Sin resultados",
                                            tint = Color.Gray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No se encontraron peluqueros anteriores",
                                            color = Color.Gray.copy(alpha = 0.8f),
                                            fontSize = 14.sp,
                                            fontFamily = defaultFont
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredBarbers, key = { it.usuarioid }) { barber ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showActivationDialog = BarberToActivate(
                                                        barber.usuarioid,
                                                        barber.nombre,
                                                        barber.especialidad
                                                    )
                                                }
                                                .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp)),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.DarkGray)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(12.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = barber.nombre,
                                                        color = Color.White,
                                                        style = TextStyle(fontFamily = defaultFont),
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = barber.especialidad ?: "Sin especialidad",
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        style = TextStyle(fontFamily = defaultFont),
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(16.dp)) }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Añadir peluquero nuevo",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = defaultFont,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color(0xFF2C2C2C), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pendiente...",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontFamily = defaultFont
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        showActivationDialog?.let { barberToActivate ->
            Dialog(onDismissRequest = { showActivationDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Activar Peluquero",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = defaultFont,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "¿Estás seguro de que quieres activar a ${barberToActivate.nombre}?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = defaultFont,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { showActivationDialog = null },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        barberViewModel.activateBarber(barberToActivate.usuarioId)
                                        delay(500)
                                        barberViewModel.getInactiveBarbers()
                                        snackbarType = SnackbarType.SUCCESS
                                        snackbarMessage = "${barberToActivate.nombre} añadido al personal"
                                    }

                                    showActivationDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Confirmar")
                            }
                        }
                    }
                }
            }
        }
    }
}