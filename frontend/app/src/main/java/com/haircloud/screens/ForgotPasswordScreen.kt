package com.haircloud.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo para ingresar el email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para enviar código de verificación
        Button(
            onClick = {
                forgotPasswordViewModel.sendVerificationCode(email)
                isCodeSent = true
            },
            enabled = email.isNotEmpty()
        ) {
            Text("Enviar Código")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manejo del estado del ViewModel
        when (forgotPasswordState) {
            is ForgotPasswordState.Loading -> CircularProgressIndicator()
            is ForgotPasswordState.CodeSentSuccess -> {
                Log.d("ForgotPasswordScreen", "Código enviado con éxito")
                Text("Código enviado con éxito", color = Color.Green)
            }
            is ForgotPasswordState.Error -> {
                Text(
                    text = (forgotPasswordState as ForgotPasswordState.Error).message,
                    color = Color.Red
                )
            }
            else -> {}
        }

        // Sección para ingresar el código de verificación (solo si se ha enviado)
        if (isCodeSent) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Introduce el código recibido", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código de verificación") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para verificar el código
            Button(
                onClick = {
                    forgotPasswordViewModel.verifyCode(email, code)
                },
                enabled = code.isNotEmpty()
            ) {
                Text("Confirmar")
            }

            // Manejo del estado después de verificar el código
            when (forgotPasswordState) {
                is ForgotPasswordState.CodeVerifiedSuccess -> {
                    Log.d("ForgotPasswordScreen", "Código verificado con éxito")
                    Text("Código verificado correctamente", color = Color.Green)

                    // Navegar a ResetPasswordScreen cuando el código sea verificado
                    LaunchedEffect(forgotPasswordState) {
                        if (forgotPasswordState is ForgotPasswordState.CodeVerifiedSuccess) {
                            val verifiedCode = code // Captura el código ingresado
                            navController.navigate("reset_password/$email/$verifiedCode")
                        }
                    }


                }

                is ForgotPasswordState.Error -> {
                    Text(
                        text = (forgotPasswordState as ForgotPasswordState.Error).message,
                        color = Color.Red
                    )
                }
                else -> {}
            }
        }
    }
}
