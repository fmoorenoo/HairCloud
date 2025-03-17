package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val blueWhiteGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF77AEE2), Color(0xFFFFFFFF))
    )
    val headersFont = FontFamily(Font(R.font.headers_font, FontWeight.Normal))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val defaultStyle = TextStyle(fontFamily = defaultFont, fontSize = 23.sp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blueWhiteGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 36.dp, end = 36.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "HairCloud",
                    style = TextStyle(fontFamily = headersFont, fontSize = 55.sp),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(90.dp),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Crear una cuenta",
                color = Color(0XFF132946),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = headersFont,
                    fontSize = 35.sp,
                    shadow = Shadow(
                        color = Color(0xFF7C7C7C),
                        offset = Offset(3f, 10f),
                        blurRadius = 15f
                    )
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0x8DFFFFFF))
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {
                items(
                    listOf(
                        "Nombre completo" to name,
                        "Email" to email,
                        "Nombre de usuario" to username,
                        "Contraseña" to password,
                        "Confirmar contraseña" to confirmPassword
                    )
                ) { (label, value) ->
                    Text(label, style = defaultStyle, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            when (label) {
                                "Nombre completo" -> name = it
                                "Email" -> email = it
                                "Nombre de usuario" -> username = it
                                "Contraseña" -> password = it
                                "Confirmar contraseña" -> confirmPassword = it
                            }
                        },
                        singleLine = true,
                        placeholder = { Text(if (label.contains("Email")) "example@gmail.com" else "Introducir", fontSize = 23.sp) },
                        textStyle = TextStyle(fontSize = 23.sp, fontFamily = defaultFont),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFDEDEDE),
                            focusedContainerColor = Color(0xFFAFC9E1),
                            unfocusedBorderColor = Color(0xFF646464),
                            focusedBorderColor = Color(0xFF77AEE2),
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0XFF2C2C2C))
                    ) {
                        Text("Registrarme", color = Color.White, style = defaultStyle, modifier = Modifier.padding(vertical = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Text("¿Ya tienes una cuenta?", style = defaultStyle)
                        Text(
                            text = "Iniciar sesión",
                            style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}
