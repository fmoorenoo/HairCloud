package com.haircloud.screens.barber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.ReviewResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.calculateRatingCounts
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.DeleteReviewState
import com.haircloud.viewmodel.ReviewsState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarbershopReviewsScreen(navController: NavController, localId: Int, isAdmin: Boolean) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val barbershopViewModel: BarbershopViewModel = viewModel()
    val reviewsState by barbershopViewModel.reviewsState.collectAsState()
    val deleteReviewState by barbershopViewModel.deleteReviewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    var totalRating by remember { mutableFloatStateOf(0f) }
    var totalReviews by remember { mutableIntStateOf(0) }

    LaunchedEffect(deleteReviewState) {
        when (deleteReviewState) {
            is DeleteReviewState.Success -> {
                snackbarHostState.showTypedSnackbar(
                    message = "Reseña eliminada con éxito",
                    type = SnackbarType.SUCCESS
                )
                barbershopViewModel.resetDeleteReviewState()
                barbershopViewModel.getBarbershopReviews(localId)
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

    LaunchedEffect(localId) {
        barbershopViewModel.getBarbershopReviews(localId)
    }

    LaunchedEffect(reviewsState) {
        if (reviewsState is ReviewsState.Success) {
            val reviews = (reviewsState as ReviewsState.Success).reviews
            if (reviews.isNotEmpty()) {
                totalRating = reviews.sumOf { it.calificacion }.toFloat() / reviews.size
                totalReviews = reviews.size
            } else {
                totalRating = 0f
                totalReviews = 0
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blackWhiteGradient),
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 30.dp),
                defaultFont = defaultFont
            )
        },
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Administrar reseñas",
                        style = TextStyle(
                            fontFamily = defaultFont,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = blackWhiteGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ReviewsRatingHeader(rating = totalRating, totalReviews = totalReviews, defaultFont = defaultFont)

                Spacer(modifier = Modifier.height(16.dp))

                when (reviewsState) {
                    is ReviewsState.Loading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    is ReviewsState.Success -> {
                        val reviews = (reviewsState as ReviewsState.Success).reviews
                        if (reviews.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A2A2A)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                ) {
                                    Text(
                                        text = "No hay reseñas disponibles para esta barbería",
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            reviews.forEach { review ->
                                BarberReviewCard(
                                    review = review,
                                    onDeleteReview = { resenaId ->
                                        barbershopViewModel.deleteReview(
                                            resenaId,
                                            localId,
                                            -1
                                        )
                                    },
                                    defaultFont = defaultFont,
                                    isAdmin = isAdmin
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    is ReviewsState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = (reviewsState as ReviewsState.Error).message,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        barbershopViewModel.getBarbershopReviews(localId)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3D8EE6),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Reintentar",
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 16.sp
                                    )
                                }
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
}

@Composable
fun ReviewsRatingHeader(rating: Float, totalReviews: Int, defaultFont: FontFamily) {
    val viewModel: BarbershopViewModel = viewModel()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val ratingCounts = when (reviewsState) {
        is ReviewsState.Success -> {
            val reviews = (reviewsState as ReviewsState.Success).reviews
            calculateRatingCounts(reviews)
        }
        else -> mapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Valoraciones",
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", rating),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    StarRating(rating = rating)
                    Text(
                        text = "$totalReviews ${if (totalReviews == 1) "reseña" else "reseñas"}",
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp,
                        color = Color(0xFFD9D9D9)
                    )
                }

                Column(
                    modifier = Modifier.weight(0.6f)
                ) {
                    Text(
                        text = "Estrellas  |  Votos",
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    for (i in 5 downTo 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "$i",
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.width(20.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                            ) {
                                val totalVotes = ratingCounts.values.sum().coerceAtLeast(1)
                                val votes = ratingCounts[i] ?: 0
                                val percentage = votes.toFloat() / totalVotes.toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percentage)
                                        .background(
                                            Color(0xFF3D8EE6),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            Text(
                                text = "${ratingCounts[i] ?: 0}",
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .width(40.dp)
                                    .padding(start = 8.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarberReviewCard(
    review: ReviewResponse,
    onDeleteReview: (Int) -> Unit,
    defaultFont: FontFamily,
    isAdmin: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF393939))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Cliente",
                        tint = Color(0xFF3D8EE6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = review.cliente_nombre ?: "Cliente anónimo",
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatFecha(review.fecharesena),
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 15.sp,
                    color = Color(0xFFAAAAAA),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(review.calificacion.toFloat())

                if (isAdmin) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar reseña",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            val desc = review.comentario?.trim().takeIf { it?.isNotBlank() == true } ?: "Sin comentario"
            val isDescEmpty = review.comentario.isNullOrBlank()

            Column {
                Text(
                    text = desc,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 16.sp,
                    color = if (isDescEmpty) Color(0xFFAAAAAA) else Color.White,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
                )

                if (!isDescEmpty && desc.length > 80) {
                    Text(
                        text = if (expanded) "Ver menos" else "Ver más",
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3D8EE6),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expanded = !expanded }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Eliminar reseña",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        fontSize = 18.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteReview(review.resenaid)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text(
                        text = "Eliminar",
                        style = TextStyle(
                            fontFamily = defaultFont,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancelar",
                        style = TextStyle(
                            fontFamily = defaultFont,
                            color = Color.White
                        )
                    )
                }
            },
            containerColor = Color(0xFF2A2A2A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFD9D9D9)
        )
    }
}

@Composable
fun StarRating(rating: Float) {
    val roundedRating = (rating * 2).toInt() / 2f
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val star = when {
                index < roundedRating.toInt() -> Icons.Default.Star
                index == roundedRating.toInt() && roundedRating % 1 == 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = star,
                contentDescription = "Star $index",
                tint = Color(0xFF3D8EE6),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

fun formatFecha(fecha: String?): String {
    if (fecha == null) return "Fecha desconocida"

    return try {
        val fechaParts = fecha.split("T")[0].split("-")
        val year = fechaParts[0]
        val month = fechaParts[1]
        val day = fechaParts[2]

        "$day/$month/$year"
    } catch (_: Exception) {
        "Fecha desconocida"
    }
}