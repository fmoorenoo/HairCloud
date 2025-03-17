package com.haircloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.haircloud.navigation.AppNavigation
import com.haircloud.screens.LoginScreen

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
    var userRole by rememberSaveable { mutableStateOf<String?>(null) }

    Box {
        if (userRole == null) {
            LoginScreen { role ->
                userRole = role
            }
        } else {
            AppNavigation(userRole!!)
        }
    }
}
