package com.haircloud.screens.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import com.haircloud.R
import com.haircloud.viewmodel.BarbershopViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.haircloud.data.model.ServiceResponse
import com.haircloud.viewmodel.ServiceState

@Composable
fun ServicesSection(
    localId: Int?,
    onServiceSelected: (ServiceResponse) -> Unit
) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val barbershopViewModel = remember { BarbershopViewModel() }
    val servicesState by barbershopViewModel.servicesState.collectAsState()
    var servicesSectionExpanded by remember { mutableStateOf(true) }
    var selectedServiceId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedService by remember { mutableStateOf<ServiceResponse?>(null) }

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
                    fontSize = 23.sp,
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Buscar servicios",
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 18.sp,
                                color = Color(0xFFAAAAAA)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color(0xFFAAAAAA)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3D8EE6),
                            unfocusedBorderColor = Color(0xFF666666),
                            focusedContainerColor = Color(0xFF151515),
                            unfocusedContainerColor = Color(0xFF151515),
                            cursorColor = Color(0xFF3D8EE6),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = defaultFont,
                            fontSize = 18.sp
                        ),
                    )

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
                            val allServices = (servicesState as ServiceState.Success).services
                            val filteredServices = if (searchQuery.text.isNotEmpty()) {
                                allServices.filter {
                                    it.nombre.contains(searchQuery.text, ignoreCase = true)
                                }
                            } else {
                                allServices
                            }

                            if (filteredServices.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.text.isNotEmpty())
                                            "No se encontraron servicios con ese nombre"
                                        else
                                            "No hay servicios disponibles",
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
                                        .padding(vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredServices.size) { index ->
                                        val service = filteredServices[index]
                                        val isSelected = selectedServiceId == service.servicioid

                                        ServiceItem(
                                            service = service,
                                            isSelected = isSelected,
                                            onServiceClick = {
                                                selectedServiceId = if (isSelected) null else service.servicioid
                                                if (!isSelected) {
                                                    selectedService = service
                                                    onServiceSelected(service)
                                                } else {
                                                    selectedService = null
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is ServiceState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
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
}

@Composable
fun ServiceItem(
    service: ServiceResponse,
    isSelected: Boolean,
    onServiceClick: () -> Unit
) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onServiceClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF3D8EE6))
                )
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = service.nombre,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color(0xFF3D8EE6) else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = " • ${service.duracion} min",
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 17.sp,
                            color = Color(0xFF3D8EE6)
                        )
                    }

                    if (service.descripcion != null) {
                        Text(
                            text = service.descripcion,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "${service.precio}€",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF3D8EE6)
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }
        }
    }
}