package com.haircloud.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import com.haircloud.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    token: String?,
    userId: Int?,
    role: String?
) {
    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val headersFont = FontFamily(
        Font(R.font.headers_font, FontWeight.Normal)
    )
    LaunchedEffect(token, userId, role) {
        delay(1000)
        when {
            token.isNullOrEmpty() || userId == null -> {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            role == "cliente" -> {
                navController.navigate("client_home/$userId") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            else -> {
                navController.navigate("barber_home/$userId") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(brush = blackWhiteGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "HairCloud",
                style = TextStyle(fontFamily = headersFont, fontSize = 55.sp),
                textAlign = TextAlign.Center,
                color = Color(0xFFFFFFFF),
            )
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.height(70.dp).wrapContentHeight(),
                contentScale = ContentScale.Inside
            )
        }
    }
}