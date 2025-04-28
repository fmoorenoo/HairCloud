package com.haircloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
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

    val token by authViewModel.getTokenFlow().collectAsState(initial = null)
    val userId by authViewModel.getUserIdFlow().collectAsState(initial = null)
    val role by authViewModel.getRoleFlow().collectAsState(initial = null)

    AppNavigation(navController, authViewModel, token, userId, role)
}

