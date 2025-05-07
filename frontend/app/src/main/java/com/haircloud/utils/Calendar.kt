package com.haircloud.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
    onMonthChanged: (YearMonth) -> Unit = {},
    initialMonth: YearMonth? = null,
    diasConCitas: List<LocalDate> = emptyList(),
    allowPreviousMonth: Boolean = false
) {
    val today = remember { LocalDate.now() }
    val currentActualMonth = remember { YearMonth.from(today) }
    val startMonth = initialMonth ?: currentActualMonth

    val maxAllowedMonth = remember { currentActualMonth.plusMonths(4) }
    val minAllowedMonth = if (allowPreviousMonth) currentActualMonth.minusMonths(1) else currentActualMonth

    var displayedYearMonth by remember { mutableStateOf(startMonth) }

    val firstDayOfMonth = displayedYearMonth.atDay(1)
    val daysInMonth = displayedYearMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7

    val canGoToPreviousMonth = displayedYearMonth.isAfter(minAllowedMonth) || displayedYearMonth == minAllowedMonth

    val canGoToNextMonth = displayedYearMonth.isBefore(maxAllowedMonth)

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
                    if (canGoToPreviousMonth) {
                        displayedYearMonth = displayedYearMonth.minusMonths(1)
                        onMonthChanged(displayedYearMonth)
                    }
                },
                enabled = canGoToPreviousMonth
            ) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Mes anterior",
                    tint = if (canGoToPreviousMonth) Color.White else Color.Gray.copy(alpha = 0.5f)
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
            IconButton(
                onClick = {
                    if (canGoToNextMonth) {
                        displayedYearMonth = displayedYearMonth.plusMonths(1)
                        onMonthChanged(displayedYearMonth)
                    }
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

        // Días de la semana
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
                val isEnabled = (allowPreviousMonth || !isPastDate || isToday) && isWorkingDay
                val hasCita = diasConCitas.contains(date)

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                isSelected -> Color(0xFFE1E1E1)
                                hasCita && isPastDate -> Color(0xFF5C6E7D).copy(alpha = 0.25f)
                                hasCita -> Color(0xFF4A89BE).copy(alpha = 0.2f)
                                !isWorkingDay -> Color.Red.copy(alpha = 0.2f)
                                isPastDate -> Color.Gray.copy(alpha = 0.3f)
                                isToday -> Color(0xFF2C93E7).copy(alpha = 0.3f)
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
                        isPastDate -> Color(0xFFFFFFFF).copy(alpha = 0.6f)
                        !isWorkingDay -> Color(0xFFE1E1E1)
                        else -> Color.White
                    }

                    if (isToday) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${day + 1}",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "HOY",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (hasCita) 10.sp else 12.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                if (hasCita) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF30D1FF))
                                    )
                                }
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${day + 1}",
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                            if (hasCita) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF30D1FF))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableSlotsGrid(
    slots: List<AvailableSlot>,
    onSlotSelected: (AvailableSlot) -> Unit,
    selectedSlot: AvailableSlot?,
    modifier: Modifier = Modifier
) {
    val columns = 4
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(max = 300.dp)
    ) {
        items(slots.size) { index ->
            val slot = slots[index]
            val isSelected = slot == selectedSlot

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        when {
                            isSelected -> Color(0xFFDADADA)
                            else -> Color.White
                        }
                    )
                    .border(
                        if (!isSelected) 0.dp else
                            2.dp, color = Color(0xFF5AB641), shape = RoundedCornerShape(5.dp)
                    )
                    .clickable(enabled = true) { onSlotSelected(slot) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = slot.desde,
                        color = if (isSelected) Color.Black else Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}