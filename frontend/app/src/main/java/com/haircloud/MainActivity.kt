package com.haircloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haircloud.navigation.AppNavigation
import com.haircloud.screens.LoginScreen
import com.haircloud.screens.RegisterScreen
import com.haircloud.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userViewModel: UserViewModel = viewModel()
            HairCloudApp(userViewModel)
        }
    }
}

@Composable
fun HairCloudApp(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    var userRole by rememberSaveable { mutableStateOf<String?>(null) }

    AppNavigation(navController, userRole, userViewModel)

    LaunchedEffect(userRole) {
        userRole?.let {
            val destination = if (it == "cliente") "home_cliente" else "home_peluquero"
            navController.navigate(destination) {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}

