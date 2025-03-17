package com.haircloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haircloud.navigation.AppNavigation
import com.haircloud.screens.LoginScreen
import com.haircloud.screens.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HairCloudApp()
        }
    }
}

@Composable
fun HairCloudApp() {
    val navController = rememberNavController()
    var userRole by rememberSaveable { mutableStateOf<String?>(null) }

    Box {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(navController) { role ->
                    userRole = role
                    val destination = if (role == "cliente") "home_cliente" else "home_peluquero"
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            composable("register") { RegisterScreen(navController) }
        }

        userRole?.let {
            AppNavigation(navController, it)
        }
    }
}
