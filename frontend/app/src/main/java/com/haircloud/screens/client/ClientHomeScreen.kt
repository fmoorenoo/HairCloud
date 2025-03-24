package com.haircloud.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ClientHomeScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val headersFont = FontFamily(Font(R.font.headers_font, FontWeight.Normal))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(key1 = true) {
        snackbarHostState.showTypedSnackbar(
            message = "Inicio de sesión exitoso",
            type = SnackbarType.SUCCESS
        )
    }

    Box(
        modifier = Modifier
            .background(brush = blackWhiteGradient)
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
                        .wrapContentHeight(),
                    contentScale = ContentScale.Inside
                )
                Image(
                    painter = painterResource(id = R.drawable.user_profile_1),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .height(55.dp)
                        .wrapContentHeight(),
                    contentScale = ContentScale.Inside
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Barberías",
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
                    onClick = {},
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
                    .size(75.dp)
                    .background(Color(0x8BB6B6B6), shape = RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmarks,
                    contentDescription = "Favoritos",
                    tint = Color(0xFF282828),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(45.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.Top)
                    .background(Color(0xA9FFFFFF), shape = RoundedCornerShape(20.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.scissors_icon),
                    contentDescription = "Barberías",
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

        // Snackbar
        CustomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
            defaultFont = defaultFont
        )
    }
}