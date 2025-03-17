package com.haircloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(userRole: String) {
    val navController = rememberNavController()

    val startDestination = if (userRole == "cliente") "home_cliente" else "home_peluquero"

    NavHost(navController = navController, startDestination = startDestination) {

        if (userRole == "cliente") {
            print("Pantallas de cliente")
        }

        if (userRole == "peluquero") {
            print("Pantallas de peluquero")
        }
    }
}
