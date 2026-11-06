package com.haircloud

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.haircloud.data.SessionManager
import com.haircloud.navigation.AppNavigation
import com.haircloud.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            HairCloudApp(authViewModel)
        }
    }
}

@Composable
fun HairCloudApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val token by authViewModel.getTokenFlow().collectAsState(initial = null)
    val userId by authViewModel.getUserIdFlow().collectAsState(initial = null)
    val role by authViewModel.getRoleFlow().collectAsState(initial = null)

    // Si el servidor revoca la sesión (contraseña cambiada, baja de personal,
    // refresh token caducado...) se vuelve al login desde cualquier pantalla.
    LaunchedEffect(Unit) {
        SessionManager.expired.collect {
            Toast.makeText(context, "Tu sesión ha caducado. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    AppNavigation(navController, authViewModel, token, userId, role)
}
