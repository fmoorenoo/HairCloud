package com.haircloud.screens.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haircloud.data.model.ServiceResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.AddReviewState
import com.haircloud.viewmodel.ClientState
import com.haircloud.viewmodel.ClientViewModel
import com.haircloud.viewmodel.DeleteReviewState
import com.haircloud.viewmodel.ReviewsState
import java.util.Locale

@Composable
fun BarberInfoScreen(navController: NavController, userId: Int?, localId: Int?) {
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val barbershopViewModel: BarbershopViewModel = viewModel()
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()
    val clientViewModel = remember { ClientViewModel() }
    val clientState by clientViewModel.clientState.collectAsState()
    var clienteId by remember { mutableIntStateOf(-1) }
    var infoSectionExpanded by remember { mutableStateOf(true) }
    var selectedService by remember { mutableStateOf<ServiceResponse?>(null) }
    var showSelectionCard by remember { mutableStateOf(false) }
    var showReviews by remember { mutableStateOf(false) }
    var showAddReviewForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteReviewState by barbershopViewModel.deleteReviewState.collectAsState()
    val addReviewState by barbershopViewModel.addReviewState.collectAsState()

    LaunchedEffect(addReviewState) {
        when (addReviewState) {
            is AddReviewState.Success -> {
                showAddReviewForm = false
                snackbarHostState.showTypedSnackbar(
                    message = "Reseña enviada con éxito",
                    type = SnackbarType.SUCCESS
                )
                barbershopViewModel.resetAddReviewState()
            }
            is AddReviewState.Error -> {
                snackbarHostState.showTypedSnackbar(
                    message = (addReviewState as AddReviewState.Error).message,
                    type = SnackbarType.ERROR
                )
                barbershopViewModel.resetAddReviewState()
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteReviewState) {
        when (deleteReviewState) {
            is DeleteReviewState.Success -> {
                snackbarHostState.showTypedSnackbar(
                    message = "Reseña eliminada con éxito",
                    type = SnackbarType.SUCCESS
                )
                barbershopViewModel.resetDeleteReviewState()
            }
            is DeleteReviewState.Error -> {
                snackbarHostState.showTypedSnackbar(
                    message = (deleteReviewState as DeleteReviewState.Error).message,
                    type = SnackbarType.ERROR
                )
                barbershopViewModel.resetDeleteReviewState()
            }
            else -> {}
        }
    }


    LaunchedEffect(userId) {
        userId?.let {
            clientViewModel.getClient(it)
        }
    }
    LaunchedEffect(clientState) {
        if (clientState is ClientState.Success) {
            clienteId = (clientState as ClientState.Success).client.clienteid
        }
    }

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(clienteId, localId) {
        if (localId != null) {
            barbershopViewModel.getBarbershopById(clienteId, localId)
        }
    }

    LaunchedEffect(singleBarbershopState) {
        if (singleBarbershopState is SingleBarbershopState.Success) {
            val barbershop = (singleBarbershopState as SingleBarbershopState.Success).barbershop
            isFavorite = barbershop.es_favorito
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
                    onClick = {
                        if (localId != null) {
                            if (isFavorite) {
                                barbershopViewModel.removeFavorite(clienteId, localId, show = "One")
                            } else {
                                barbershopViewModel.addFavorite(clienteId, localId, show = "One")
                            }
                            barbershopViewModel.getBarbershopById(userId!!, localId)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (singleBarbershopState) {
                is SingleBarbershopState.Success -> {
                    val barbershop = (singleBarbershopState as SingleBarbershopState.Success).barbershop

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 10.dp
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (!showReviews) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Ver reseñas",
                                        tint = Color(0xFF3D8EE6),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .size(30.dp)
                                            .clickable {
                                                showReviews = true
                                            }
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedVisibility(
                                        visible = !showReviews,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f", barbershop.rating ?: 0f),
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 42.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                StarRating(barbershop.rating ?: 0f, "")
                                                Text(
                                                    text = "(${barbershop.cantidad_resenas} ${if (barbershop.cantidad_resenas == 1) "reseña" else "reseñas"})",
                                                    style = TextStyle(fontFamily = defaultFont),
                                                    fontSize = 16.sp,
                                                    color = Color(0xFFD9D9D9)
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = showReviews,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { showAddReviewForm = !showAddReviewForm },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterEnd)
                                                        .size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (showAddReviewForm) Icons.Default.Remove else Icons.Default.Add,
                                                        contentDescription = if (showAddReviewForm) "Ocultar formulario" else "Añadir reseña",
                                                        tint = Color(0xFF3D8EE6)
                                                    )
                                                }
                                                Text(
                                                    text = "Reseñas",
                                                    modifier = Modifier.align(Alignment.Center),
                                                    style = TextStyle(
                                                        fontFamily = defaultFont,
                                                        fontSize = 30.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier
                                                    .clickable { showReviews = false }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VisibilityOff,
                                                    contentDescription = "Ocultar reseñas",
                                                    tint = Color(0xFF3D8EE6),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Ocultar reseñas",
                                                    style = TextStyle(fontFamily = defaultFont),
                                                    fontSize = 16.sp,
                                                    color = Color(0xFF3D8EE6)
                                                )
                                            }
                                            AnimatedVisibility(
                                                visible = showAddReviewForm,
                                                enter = expandVertically() + fadeIn(),
                                                exit = shrinkVertically() + fadeOut()
                                            ) {
                                                AddReviewForm(
                                                    clienteId = clienteId,
                                                    localId = localId ?: 0,
                                                    barbershopViewModel = barbershopViewModel
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!showReviews) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { infoSectionExpanded = !infoSectionExpanded },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF3D8EE6)
                                ),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (!infoSectionExpanded) 16.dp else 0.dp,
                                    bottomEnd = if (!infoSectionExpanded) 16.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Información de la barbería",
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = if (infoSectionExpanded)
                                            Icons.Default.KeyboardArrowUp
                                        else
                                            Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (infoSectionExpanded)
                                            "Ocultar información"
                                        else
                                            "Mostrar información",
                                        tint = Color.White
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = infoSectionExpanded,
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
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        InfoRow(
                                            label = "Horario",
                                            value = "${barbershop.horarioapertura.substring(0, 5)} - ${barbershop.horariocierre.substring(0, 5)}",
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
                                            value = if (barbershop.puntos_habilitados) "Habilitado - Tienes ${barbershop.cantidad_puntos ?: 0} puntos" else "No habilitado",
                                            icon = Icons.Filled.CheckCircle
                                        )
                                    }
                                }
                            }

                            ServicesSection(
                                localId = localId,
                                onServiceSelected = { service ->
                                    infoSectionExpanded = false
                                    selectedService = service
                                    showSelectionCard = service != null
                                }
                            )
                            AnimatedVisibility(
                                visible = showSelectionCard && selectedService != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF3D8EE6)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 10.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedService?.nombre ?: "",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF151515)
                                            )

                                            Text(
                                                text = "${selectedService?.precio ?: 0.0}€",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF151515)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                selectedService?.let { service ->
                                                    navController.navigate("client_booking/${userId}/${localId}/${service.servicioid}")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF151515),
                                                contentColor = Color(0xFF3D8EE6)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Continuar",
                                                style = TextStyle(fontFamily = defaultFont),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            ReviewsSection(
                                rating = barbershop.rating ?: 0f,
                                totalReviews = barbershop.cantidad_resenas,
                                barbershopViewModel = barbershopViewModel,
                                currentClienteId = clienteId
                            )
                        }
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
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Error al cargar la información",
                            color = Color.White,
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
        CustomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
            defaultFont = defaultFont
        )
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
                .padding(end = 12.dp, top = 2.dp)
                .size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp,
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
                    fontSize = 18.sp,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3D8EE6),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 16.sp,
                    color = Color(0xFFAAAAAA)
                )
                Text(
                    text = if (expanded) value else if (value.length > 60) value.take(60) + "..." else value,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    color = Color.White,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Mostrar menos" else "Mostrar más",
                tint = Color(0xFF3D8EE6),
                modifier = Modifier.size(24.dp)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
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
                modifier = Modifier.size(23.dp)
            )
        }
        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 19.sp,
                color = Color(0xFFD9D9D9)
            )
        }
    }
}

