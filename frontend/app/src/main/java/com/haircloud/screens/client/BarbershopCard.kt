package com.haircloud.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haircloud.R
import kotlin.math.roundToInt


@Composable
fun BarbershopCard(
    name: String,
    address: String,
    rating: Float,
    totalRating: Int,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false,
    favoriteButtonEnabled: Boolean = true
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

            IconButton(
                onClick = {
                    isMarkedFavorite = !isMarkedFavorite
                    onFavoriteClick()
                },
                enabled = favoriteButtonEnabled,
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