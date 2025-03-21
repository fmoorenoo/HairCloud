package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.utils.CredentialsValidator
import com.haircloud.viewmodel.ForgotPasswordViewModel
import com.haircloud.viewmodel.ForgotPasswordState
import com.haircloud.viewmodel.RegisterState
import com.haircloud.viewmodel.UserViewModel
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar

@Composable
fun RegisterScreen(navController: NavController, forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(), userViewModel: UserViewModel = viewModel()) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    val isPasswordValid = CredentialsValidator.isPasswordValid(password)
    val isUsernameValid = CredentialsValidator.isUsernameValid(username)
    val registerState by userViewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val forgotPasswordState by forgotPasswordViewModel.forgotPasswordState.collectAsState()

    val blueWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF77AEE2), Color(0xFFFFFFFF)))
    val headersFont = FontFamily(Font(R.font.headers_font, FontWeight.Normal))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val defaultStyle = TextStyle(fontFamily = defaultFont, fontSize = 23.sp)

    // Manejar el estado del registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                snackbarHostState.showTypedSnackbar(
                    message = "Cuenta creada con éxito",
                    type = SnackbarType.SUCCESS
                )
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
                userViewModel.resetRegisterState()
            }
            is RegisterState.Error -> {
                snackbarHostState.showTypedSnackbar(
                    message = (registerState as RegisterState.Error).message,
                    type = SnackbarType.ERROR
                )
            }
            else -> {}
        }
    }

    // Manejar los estados de ForgotPassword
    LaunchedEffect(forgotPasswordState) {
        when (forgotPasswordState) {
            is ForgotPasswordState.CodeSentSuccess -> {
                step = 2
                snackbarHostState.showTypedSnackbar(
                    message = "Código enviado con éxito",
                    type = SnackbarType.SUCCESS
                )
                forgotPasswordViewModel.resetForgotPasswordState()
            }
            is ForgotPasswordState.CodeVerifiedSuccess -> {
                step = 3
                verificationCode = ""
                snackbarHostState.showTypedSnackbar(
                    message = "Código verificado correctamente",
                    type = SnackbarType.SUCCESS
                )
                forgotPasswordViewModel.resetForgotPasswordState()
            }
            is ForgotPasswordState.CodeSentError -> {
                snackbarHostState.showTypedSnackbar(
                    message = (forgotPasswordState as ForgotPasswordState.CodeSentError).message,
                    type = SnackbarType.ERROR
                )
                forgotPasswordViewModel.resetForgotPasswordState()
            }
            is ForgotPasswordState.CodeVerifiedError -> {
                snackbarHostState.showTypedSnackbar(
                    message = (forgotPasswordState as ForgotPasswordState.CodeVerifiedError).message,
                    type = SnackbarType.ERROR
                )
                forgotPasswordViewModel.resetForgotPasswordState()
            }
            else -> {}
        }
    }

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
                Text("HairCloud", style = TextStyle(fontFamily = headersFont, fontSize = 55.sp), textAlign = TextAlign.Center)
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(90.dp).wrapContentHeight(),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                when (step) {
                    1 -> "Crear una cuenta"
                    2 -> "Verificar email"
                    else -> "Completar registro"
                },
                color = Color(0XFF132946),
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = headersFont, fontSize = 35.sp),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .height(700.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0x8DFFFFFF))
                    .padding(top = 25.dp, start = 25.dp, end = 25.dp)
            ) {
                when (step) {
                    1 -> {
                        InputField("Nombre completo", name) { name = it }
                        Spacer(modifier = Modifier.height(16.dp))
                        InputField("Email", email, isEmail = true) { email = it }
                    }
                    2 -> {
                        InputField("Código de verificación", verificationCode) { verificationCode = it }
                    }
                    3 -> {
                        InputField("Nombre de usuario", username) { username = it }
                        var showDialog by remember { mutableStateOf(false) }
                        var dialogMessage by remember { mutableStateOf("") }
                        var dialogTitle by remember { mutableStateOf("") }

                        // Validar usuario
                        if (!isUsernameValid && username.isNotEmpty()) {
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
                                    dialogTitle = "Nombre de usuario"
                                    dialogMessage = "Entre 6 y 20 caracteres\n" +
                                            "Solo letras, números, '_' y '.'\n" +
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

                        Spacer(modifier = Modifier.height(10.dp))

                        PasswordField(password) { password = it }

                        // Validar contraseña
                        if (!isPasswordValid && password.isNotEmpty()) {
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
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    Button(
                                        onClick = { showDialog = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF132946),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Entendido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                title = {
                                    Text(
                                        text = dialogTitle,
                                        style = TextStyle(fontFamily = defaultFont, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF132946),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                text = {
                                    Text(
                                        text = dialogMessage,
                                        style = TextStyle(fontFamily = defaultFont, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF333333),
                                    )
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                containerColor = Color(0xFF9FCBE7),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(35.dp))

                val isStep1Filled = name.isNotBlank() && email.isNotBlank()
                val isStep2Filled = verificationCode.isNotBlank()
                val isStep3Filled = isUsernameValid && isPasswordValid

                Button(
                    onClick = {
                        when (step) {
                            1 -> forgotPasswordViewModel.sendVerificationCode(email, "email_verification")
                            2 -> forgotPasswordViewModel.verifyCode(email, verificationCode, "email_verification")
                            3 -> userViewModel.register(name, email, username, password)
                        }
                    },
                    enabled = when (step) {
                        1 -> isStep1Filled
                        2 -> isStep2Filled
                        else -> isStep3Filled
                    },
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
                        text = when (step) {
                            1 -> "Enviar Código"
                            2 -> "Verificar Código"
                            else -> "Registrarme"
                        },
                        color = Color.White,
                        style = defaultStyle,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                when (registerState) {
                    is RegisterState.Loading ->
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
                    else -> {}
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    else -> {}
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (step in 2..3) {
                        Text(
                            text = "Volver",
                            style = defaultStyle.copy(color = Color(0XFF2879E3), fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable {
                                forgotPasswordViewModel.resetForgotPasswordState()
                                step--
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (step == 1) {
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
                        isPassword -> "*****"
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
            placeholder = { Text("*****", fontSize = 23.sp) },
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
