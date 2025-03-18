package com.haircloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haircloud.screens.LoginScreen
import com.haircloud.screens.RegisterScreen
import com.haircloud.screens.barber.BarberHomeScreen
import com.haircloud.screens.client.ClientHomeScreen
import com.haircloud.viewmodel.UserViewModel

@Composable
fun AppNavigation(navController: NavHostController, userRole: String?, userViewModel: UserViewModel) {
    NavHost(navController = navController, startDestination = "login") {

        // Login
        composable("login") {
            LoginScreen(navController, userViewModel) { role ->
                navController.navigate(if (role == "cliente") "home_cliente" else "home_peluquero") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        // Registro
        composable("register") { RegisterScreen(navController) }

        // Clientes Home
        composable("home_cliente") { ClientHomeScreen(navController) }

        // Peluqueros Home
        composable("home_peluquero") { BarberHomeScreen(navController) }
    }
}

