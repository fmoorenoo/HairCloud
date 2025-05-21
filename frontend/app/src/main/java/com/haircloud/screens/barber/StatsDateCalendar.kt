package com.haircloud.screens.barber

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haircloud.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun StatsDateCalendar(
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val currentActualMonth = remember { YearMonth.from(today) }
    val maxAllowedMonth = currentActualMonth
    val minAllowedMonth = remember { currentActualMonth.minusYears(1) }

    var displayedYearMonth by remember { mutableStateOf(currentActualMonth) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    val firstDayOfMonth = displayedYearMonth.atDay(1)
    val daysInMonth = displayedYearMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7

    val canGoToPreviousMonth = displayedYearMonth.isAfter(minAllowedMonth)
    val canGoToNextMonth = displayedYearMonth.isBefore(maxAllowedMonth)

    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    AnimatedVisibility(
        visible = show,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (canGoToPreviousMonth) displayedYearMonth = displayedYearMonth.minusMonths(1)
                    },
                    enabled = canGoToPreviousMonth
                ) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Mes anterior",
                        tint = if (canGoToPreviousMonth) Color.White else Color.Gray.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = "${displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercase() }} ${displayedYearMonth.year}",
                    color = Color.White,
                    fontFamily = defaultFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )

                IconButton(
                    onClick = {
                        if (canGoToNextMonth) displayedYearMonth = displayedYearMonth.plusMonths(1)
                    },
                    enabled = canGoToNextMonth
                ) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Mes siguiente",
                        modifier = Modifier.rotate(180f),
                        tint = if (canGoToNextMonth) Color.White else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                daysOfWeek.forEach { day ->
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = Color(0xFF9D9D9D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(40.dp))
                }

                items(daysInMonth) { index ->
                    val date = firstDayOfMonth.plusDays(index.toLong())
                    val isSelected = date == startDate || date == endDate
                    val isInRange = startDate != null && endDate != null && date > startDate && date < endDate

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isSelected -> Color(0xFF9DD0FF).copy(alpha = 0.4f)
                                    isInRange -> Color(0xFF66B4FF).copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable {
                                when {
                                    startDate == null || (startDate != null && endDate != null) -> {
                                        startDate = date
                                        endDate = null
                                    }
                                    date.isBefore(startDate) -> {
                                        endDate = startDate
                                        startDate = date
                                    }
                                    else -> endDate = date
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${date.dayOfMonth}",
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Cancelar",
                    color = Color.Gray,
                    modifier = Modifier.clickable { onCancel() }
                )
                Text(
                    text = "Confirmar",
                    color = if (startDate != null && endDate != null) Color(0xFF5FB1FF) else Color.Gray,
                    modifier = Modifier.clickable(
                        enabled = startDate != null && endDate != null
                    ) {
                        onConfirm(startDate!!, endDate!!)
                    }
                )
            }
        }
    }
}