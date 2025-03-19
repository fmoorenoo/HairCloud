package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.haircloud.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.viewmodel.ForgotPasswordViewModel
import com.haircloud.viewmodel.ForgotPasswordState

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    val forgotPasswordState by forgotPasswordViewModel.forgotPasswordState.collectAsState()
    val blueWhiteGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF77AEE2), Color(0xFFFFFFFF))
    )
    val headersFont = FontFamily(
        Font(R.font.headers_font, FontWeight.Normal)
    )
    val defaultFont = FontFamily(
        Font(R.font.default_font, FontWeight.Normal)
    )
    val defaultStyle = TextStyle(
        fontFamily = defaultFont,
        fontSize = 23.sp,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = blueWhiteGradient
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 36.dp, end = 36.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HairCloud",
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
                text = "Verificación",
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

            Column(
                modifier = Modifier
                    .height(700.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    )
                    .background(
                        Color(0x8DFFFFFF),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    )
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {
                Text(
                    text = "Email",
                    style = defaultStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    placeholder = { Text(text = "example@gmail.com", style = defaultStyle) },
                    textStyle = TextStyle(fontSize = 23.sp, fontFamily = defaultFont),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFDEDEDE),
                        focusedContainerColor = Color(0xFFAFC9E1),
                        unfocusedBorderColor = Color(0xFF646464),
                        focusedBorderColor = Color(0xFF77AEE2),
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        forgotPasswordViewModel.sendVerificationCode(email)
                    },
                    enabled = email.isNotEmpty(),
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
                        text = "Enviar código",
                        color = Color.White,
                        style = defaultStyle,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Manejo del estado del ViewModel
                when (forgotPasswordState) {
                    is ForgotPasswordState.Loading ->
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
                    is ForgotPasswordState.CodeSentSuccess -> {
                        isCodeSent = true
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = "Código enviado con éxito",
                                style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    is ForgotPasswordState.CodeSentError -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = (forgotPasswordState as ForgotPasswordState.CodeSentError).message,
                                style = defaultStyle.copy(color = Color(0xFFB74A5A), fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                    else -> {}
                }

                // Sección para ingresar el código de verificación
                if (isCodeSent) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Introduce el código recibido",
                        style = defaultStyle,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        placeholder = { Text(text = "Número de 6 dígitos", style = defaultStyle) },
                        textStyle = TextStyle(fontSize = 23.sp, fontFamily = defaultFont),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFDEDEDE),
                            focusedContainerColor = Color(0xFFAFC9E1),
                            unfocusedBorderColor = Color(0xFF646464),
                            focusedBorderColor = Color(0xFF77AEE2),
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            forgotPasswordViewModel.verifyCode(email, code)
                        },
                        enabled = code.isNotEmpty(),
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
                            text = "Verificar",
                            color = Color.White,
                            style = defaultStyle,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Manejo del estado después de verificar el código
                    when (forgotPasswordState) {
                        is ForgotPasswordState.CodeVerifiedSuccess -> {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Código verificado correctamente",
                                    style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                                )
                            }

                            // Navegar a ResetPasswordScreen cuando el código sea verificado
                            LaunchedEffect(forgotPasswordState) {
                                if (forgotPasswordState is ForgotPasswordState.CodeVerifiedSuccess) {
                                    val verifiedCode = code
                                    forgotPasswordViewModel.resetForgotPasswordState()
                                    navController.navigate("reset_password/$email/$verifiedCode")
                                }
                            }
                        }

                        is ForgotPasswordState.CodeVerifiedError -> {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = (forgotPasswordState as ForgotPasswordState.CodeVerifiedError).message,
                                    style = defaultStyle.copy(color = Color(0xFFB74A5A), fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        else -> {}
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "Volver",
                        style = defaultStyle.copy(color = Color(0XFF2879E3),fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            forgotPasswordViewModel.resetForgotPasswordState()
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}