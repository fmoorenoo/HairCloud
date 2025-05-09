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
    userID: Int?,
    role: String?
) {
    val startDestination = when {
        token.isNullOrEmpty() || userID == null -> "login"
        role == "cliente" -> "client_home/$userID"
        else -> "barber_home/$userID"
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
            ClientProfileScreen(navController, userId)
        }

        // Perfil barbería
        composable("barber_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            BarberProfileScreen(navController, userId)
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

        // Peluqueros Settings
        composable("barber_settings/{userId}/{isAdmin}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
            BarberSettingsScreen(navController, userId, isAdmin)
        }

        // Peluqueros Reportes
//        composable("barber_reports/{userId}") { backStackEntry ->
//            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
//            BarberReportsScreen(navController, userId)
//        }

        // Peluqueros Info Barbería
        composable("barbershop_info/{userId}/{localId}/{isAdmin}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
            BarbershopInfoScreen(navController, userId?.toInt() ?: -1, localId?.toInt() ?: -1, isAdmin)
        }

        // Peluqueros Reviews Barbería
        composable("barbershop_reviews/{localId}/{isAdmin}") { backStackEntry ->
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
            BarbershopReviewsScreen(navController, localId ?: -1, isAdmin)
        }

        // Peluqueros Servicios
        composable("barbershop_services/{localId}/{isAdmin}") { backStackEntry ->
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
            BarbershopServicesScreen(navController, localId?.toInt() ?: -1, isAdmin)
        }

        // Peluqueros Personal
        composable("barbershop_barbers/{localId}/{userId}/{isAdmin}") { backStackEntry ->
            val localId = backStackEntry.arguments?.getString("localId")?.toIntOrNull()
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
            BarbershopBarbersScreen(navController, localId?.toInt() ?: -1, userId?.toInt() ?: -1, isAdmin)
        }
    }
}
