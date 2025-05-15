package com.haircloud.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RequirementsDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    fontFamily: FontFamily
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF132946),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Entendido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = title,
                style = TextStyle(fontFamily = fontFamily, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF132946),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = TextStyle(fontFamily = fontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF333333),
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        containerColor = Color(0xFF9FCBE7),
        shape = RoundedCornerShape(12.dp)
    )
}