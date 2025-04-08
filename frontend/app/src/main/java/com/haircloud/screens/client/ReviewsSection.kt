package com.haircloud.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.haircloud.data.model.ReviewResponse
import com.haircloud.utils.calculateRatingCounts
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.ReviewsState
import com.haircloud.viewmodel.SingleBarbershopState
import java.util.Locale

@Composable
fun ReviewsSection(rating: Float, totalReviews: Int, barbershopViewModel: BarbershopViewModel) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()
    val reviewsState by barbershopViewModel.reviewsState.collectAsState()
    val localId = (if (singleBarbershopState is SingleBarbershopState.Success) {
        (singleBarbershopState as SingleBarbershopState.Success).barbershop.localid
    } else null)

    LaunchedEffect(localId) {
        localId?.let {
            barbershopViewModel.getBarbershopReviews(it)
        }
    }

    val ratingCounts = when (reviewsState) {
        is ReviewsState.Success -> {
            val reviews = (reviewsState as ReviewsState.Success).reviews
            calculateRatingCounts(reviews)
        }
        else -> mapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        StarRating(rating, "")
                        Text(
                            text = "$totalReviews ${if (totalReviews == 1) "reseña" else "reseñas"}",
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 16.sp,
                            color = Color(0xFFD9D9D9)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.6f)

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
                                        .width(50.dp)
                                        .background(Color(0xFF444444), RoundedCornerShape(4.dp))
                                ) {
                                    val totalVotes = ratingCounts.values.sum().coerceAtLeast(1)
                                    val votes = ratingCounts[i] ?: 0
                                    val percentage = votes.toFloat() / totalVotes.toFloat()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(percentage)
                                            .background(Color(0xFF3D8EE6), RoundedCornerShape(4.dp))
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
                        RealReviewCard(review)
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
                                localId?.let {
                                    barbershopViewModel.getBarbershopReviews(it)
                                }
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
}

@Composable
fun RealReviewCard(review: ReviewResponse) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.cliente_nombre ?: "Cliente anónimo",
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    text = formatFecha(review.fecharesena),
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 14.sp,
                    color = Color(0xFFAAAAAA)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            StarRating(review.calificacion.toFloat(), "")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comentario ?: "Sin comentario",
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp,
                color = Color.White
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