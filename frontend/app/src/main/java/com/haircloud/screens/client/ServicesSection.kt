package com.haircloud.screens.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import com.haircloud.data.model.ServiceResponse
import com.haircloud.viewmodel.ServiceState

@Composable
fun ServicesSection(navController: NavController, userId: Int?, localId: Int?) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val barbershopViewModel = remember { BarbershopViewModel() }
    val servicesState by barbershopViewModel.servicesState.collectAsState()
    var servicesSectionExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(localId) {
        if (localId != null) {
            barbershopViewModel.getServicesById(localId)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable { servicesSectionExpanded = !servicesSectionExpanded },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF3D8EE6)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (!servicesSectionExpanded) 16.dp else 0.dp,
                bottomEnd = if (!servicesSectionExpanded) 16.dp else 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Servicios disponibles",
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Icon(
                    imageVector = if (servicesSectionExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = if (servicesSectionExpanded)
                        "Ocultar servicios"
                    else
                        "Mostrar servicios",
                    tint = Color.White
                )
            }
        }

        AnimatedVisibility(
            visible = servicesSectionExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                when (servicesState) {
                    is ServiceState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is ServiceState.Success -> {
                        val services = (servicesState as ServiceState.Success).services
                        if (services.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay servicios disponibles",
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 16.sp,
                                    color = Color(0xFFAAAAAA),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 300.dp)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(services.size) { index ->
                                    ServiceItem(service = services[index])
                                }
                            }
                        }
                    }
                    is ServiceState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (servicesState as ServiceState.Error).message,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp,
                                color = Color(0xFFFF5252),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (localId != null) {
                                        barbershopViewModel.getServicesById(localId)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD9D9D9),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
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
}

@Composable
fun ServiceItem(service: ServiceResponse) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F1F1F)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.nombre,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (service.descripcion != null) {
                    Text(
                        text = service.descripcion,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${service.duracion} min",
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 14.sp,
                    color = Color(0xFF3D8EE6)
                )

            }
            Text(
                text = "${service.precio.toInt()}€",
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}