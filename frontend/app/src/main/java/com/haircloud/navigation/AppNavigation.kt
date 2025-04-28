package com.haircloud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haircloud.screens.*
import com.haircloud.screens.barber.*
import com.haircloud.screens.client.*
import com.haircloud.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    token: String?,
    userId: Int?,
    role: String?
) {
    val startDestination = when {
        token.isNullOrEmpty() || userId == null -> "login"
        role == "cliente" -> "client_home/$userId"
        else -> "barber_home/$userId"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Login
        composable("login") {
            LoginScreen(navController, authViewModel)
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

        // Perfil cliente
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            ProfileScreen(navController, userId)
        }

        // Manual de usuario
        composable("user_manual") {
            UserManualScreen(navController)
        }

        // Clientes Home
        composable("client_home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            ClientHomeScreen(navController, userId)
        }

        // Favs cliente
        composable("client_favs/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            ClientFavsScreen(navController, userId)
        }

        // Citas cliente
        composable("client_dates/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            ClientDatesScreen(navController, userId)
        }

        // Barbería cliente
        composable("client_barber_info/{userId}/{localId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            BarberInfoScreen(navController, userId, localId)
        }

        // Booking cliente
        composable("client_booking/{userId}/{localId}/{serviceId}/{clientId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            val serviceId = backStackEntry.arguments?.getString("serviceId")?.toIntOrNull()
            val clientId = backStackEntry.arguments?.getString("clientId")?.toIntOrNull()
            BookingScreen(navController, userId, localId, serviceId, clientId)
        }

        // Peluqueros Home
        composable("barber_home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            BarberHomeScreen(navController, userId)
        }
    }
}
