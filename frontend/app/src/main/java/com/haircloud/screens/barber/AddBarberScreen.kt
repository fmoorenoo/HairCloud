package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.CreateBarberRequest
import com.haircloud.data.model.WorkDaySchedule
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarberViewModel
import com.haircloud.viewmodel.CreateBarberState
import com.haircloud.viewmodel.InactiveBarbersState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BarberToActivate(val usuarioId: Int, val nombre: String, val especialidad: String?)

data class WorkSchedule(
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)
data class DaySchedule(
    val day: String,
    var isActive: Boolean = false,
    var startTime: String = "",
    var endTime: String = ""
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddBarberScreen(navController: NavController, localId: Int, userId: Int?) {
    val barberViewModel: BarberViewModel = viewModel()
    val inactiveBarbersState by barberViewModel.inactiveBarbersState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF1E1E1E)

    var isNavigating by remember { mutableStateOf(false) }

    var showActivationDialog by remember { mutableStateOf<BarberToActivate?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    var selectedTab by remember { mutableStateOf(AddBarberTabs.NEW_BARBER) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(Unit) {
        barberViewModel.getInactiveBarbers()
    }

    Scaffold(
        modifier = Modifier
            .background(brush = blackWhiteGradient)
            .fillMaxSize(),
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 30.dp),
                defaultFont = defaultFont
            )
        },
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkSurface.copy(alpha = 0.8f))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Añadir peluquero",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkSurface.copy(alpha = 0.7f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AddBarberTabs.entries.forEach { tab ->
                        TabItem(
                            text = tab.title,
                            isSelected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            defaultFont = defaultFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    AddBarberTabs.NEW_BARBER -> {
                        NewBarberSection(
                            localId = localId,
                            barberViewModel = barberViewModel,
                            onResult = { message, isSuccess ->
                                snackbarType = if (isSuccess) SnackbarType.SUCCESS else SnackbarType.ERROR
                                snackbarMessage = message
                            }
                        )
                    }
                    AddBarberTabs.PREVIOUS_BARBERS -> {
                        PreviousBarbersSection(
                            inactiveBarbersState = inactiveBarbersState,
                            defaultFont = defaultFont,
                            onBarberActivate = { barberToActivate ->
                                showActivationDialog = barberToActivate
                            }
                        )
                    }
                }
            }
        }

        showActivationDialog?.let { barberToActivate ->
            Dialog(onDismissRequest = { showActivationDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Activar Peluquero",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = defaultFont,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "¿Estás seguro de que quieres activar a ${barberToActivate.nombre}?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = defaultFont,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { showActivationDialog = null },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        barberViewModel.activateBarber(barberToActivate.usuarioId)
                                        delay(500)
                                        barberViewModel.getInactiveBarbers()
                                        snackbarType = SnackbarType.SUCCESS
                                        snackbarMessage = "${barberToActivate.nombre} añadido al personal"
                                    }

                                    showActivationDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Confirmar")
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AddBarberTabs(val title: String) {
    NEW_BARBER("Nuevo peluquero"),
    PREVIOUS_BARBERS("Peluqueros anteriores")
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    defaultFont: FontFamily
) {
    val backgroundColor by animateFloatAsState(targetValue = if (isSelected) 1f else 0f, label = "tab_background")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = Color.White.copy(
                    alpha = backgroundColor * 0.2f
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = defaultFont,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun NewBarberSection(
    localId: Int,
    barberViewModel: BarberViewModel,
    onResult: (String, Boolean) -> Unit
) {
    var nombreUsuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var especialidad by remember { mutableStateOf("") }
    var workSchedules by remember { mutableStateOf(listOf<WorkSchedule>()) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val camposValidos = nombreUsuario.isNotBlank() && contrasena.isNotBlank()
            && email.isNotBlank() && nombre.isNotBlank() && workSchedules.isNotEmpty()

    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF2C2C2C)

    val createState by barberViewModel.createBarberState.collectAsState()

    LaunchedEffect(createState) {
        when (createState) {
            is CreateBarberState.Success -> {
                onResult((createState as CreateBarberState.Success).message, true)
                nombreUsuario = ""
                contrasena = ""
                email = ""
                nombre = ""
                especialidad = ""
                workSchedules = emptyList()
                barberViewModel.resetCreateBarberState()
            }
            is CreateBarberState.Error -> {
                onResult((createState as CreateBarberState.Error).message, false)
                barberViewModel.resetCreateBarberState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(darkSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear nuevo peluquero",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = defaultFont,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = { nombreUsuario = it },
            label = { Text("Nombre de usuario", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF444444),
                unfocusedBorderColor = Color(0xFF666666),
                focusedLabelColor = Color(0xFFAAAAAA),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF444444),
                unfocusedBorderColor = Color(0xFF666666),
                focusedLabelColor = Color(0xFFAAAAAA),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF444444),
                unfocusedBorderColor = Color(0xFF666666),
                focusedLabelColor = Color(0xFFAAAAAA),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF444444),
                unfocusedBorderColor = Color(0xFF666666),
                focusedLabelColor = Color(0xFFAAAAAA),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = especialidad,
            onValueChange = { especialidad = it },
            label = { Text("Especialidad (opcional)", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF444444),
                unfocusedBorderColor = Color(0xFF666666),
                focusedLabelColor = Color(0xFFAAAAAA),
                unfocusedLabelColor = Color(0xFF888888),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showScheduleDialog = true }
                .background(Color(0xFF444444))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Seleccionar horario",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (workSchedules.isEmpty()) "Añadir horario de trabajo" else "Horario de trabajo (${workSchedules.size} días)",
                color = Color.White,
                fontFamily = defaultFont,
                fontSize = 17.sp
            )
        }

        Button(
            onClick = {
                val request = CreateBarberRequest(
                    nombreusuario = nombreUsuario,
                    contrasena = contrasena,
                    email = email,
                    nombre = nombre,
                    especialidad = if (especialidad.isBlank()) null else especialidad,
                    localid = localId,
                    horario = workSchedules.map {
                        WorkDaySchedule(it.diaSemana, it.horaInicio, it.horaFin)
                    }
                )
                coroutineScope.launch {
                    barberViewModel.createBarber(request)
                }
            },
            enabled = camposValidos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (camposValidos) Color(0xFF284E6E) else Color.DarkGray
            )
        ) {
            Text(
                "Añadir al personal",
                color = Color.White,
                fontFamily = defaultFont,
                fontSize = 20.sp
            )
        }

        if (showScheduleDialog) {
            BarberScheduleDialog(
                onDismiss = { showScheduleDialog = false },
                onSave = { selectedSchedules ->
                    workSchedules = selectedSchedules
                },
                initialSchedules = workSchedules
            )
        }
    }
}

@Composable
fun BarberScheduleDialog(
    onDismiss: () -> Unit,
    onSave: (List<WorkSchedule>) -> Unit,
    initialSchedules: List<WorkSchedule>
) {
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF2C2C2C)

    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var schedules by remember {
        mutableStateOf(
            daysOfWeek.map { day ->
                val existing = initialSchedules.find { it.diaSemana == day }
                if (existing != null)
                    DaySchedule(day, true, existing.horaInicio, existing.horaFin)
                else
                    DaySchedule(day)
            }
        )
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
                                onSave(selected)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Guardar", fontFamily = defaultFont, color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun PreviousBarbersSection(
    inactiveBarbersState: InactiveBarbersState,
    defaultFont: FontFamily,
    onBarberActivate: (BarberToActivate) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    when (val state = inactiveBarbersState) {
        is InactiveBarbersState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is InactiveBarbersState.Error -> {
            val message = state.message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    color = Color.Red,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 16.sp
                )
            }
        }
        is InactiveBarbersState.Success -> {
            val barbers = state.barbers

            val filteredBarbers = barbers.filter {
                searchQuery.isEmpty() ||
                        it.nombre.lowercase().contains(searchQuery.lowercase())
            }

            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar peluquero") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF444444),
                        unfocusedBorderColor = Color(0xFF666666),
                        focusedLabelColor = Color(0xFFAAAAAA),
                        unfocusedLabelColor = Color(0xFF888888),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                if (filteredBarbers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "Sin resultados",
                                tint = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron peluqueros",
                                color = Color.Gray.copy(alpha = 0.8f),
                                fontSize = 18.sp,
                                fontFamily = defaultFont
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBarbers, key = { it.usuarioid }) { barber ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onBarberActivate( BarberToActivate(
                                                barber.usuarioid,
                                                barber.nombre,
                                                barber.especialidad)
                                        )
                                    }
                                    .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = barber.nombre,
                                            color = Color.White,
                                            style = TextStyle(fontFamily = defaultFont),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = barber.especialidad ?: "Sin especialidad",
                                            color = Color.White.copy(alpha = 0.8f),
                                            style = TextStyle(fontFamily = defaultFont),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
        else -> {}
    }
}