@Composable
fun ClickableStarRating(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Rate ${index + 1} stars",
                tint = Color(0xFF3D8EE6),
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        onRatingChanged((index + 1).toFloat())
                    }
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun AddReviewForm(
    clienteId: Int,
    localId: Int,
    barbershopViewModel: BarbershopViewModel
) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    var rating by remember { mutableFloatStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    val reviewsState by barbershopViewModel.reviewsState.collectAsState()

    LaunchedEffect(reviewsState) {
        when (reviewsState) {
            is ReviewsState.Success -> {
            }
            is ReviewsState.Error -> {
            }
            else -> {}
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tu valoración",
                style = TextStyle(fontFamily = defaultFont, fontSize = 18.sp, color = Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClickableStarRating(
                rating = rating,
                onRatingChanged = { newRating ->
                    rating = newRating
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = {
                    Text("Comentario", color = Color(0xFFAAAAAA), fontFamily = defaultFont)
                },
                textStyle = TextStyle(fontFamily = defaultFont, fontSize = 16.sp, color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1F1F1F),
                    unfocusedContainerColor = Color(0xFF1F1F1F),
                    focusedBorderColor = Color(0xFF3D8EE6),
                    unfocusedBorderColor = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    barbershopViewModel.addReview(clienteId, localId, rating.toDouble(), comment)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3D8EE6),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color(0xFF888888)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = rating > 0
            ) {
                Text(
                    text = if (reviewsState is ReviewsState.Loading) "Enviando..." else "Enviar reseña",
                    style = TextStyle(fontFamily = defaultFont, fontSize = 16.sp)
                )
            }
            if (reviewsState is ReviewsState.Loading) {
                CircularProgressIndicator(
                    color = Color(0xFF3D8EE6),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}