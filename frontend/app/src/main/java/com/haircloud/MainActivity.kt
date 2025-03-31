package com.haircloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
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
    var userRole by rememberSaveable { mutableStateOf<String?>(null) }

    AppNavigation(navController, authViewModel)

    LaunchedEffect(userRole) {
        userRole?.let {
            val destination = if (it == "cliente") "home_cliente" else "home_peluquero"
            navController.navigate(destination) {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}

