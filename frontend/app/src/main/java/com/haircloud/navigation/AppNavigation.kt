package com.haircloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haircloud.screens.LoginScreen
import com.haircloud.screens.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController, userRole: String) {
    val startDestination = if (userRole == "cliente") "home_cliente" else "home_peluquero"

    NavHost(navController = navController, startDestination = startDestination) {

        // PANTALLAS DE LOGIN Y REGISTRO
        composable("login") { LoginScreen(navController) { role ->
            navController.navigate(if (role == "cliente") "home_cliente" else "home_peluquero") {
                popUpTo("login") { inclusive = true }
            }
        }}
        composable("register") { RegisterScreen(navController) }

        // PANTALLAS CLIENTES


        // PANTALLAS PELUQUEROS

    }
}


