package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.utils.CredentialsValidator
import com.haircloud.viewmodel.ForgotPasswordViewModel
import com.haircloud.viewmodel.ForgotPasswordState
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.RequirementsDialog
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(navController: NavController, email: String, code: String, username: String) {
    var newPassword by remember { mutableStateOf("") }
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
    val forgotPasswordState by forgotPasswordViewModel.forgotPasswordState.collectAsState()
    val isPasswordValid = CredentialsValidator.isPasswordValid(newPassword)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var buttonsEnabled by remember { mutableStateOf(true) }
    var isNavigating by remember { mutableStateOf(false) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val headersFont = FontFamily(Font(R.font.headers_font, FontWeight.Normal))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val defaultStyle = TextStyle(fontFamily = defaultFont, fontSize = 18.sp)

    Box(
        modifier = Modifier.fillMaxSize().background(brush = blackWhiteGradient),
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
                text = "Cambiar contraseña",
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
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0x8DFFFFFF), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {
                Text(
                    text = "Nombre de usuario",
                    style = defaultStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 23.sp, fontFamily = defaultFont),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color(0xFF494949)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF919191),
                        focusedContainerColor = Color(0xFF919191),
                        unfocusedBorderColor = Color(0xFF646464),
                        focusedBorderColor = Color(0xFF646464),
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nueva contraseña",
                    style = defaultStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    placeholder = { Text(text = "*****", style = defaultStyle) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 23.sp, fontFamily = defaultFont),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (newPassword.isNotEmpty()) {
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
                var showDialog by remember { mutableStateOf(false) }
                var dialogMessage by remember { mutableStateOf("") }
                var dialogTitle by remember { mutableStateOf("") }

                // Validar contraseña
                if (!isPasswordValid && newPassword.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "Ver requisitos",
                            style = defaultStyle.copy(color = Color(0xFFB74A5A), fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(onClick = {
                            dialogTitle = "Contraseña"
                            dialogMessage = "Al menos cuatro letras\n" +
                                    "Al menos un número\n" +
                                    "Solo los símbolos: '-', '_', '.'\n" +
                                    "No puede contener espacios"
                            showDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ChecklistRtl,
                                contentDescription = "Información",
                                tint = Color(0xFFB74A5A),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                if (showDialog) {
                    RequirementsDialog(
                        title = dialogTitle,
                        message = dialogMessage,
                        onDismiss = { showDialog = false },
                        fontFamily = defaultFont
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { forgotPasswordViewModel.resetPassword(email, code, newPassword) },
                    enabled = isPasswordValid && buttonsEnabled,
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
                        text = "Confirmar",
                        color = Color.White,
                        style = defaultStyle,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manejo de estados del ViewModel
                when (forgotPasswordState) {
                    is ForgotPasswordState.Loading ->
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF2879E3),
                                strokeWidth = 5.dp
                            )
                        }
                    is ForgotPasswordState.ResetPasswordSuccess -> {
                        LaunchedEffect(forgotPasswordState) {
                            // Snackbar
                            scope.launch {
                                snackbarHostState.showTypedSnackbar(
                                    message = "Contraseña cambiada",
                                    type = SnackbarType.SUCCESS
                                )
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    is ForgotPasswordState.ResetPasswordError -> {
                        LaunchedEffect(forgotPasswordState) {
                            scope.launch {
                                snackbarHostState.showTypedSnackbar(
                                    message = (forgotPasswordState as ForgotPasswordState.ResetPasswordError).message,
                                    type = SnackbarType.ERROR
                                )
                            }
                        }
                    }
                    else -> {}
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "Volver",
                        style = defaultStyle.copy(color = Color(0XFF2879E3),fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            if (buttonsEnabled) {
                                forgotPasswordViewModel.resetForgotPasswordState()
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("forgot_password")
                                }
                            }
                        }
                    )
                }
            }
        }

        // SnackbarHost con CustomSnackbarHost
        CustomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp),
            defaultFont = defaultFont
        )
    }
}