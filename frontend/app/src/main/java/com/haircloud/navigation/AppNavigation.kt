package com.haircloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haircloud.screens.ForgotPasswordScreen
import com.haircloud.screens.LoginScreen
import com.haircloud.screens.RegisterScreen
import com.haircloud.screens.ResetPasswordScreen
import com.haircloud.screens.barber.BarberHomeScreen
import com.haircloud.screens.client.ClientHomeScreen
import com.haircloud.viewmodel.UserViewModel

@Composable
fun AppNavigation(navController: NavHostController, userViewModel: UserViewModel) {
    NavHost(navController = navController, startDestination = "login") {

        // Login
        composable("login") {
            LoginScreen(navController, userViewModel)
        }

        // Registro
        composable("register") { RegisterScreen(navController) }

        // Recuperar contraseña
        composable("forgot_password") { ForgotPasswordScreen(navController) }

        // Cambiar contraseña
        composable("reset_password/{email}/{code}/{username}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            ResetPasswordScreen(navController, email, code, username)
        }


        // Clientes Home
        composable("home_cliente") { ClientHomeScreen(navController) }

        // Peluqueros Home
        composable("home_peluquero") { BarberHomeScreen(navController) }
    }
}

