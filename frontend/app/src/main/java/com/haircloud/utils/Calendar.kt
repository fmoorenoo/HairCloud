package com.haircloud.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.haircloud.data.model.AvailableSlot
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarMonth(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    workingDays: List<Int> = listOf(1, 2, 3, 4, 5),
    onMonthChanged: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val currentActualMonth = remember { YearMonth.from(today) }

    var displayedYearMonth by remember { mutableStateOf(currentActualMonth) }

    val firstDayOfMonth = displayedYearMonth.atDay(1)
    val daysInMonth = displayedYearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val canGoToPreviousMonth = displayedYearMonth.isAfter(currentActualMonth) ||
            displayedYearMonth.equals(currentActualMonth)

    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mes anterior
            IconButton(
                onClick = {
                    if (canGoToPreviousMonth && !displayedYearMonth.equals(currentActualMonth)) {
                        displayedYearMonth = displayedYearMonth.minusMonths(1)
                        onMonthChanged()
                    }
                },
                enabled = canGoToPreviousMonth && !displayedYearMonth.equals(currentActualMonth)
            ) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Mes anterior",
                    tint = if (canGoToPreviousMonth && !displayedYearMonth.equals(currentActualMonth))
                        Color.White else Color.Gray.copy(alpha = 0.5f)
                )
            }

            // Nombre del mes y año
            Text(
                text = "${displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    .replaceFirstChar { it.uppercase() }} ${displayedYearMonth.year}",
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = defaultFont,
                    fontWeight = FontWeight.Bold
                ),
                fontSize = 30.sp
            )

            // Mes siguiente
            IconButton(onClick = {
                displayedYearMonth = displayedYearMonth.plusMonths(1)
                onMonthChanged()
            }) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Mes siguiente",
                    modifier = Modifier.rotate(180f),
                    tint = Color.White
                )
            }
        }

        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = listOf("D", "L", "M", "X", "J", "V", "S")
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Días del mes
            items(daysInMonth) { day ->
                val date = firstDayOfMonth.plusDays(day.toLong())
                val dayOfWeek = date.dayOfWeek.value % 7
                val isWorkingDay = workingDays.contains(dayOfWeek)
                val isSelected = selectedDate == date
                val isPastDate = date.isBefore(today)
                val isToday = date == today
                val isEnabled = (!isPastDate || isToday) && isWorkingDay

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isSelected -> Color.White
                                isPastDate -> Color.Gray.copy(alpha = 0.1f)
                                !isWorkingDay -> Color.Red.copy(alpha = 0.2f)
                                isToday -> Color(0xFF30D1FF).copy(alpha = 0.3f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable(enabled = isEnabled) {
                            if (isEnabled) onDateSelected(date)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val textColor = when {
                        isSelected -> Color.Black
                        isPastDate -> Color.Gray.copy(alpha = 0.5f)
                        !isWorkingDay -> Color(0xFFE1E1E1)
                        else -> Color.White
                    }

                    if (isToday) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${day + 1}",
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "HOY",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "${day + 1}",
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableSlotsList(
    slots: List<AvailableSlot>,
    onSlotSelected: (AvailableSlot) -> Unit,
    selectedSlot: AvailableSlot?
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(slots) { slot ->
            val isSelected = slot == selectedSlot
            val isOccupied = false

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isSelected -> Color.White
                            isOccupied -> Color.Red.copy(alpha = 0.5f)
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable { onSlotSelected(slot) }
            ) {
                Text(
                    text = slot.desde,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}