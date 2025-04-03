package com.haircloud.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haircloud.R

// Tipos de snackbar
enum class SnackbarType {
    ERROR,
    SUCCESS,
    WARNING,
    INFO
}

// Datos del mensaje
data class SnackbarMessage(
    val message: String,
    val type: SnackbarType
)

@Composable
fun CustomSnackbar(
    message: String,
    type: SnackbarType,
    defaultFont: FontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))
) {
    // Color e icono según el tipo de mensaje
    val (backgroundColor, icon) = when (type) {
        SnackbarType.ERROR -> Pair(Color(0xFFB74A5A), Icons.Default.Cancel)
        SnackbarType.SUCCESS -> Pair(Color(0xFF439B3E), Icons.Default.CheckCircle)
        SnackbarType.WARNING -> Pair(Color(0xFFE6B83D), Icons.Default.Warning)
        SnackbarType.INFO -> Pair(Color(0xFF3D8EE6), Icons.Default.Info)
    }
    Snackbar(
        containerColor = backgroundColor,
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = defaultFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = icon,
                contentDescription = "Estado",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

suspend fun SnackbarHostState.showTypedSnackbar(
    message: String,
    type: SnackbarType,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    val snackbarMessage = SnackbarMessage(message, type)

    return this.showSnackbar(
        message = "$type::$message",
        duration = duration
    )
}

@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    defaultFont: FontFamily = FontFamily(Font(R.font.default_font, FontWeight.Normal))
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            val messageData = parseSnackbarMessage(data.visuals.message)
            CustomSnackbar(
                message = messageData.message,
                type = messageData.type,
                defaultFont = defaultFont
            )
        }
    )
}

private fun parseSnackbarMessage(encodedMessage: String): SnackbarMessage {
    return try {
        val parts = encodedMessage.split("::", limit = 2)
        val type = SnackbarType.valueOf(parts[0])
        val message = parts[1]
        SnackbarMessage(message, type)
    } catch (_: Exception) {
        SnackbarMessage(encodedMessage, SnackbarType.ERROR)
    }
}