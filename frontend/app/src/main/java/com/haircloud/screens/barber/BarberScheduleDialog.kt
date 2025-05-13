package com.haircloud.screens.barber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haircloud.R
import com.haircloud.viewmodel.BarberViewModel

@Composable
fun BarberScheduleDialog(
    onDismiss: () -> Unit,
    onSave: (List<WorkSchedule>) -> Unit,
    initialSchedules: List<WorkSchedule>,
    editable: Boolean = false,
    peluqueroId: Int = -1
) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF2C2C2C)

    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var schedules by remember { mutableStateOf(emptyList<DaySchedule>()) }

    val barberViewModel: BarberViewModel = viewModel()

    LaunchedEffect(initialSchedules) {
        schedules = daysOfWeek.map { day ->
            val existing = initialSchedules.find { it.diaSemana == day }
            if (existing != null)
                DaySchedule(day, true, existing.horaInicio, existing.horaFin)
            else
                DaySchedule(day)
        }
    }
    val originalMap = remember(initialSchedules) {
        initialSchedules.associateBy { it.diaSemana }
    }

    val hasChanges = schedules.any { day ->
        if (!day.isActive) return@any originalMap.containsKey(day.day)
        val original = originalMap[day.day]
        original?.horaInicio != day.startTime || original.horaFin != day.endTime
    }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = darkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Horario laboral",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = defaultFont
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    itemsIndexed(schedules) { index, schedule ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = schedule.isActive,
                                    onCheckedChange = { checked ->
                                        schedules = schedules.toMutableList().also {
                                            it[index] = it[index].copy(isActive = checked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color.White,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Text(
                                    schedule.day,
                                    color = Color.White,
                                    fontFamily = defaultFont,
                                    fontSize = 16.sp
                                )
                            }

                            if (schedule.isActive) {
                                val startTimeParts = schedule.startTime.split(":").let {
                                    if (it.size == 2) Pair(it[0], it[1]) else Pair("", "")
                                }
                                val endTimeParts = schedule.endTime.split(":").let {
                                    if (it.size == 2) Pair(it[0], it[1]) else Pair("", "")
                                }

                                var startHour by remember(schedule.startTime) { mutableStateOf(startTimeParts.first) }
                                var startMinute by remember(schedule.startTime) { mutableStateOf(startTimeParts.second) }
                                var endHour by remember(schedule.endTime) { mutableStateOf(endTimeParts.first) }
                                var endMinute by remember(schedule.endTime) { mutableStateOf(endTimeParts.second) }

                                Column(
                                    modifier = Modifier.padding(start = 36.dp, top = 8.dp)
                                ) {
                                    Text(
                                        "Inicio:",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontFamily = defaultFont,
                                        fontSize = 14.sp
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = startHour,
                                            onValueChange = { value ->
                                                if (value.isEmpty() || (value.toIntOrNull() in 0..23 && value.length <= 2)) {
                                                    startHour = value
                                                    if (value.isNotEmpty() && startMinute.isNotEmpty()) {
                                                        schedules = schedules.toMutableList().also {
                                                            it[index] = it[index].copy(startTime = "$value:$startMinute")
                                                        }
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            modifier = Modifier.width(60.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF444444),
                                                unfocusedBorderColor = Color(0xFF666666),
                                                cursorColor = Color.White,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                            ),
                                            placeholder = { Text("00", color = Color.Gray) },
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Text(
                                            ":",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        OutlinedTextField(
                                            value = startMinute,
                                            onValueChange = { value ->
                                                if (value.isEmpty() || (value.toIntOrNull() in 0..59 && value.length <= 2)) {
                                                    startMinute = value
                                                    if (startHour.isNotEmpty() && value.isNotEmpty()) {
                                                        schedules = schedules.toMutableList().also {
                                                            it[index] = it[index].copy(startTime = "${startHour}:$value")
                                                        }
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            modifier = Modifier.width(60.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF444444),
                                                unfocusedBorderColor = Color(0xFF666666),
                                                cursorColor = Color.White,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                            ),
                                            placeholder = { Text("00", color = Color.Gray) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Fin:",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontFamily = defaultFont,
                                        fontSize = 14.sp
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = endHour,
                                            onValueChange = { value ->
                                                if (value.isEmpty() || (value.toIntOrNull() in 0..23 && value.length <= 2)) {
                                                    endHour = value
                                                    if (value.isNotEmpty() && endMinute.isNotEmpty()) {
                                                        schedules = schedules.toMutableList().also {
                                                            it[index] = it[index].copy(endTime = "$value:$endMinute")
                                                        }
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            modifier = Modifier.width(60.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF444444),
                                                unfocusedBorderColor = Color(0xFF666666),
                                                cursorColor = Color.White,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                            ),
                                            placeholder = { Text("00", color = Color.Gray) },
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Text(
                                            ":",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        OutlinedTextField(
                                            value = endMinute,
                                            onValueChange = { value ->
                                                if (value.isEmpty() || (value.toIntOrNull() in 0..59 && value.length <= 2)) {
                                                    endMinute = value
                                                    if (endHour.isNotEmpty() && value.isNotEmpty()) {
                                                        schedules = schedules.toMutableList().also {
                                                            it[index] = it[index].copy(endTime = "${endHour}:$value")
                                                        }
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            modifier = Modifier.width(60.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF444444),
                                                unfocusedBorderColor = Color(0xFF666666),
                                                cursorColor = Color.White,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                            ),
                                            placeholder = { Text("00", color = Color.Gray) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1E1E)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFFFFFFFF),
                            fontSize = 17.sp,
                            fontFamily = defaultFont,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancelar", fontFamily = defaultFont)
                    }

                    Button(
                        onClick = {
                            val invalidDays = schedules.filter {
                                it.isActive && it.startTime.isNotBlank() && it.endTime.isNotBlank()
                            }.filter {
                                val start = it.startTime.split(":")
                                val end = it.endTime.split(":")
                                if (start.size != 2 || end.size != 2) {
                                    true
                                } else {
                                    val startHour = start[0].toIntOrNull() ?: 0
                                    val startMinute = start[1].toIntOrNull() ?: 0
                                    val endHour = end[0].toIntOrNull() ?: 0
                                    val endMinute = end[1].toIntOrNull() ?: 0

                                    (startHour * 60 + startMinute) >= (endHour * 60 + endMinute)
                                }
                            }

                            if (invalidDays.isNotEmpty()) {
                                val dayNames = invalidDays.joinToString(", ") { it.day }
                                errorMessage = "Error ($dayNames)\n La hora de inicio debe ser anterior a la hora de fin."
                            } else {
                                val selected = schedules.filter {
                                    it.isActive && it.startTime.isNotBlank() && it.endTime.isNotBlank()
                                }.map {
                                    WorkSchedule(it.day, it.startTime, it.endTime)
                                }

                                if (editable) {
                                    barberViewModel.updateBarberSchedule(peluqueroId, selected)
                                }

                                onSave(selected)
                                onDismiss()
                            }
                        },
                        enabled = !editable || hasChanges,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Guardar", fontFamily = defaultFont, color = Color.White)
                    }

                }
            }
        }
    }
}