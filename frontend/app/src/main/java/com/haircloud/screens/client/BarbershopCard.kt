package com.haircloud.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.haircloud.R
import kotlin.math.roundToInt


@Composable
fun BarbershopCard(
    modifier: Modifier = Modifier,
    name: String,
    address: String,
    rating: Float,
    totalRating: Int,
    imageUrl: String? = null,
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false,
    favoriteButtonEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    var isMarkedFavorite by remember { mutableStateOf(isFavorite) }
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBEBEBE)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(110.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .size(60)
                        .crossfade(true)
                        .build()
                )

                val state = painter.state

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFCCCCCC)),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        is AsyncImagePainter.State.Success -> {
                            Image(
                                painter = painter,
                                contentDescription = "Imagen de $name",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.scissors_icon),
                                contentDescription = "Barbería",
                                tint = Color(0xFF282828),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }



                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = name,
                        fontFamily = defaultFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF1C1C1C)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = address,
                        fontSize = 16.sp,
                        color = Color(0xFF4D4D4D)
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
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
                                modifier = Modifier.size(23.dp)
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
            }

            IconButton(
                onClick = {
                    isMarkedFavorite = !isMarkedFavorite
                    onFavoriteClick()
                },
                enabled = favoriteButtonEnabled,
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isMarkedFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Favorito",
                    tint = Color(0xFF282828),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}
