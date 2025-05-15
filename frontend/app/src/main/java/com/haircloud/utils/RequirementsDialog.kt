package com.haircloud.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChecklistRtl
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun RequirementsDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    fontFamily: FontFamily,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    shadowElevation = 8f
                    shape = RoundedCornerShape(16.dp)
                }
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF212121), Color(0xFF131313))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChecklistRtl,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(35.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(vertical = 8.dp),
                        thickness = 2.dp,
                        color = Color(0xFF60A5FA).copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val requirements = message.split("\n").filter { it.isNotBlank() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        requirements.forEach { requirement ->
                            RequirementItem(
                                text = requirement,
                                fontFamily = fontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                    ) {
                        Text(
                            "Entendido",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    fontFamily: FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = Color(0xFF60A5FA), RoundedCornerShape(50))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp
            ),
            color = Color(0xFFE2E8F0),
            modifier = Modifier.fillMaxWidth()
        )
    }
}