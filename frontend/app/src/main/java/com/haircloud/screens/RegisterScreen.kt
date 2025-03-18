package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R

@Composable
fun RegisterScreen(navController: NavController) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 36.dp, end = 36.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "HairCloud",
                    style = TextStyle(fontFamily = headersFont, fontSize = 55.sp),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(90.dp).wrapContentHeight(),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                if (step == 1) "Crear una cuenta" else "Completar registro",
                color = Color(0XFF132946),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = headersFont,
                    fontSize = 35.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .height(700.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    .background(
                        Color(0x8DFFFFFF),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    )
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {
                if (step == 1) {
                    InputField("Nombre completo", name) { name = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField("Email", email, isEmail = true) { email = it }
                } else {
                    InputField("Nombre de usuario", username) { username = it }
                    Spacer(modifier = Modifier.height(16.dp))
                    PasswordField(password) { password = it }
                }

                Spacer(modifier = Modifier.height(35.dp))

                val isStep1Filled = name.isNotBlank() && email.isNotBlank()
                val isStep2Filled = username.isNotBlank() && password.isNotBlank()

                Button(
                    onClick = {
                        if (step == 1) step = 2 else navController.navigate("login")
                    },
                    enabled = if (step == 1) isStep1Filled else isStep2Filled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0XFF2C2C2C),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0XFF646464),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        if (step == 1) "Siguiente" else "Registrarme",
                        color = Color.White,
                        style = defaultStyle,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (step == 2) {
                        Text(
                            text = "Volver",
                            style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { step = 1 }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(text = "¿Ya tienes una cuenta?", style = defaultStyle)
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
            }
        }
    }
}

@Composable
fun InputField(label: String, value: String, isEmail: Boolean = false, isPassword: Boolean = false, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = TextStyle(fontSize = 23.sp, fontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))), modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = when {
                        isEmail -> "example@gmail.com"
                        isPassword -> "****"
                        else -> "Introducir"
                    },
                    fontSize = 23.sp
                )
            },
            textStyle = TextStyle(fontSize = 23.sp, fontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))),
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
    }
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(true) }

    Column {
        Text("Contraseña", style = TextStyle(fontSize = 23.sp, fontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))), modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text("****", fontSize = 23.sp) },
            textStyle = TextStyle(fontSize = 23.sp, fontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Color(0XFF132946),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFDEDEDE),
                focusedContainerColor = Color(0xFFAFC9E1),
                unfocusedBorderColor = Color(0xFF646464),
                focusedBorderColor = Color(0xFF77AEE2),
            )
        )
    }
}
