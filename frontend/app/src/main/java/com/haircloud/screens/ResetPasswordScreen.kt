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
fun ResetPasswordScreen(navController: NavController, email: String, code: String) {
    var newPassword by remember { mutableStateOf("") }
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
    val forgotPasswordState by forgotPasswordViewModel.forgotPasswordState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Restablecer Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo para ingresar la nueva contraseña
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva Contraseña") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para confirmar la nueva contraseña
        Button(
            onClick = { forgotPasswordViewModel.resetPassword(email, code, newPassword) },
            enabled = newPassword.isNotEmpty()
        ) {
            Text("Confirmar Nueva Contraseña")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manejo de estados del ViewModel
        when (forgotPasswordState) {
            is ForgotPasswordState.Loading -> CircularProgressIndicator()
            is ForgotPasswordState.PasswordResetSuccess -> {
                Text("Contraseña restablecida correctamente", color = Color.Green)
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
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
