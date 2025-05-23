package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.LoginState
import com.haircloud.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginState by authViewModel.loginState.collectAsState()
    var isNavigating by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val headersFont = FontFamily(
        Font(R.font.headers_font, FontWeight.Normal)
    )
    val defaultFont = FontFamily(
        Font(R.font.default_font, FontWeight.Normal)
    )
    val defaultStyle = TextStyle(
        fontFamily = defaultFont,
        fontSize = 18.sp,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = blackWhiteGradient
            ),
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

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Iniciar Sesión",
                color = Color(0xFFDADADA),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = headersFont,
                    fontSize = 30.sp,
                    shadow = Shadow(
                        color = Color(0xFF212121),
                        offset = Offset(3f, 10f),
                        blurRadius = 15f
                    )
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
                Text(
                    "Nombre de usuario",
                    style = defaultStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    placeholder = { Text("Introducir", style = defaultStyle) },
                    textStyle = TextStyle(fontSize = 18.sp, fontFamily = defaultFont),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFDEDEDE),
                        focusedContainerColor = Color(0xFFAFC9E1),
                        unfocusedBorderColor =  Color(0xFF646464),
                        focusedBorderColor = Color(0xFF77AEE2),
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Contraseña", style = defaultStyle, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    placeholder = { Text("*****", style = defaultStyle) },
                    textStyle = TextStyle(fontSize = 18.sp, fontFamily = defaultFont),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (password.isNotEmpty()) {
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
                Spacer(modifier = Modifier.height(35.dp))
                val isFormFilled = username.isNotBlank() && password.isNotBlank()
                Button(
                    onClick = {
                        authViewModel.login(username, password)
                    },
                    enabled = isFormFilled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0XFF2C2C2C),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0XFF646464),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Entrar", color = Color.White, style = defaultStyle, modifier = Modifier.padding(vertical = 6.dp))
                }

                // Manejar estados del login
                LaunchedEffect(loginState) {
                    when (loginState) {
                        is LoginState.Success -> {
                            val role = (loginState as LoginState.Success).response.rol
                            val userId = (loginState as LoginState.Success).response.usuarioid
                            authViewModel.resetLoginState()
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate(if (role == "cliente") "client_home/$userId" else "barber_home/$userId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                        is LoginState.Error -> {
                            snackbarHostState.showTypedSnackbar(
                                message = (loginState as LoginState.Error).message,
                                type = SnackbarType.ERROR
                            )
                            authViewModel.resetLoginState()
                        }
                        else -> {}
                    }
                }
                if (loginState is LoginState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFF2879E3),
                            strokeWidth = 5.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = "He olvidado mi contraseña",
                        style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            authViewModel.resetLoginState()
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("forgot_password")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        "¿Todavía no tienes una cuenta?",
                        style = defaultStyle
                    )
                    Text(
                        text = "Regístrate gratis",
                        style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            authViewModel.resetLoginState()
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigate("register")
                            }
                        }
                    )
                }
            }
        }

        // SnackbarHost para mostrar el Snackbar
        CustomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
            defaultFont = defaultFont
        )
    }
}