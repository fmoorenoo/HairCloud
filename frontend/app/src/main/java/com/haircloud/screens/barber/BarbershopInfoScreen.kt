package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.BarbershopResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.viewmodel.BarbershopViewModel
import com.haircloud.viewmodel.SingleBarbershopState
import com.haircloud.viewmodel.BarbershopUpdateState
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haircloud.utils.CredentialsValidator
import kotlinx.coroutines.launch

private val DarkSurface = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00B0FF)
private val GoldColor = Color(0xFFFFC107)
private val blackWhiteGradient =
    Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarbershopInfoScreen(navController: NavController, localId: Int, userId: Int, isAdmin: Boolean = false) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isNavigating by remember { mutableStateOf(false) }
    val barbershopViewModel: BarbershopViewModel = viewModel()
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()
    val updateState by barbershopViewModel.barbershopUpdateState.collectAsState()
    val scrollState = rememberScrollState()

    var isEditMode by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var horarioApertura by remember { mutableStateOf("") }
    var horarioCierre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val isPhoneValid = telefono.isEmpty() || CredentialsValidator.isPhoneValid(telefono)
    val isHorarioAperturaValido = CredentialsValidator.isHourValid(horarioApertura)
    val isHorarioCierreValido = CredentialsValidator.isHourValid(horarioCierre)
    val isHorarioValido = isHorarioAperturaValido && isHorarioCierreValido && horarioApertura < horarioCierre
    val requiredFieldsFilled = nombre.isNotBlank() && direccion.isNotBlank() && telefono.isNotBlank() && horarioApertura.isNotBlank() && horarioCierre.isNotBlank()
    val barbershop = (singleBarbershopState as? SingleBarbershopState.Success)?.barbershop



    val hasChanges = barbershop != null && (
            nombre != barbershop.nombre ||
                    direccion != barbershop.direccion ||
                    telefono != barbershop.telefono ||
                    horarioApertura != barbershop.horarioapertura ||
                    horarioCierre != barbershop.horariocierre ||
                    descripcion != (barbershop.descripcion ?: "")
    )
    val isUpdating = updateState is BarbershopUpdateState.Updating
    val canSave = hasChanges && !isUpdating && isPhoneValid && isHorarioValido && requiredFieldsFilled

    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(localId, userId) {
        barbershopViewModel.getBarbershopById(userId, localId)
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is BarbershopUpdateState.UpdateSuccess -> {
                scope.launch {
                    snackbarHostState.showTypedSnackbar(
                        (updateState as BarbershopUpdateState.UpdateSuccess).message,
                        type = SnackbarType.SUCCESS
                    )
                }
                barbershopViewModel.getBarbershopById(userId, localId)
                barbershopViewModel.resetUpdateState()
                isEditMode = false
            }
            is BarbershopUpdateState.UpdateError -> {
                scope.launch {
                    snackbarHostState.showTypedSnackbar(
                        (updateState as BarbershopUpdateState.UpdateError).message,
                        type = SnackbarType.ERROR
                    )
                }
                barbershopViewModel.resetUpdateState()
            }
            else -> {}
        }
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
                .background(brush = blackWhiteGradient)
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                AppBar(
                    navController = navController,
                    isNavigating = isNavigating,
                    onNavigatingChange = { isNavigating = it },
                    isAdmin = isAdmin,
                    isEditMode = isEditMode,
                    onEditModeChange = {
                        if (isEditMode && it && canSave) {
                            val updateData = mutableMapOf<String, String?>(
                                "nombre" to nombre,
                                "direccion" to direccion,
                                "telefono" to telefono,
                                "horarioapertura" to fullTimeFormat(horarioApertura),
                                "horariocierre" to fullTimeFormat(horarioCierre),
                                "descripcion" to descripcion
                            )
                            barbershopViewModel.updateBarbershop(localId, updateData)
                        } else {
                            isEditMode = it
                        }
                    },
                    defaultFont = defaultFont,
                    canSave = hasChanges && !isUpdating && isPhoneValid && isHorarioValido && requiredFieldsFilled
                )


                Spacer(modifier = Modifier.height(24.dp))

                when (singleBarbershopState) {
                    is SingleBarbershopState.Loading -> {
                        LoadingState()
                    }
                    is SingleBarbershopState.Error -> {
                        ErrorState(
                            message = (singleBarbershopState as SingleBarbershopState.Error).message,
                            defaultFont = defaultFont
                        )
                    }
                    is SingleBarbershopState.Success -> {
                        val barbershop = (singleBarbershopState as SingleBarbershopState.Success).barbershop
                        SuccessState(
                            navController = navController,
                            barbershop = barbershop,
                            isAdmin = isAdmin,
                            defaultFont = defaultFont,
                            isEditMode = isEditMode,
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            horarioApertura = horarioApertura,
                            horarioCierre = horarioCierre,
                            descripcion = descripcion,
                            onNombreChange = { nombre = it },
                            onDireccionChange = { direccion = it },
                            onTelefonoChange = { telefono = it },
                            onHorarioAperturaChange = { horarioApertura = it },
                            onHorarioCierreChange = { horarioCierre = it },
                            onDescripcionChange = { descripcion = it }
                        )
                    }
                    else -> { }
                }
            }
        }
    }
}

@Composable
fun AppBar(
    navController: NavController,
    isNavigating: Boolean,
    onNavigatingChange: (Boolean) -> Unit,
    isAdmin: Boolean,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    defaultFont: FontFamily,
    canSave: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.8f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (!isNavigating) {
                        onNavigatingChange(true)
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
                text = "Información",
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isAdmin) {
            Row {
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFB74A5A))
                            .clickable { onEditModeChange(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar edición",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val checkBgColor = if (canSave) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(checkBgColor)
                            .clickable(enabled = canSave) { onEditModeChange(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar cambios",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xC82196F3))
                            .clickable { onEditModeChange(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }


    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AccentColor,
            strokeWidth = 3.dp,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
fun ErrorState(message: String, defaultFont: FontFamily) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF521E1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = Color.White,
                style = TextStyle(fontFamily = defaultFont),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SuccessState(
    navController: NavController,
    barbershop: BarbershopResponse,
    isAdmin: Boolean,
    defaultFont: FontFamily,
    isEditMode: Boolean,
    nombre: String,
    direccion: String,
    telefono: String,
    horarioApertura: String,
    horarioCierre: String,
    descripcion: String,
    onNombreChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onHorarioAperturaChange: (String) -> Unit,
    onHorarioCierreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit
) {
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showHorarioDialog by remember { mutableStateOf(false) }
    val isPhoneValid = telefono.isEmpty() || CredentialsValidator.isPhoneValid(telefono)

    val isHorarioAperturaValido = CredentialsValidator.isHourValid(horarioApertura)
    val isHorarioCierreValido = CredentialsValidator.isHourValid(horarioCierre)
    val isHorarioValido = (isHorarioAperturaValido && isHorarioCierreValido) && (horarioApertura < horarioCierre)


    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            onNombreChange(barbershop.nombre)
            onDireccionChange(barbershop.direccion)
            onTelefonoChange(barbershop.telefono)
            onHorarioAperturaChange(barbershop.horarioapertura)
            onHorarioCierreChange(barbershop.horariocierre)
            onDescripcionChange(barbershop.descripcion ?: "")
        }
    }

    BarbershopHeaderCard(
        barbershop = barbershop,
        defaultFont = defaultFont,
        isEditMode = isEditMode,
        nombre = nombre,
        onValueChange = onNombreChange,
        localId = barbershop.localid,
        isAdmin = isAdmin,
        navController = navController
    )


    Spacer(modifier = Modifier.height(24.dp))

    if (isEditMode) {
        EditableInfoSection(
            title = "Dirección",
            content = direccion,
            onValueChange = onDireccionChange,
            icon = Icons.Default.LocationOn,
            defaultFont = defaultFont,
            iconTint = Color(0xFF4CAF50)
        )

        EditableInfoSection(
            title = "Teléfono",
            content = telefono,
            onValueChange = onTelefonoChange,
            icon = Icons.Default.Phone,
            defaultFont = defaultFont,
            iconTint = Color(0xFFE91E63)
        )
        if (!isPhoneValid) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver requisitos",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        color = Color(0xFFFD4E4E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                IconButton(onClick = { showPhoneDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.ChecklistRtl,
                        contentDescription = "Ver requisitos",
                        tint = Color(0xFFFD4E4E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        EditableHorarioSection(
            title = "Horario",
            horarioApertura = horarioApertura,
            horarioCierre = horarioCierre,
            onAperturaChange = onHorarioAperturaChange,
            onCierreChange = onHorarioCierreChange,
            defaultFont = defaultFont,
            iconTint = Color(0xFFFF9800)
        )
        if (!isHorarioValido) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver requisitos",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        color = Color(0xFFFD4E4E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                IconButton(onClick = { showHorarioDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.ChecklistRtl,
                        contentDescription = "Ver requisitos",
                        tint = Color(0xFFFD4E4E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }


        EditableInfoSection(
            title = "Descripción",
            content = descripcion,
            onValueChange = onDescripcionChange,
            icon = Icons.Default.Info,
            defaultFont = defaultFont,
            iconTint = AccentColor,
            singleLine = false
        )

        Spacer(modifier = Modifier.height(8.dp))

    } else {
        InfoSection(
            title = "Dirección",
            content = barbershop.direccion,
            icon = Icons.Default.LocationOn,
            defaultFont = defaultFont,
            iconTint = Color(0xFF4CAF50)
        )

        InfoSection(
            title = "Teléfono",
            content = barbershop.telefono,
            icon = Icons.Default.Phone,
            defaultFont = defaultFont,
            iconTint = Color(0xFFE91E63)
        )

        if (!isPhoneValid && telefono.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver requisitos",
                    style = TextStyle(
                        fontFamily = defaultFont,
                        color = Color(0xFF9A3939),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                IconButton(onClick = { showPhoneDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.ChecklistRtl,
                        contentDescription = "Ver requisitos",
                        tint = Color(0xFF9A3939),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        InfoSection(
            title = "Horario",
            content = barbershop.horarioapertura + " - " + barbershop.horariocierre,
            icon = Icons.Default.Schedule,
            defaultFont = defaultFont,
            iconTint = Color(0xFFFF9800)
        )

        InfoSection(
            title = "Descripción",
            content = barbershop.descripcion ?: "Sin descripción",
            icon = Icons.Default.Info,
            defaultFont = defaultFont,
            iconTint = AccentColor
        )
    }
    val dialogFont = defaultFont
    val dialogShape = RoundedCornerShape(12.dp)
    val dialogContainerColor = Color(0xFF9FCBE7)
    val dialogTitleColor = Color(0xFF132946)
    val dialogTextColor = Color(0xFF333333)

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            confirmButton = {
                Button(
                    onClick = { showPhoneDialog = false },
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
                    text = "Teléfono",
                    style = TextStyle(fontFamily = dialogFont, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    color = dialogTitleColor,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Solo puede contener dígitos\nLongitud entre 9 y 15 caracteres\nNo puede contener espacios",
                    style = TextStyle(fontFamily = dialogFont, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = dialogTextColor
                )
            },
            modifier = Modifier
                .clip(dialogShape)
                .background(Color.White),
            containerColor = dialogContainerColor,
            shape = dialogShape
        )
    }

    if (showHorarioDialog) {
        AlertDialog(
            onDismissRequest = { showHorarioDialog = false },
            confirmButton = {
                Button(
                    onClick = { showHorarioDialog = false },
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
                    text = "Horario",
                    style = TextStyle(fontFamily = dialogFont, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    color = dialogTitleColor,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Formato: HH:mm o HH:mm:ss\nHoras entre 00 y 23\nMinutos entre 00 y 59\nEjemplo válido: 09:30",
                    style = TextStyle(fontFamily = dialogFont, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = dialogTextColor
                )
            },
            modifier = Modifier
                .clip(dialogShape)
                .background(Color.White),
            containerColor = dialogContainerColor,
            shape = dialogShape
        )
    }
}

@Composable
fun BarbershopHeaderCard(
    barbershop: BarbershopResponse,
    defaultFont: FontFamily,
    isEditMode: Boolean = false,
    nombre: String = "",
    onValueChange: (String) -> Unit = {},
    navController: NavController,
    localId: Int,
    isAdmin: Boolean
) {
    var isNavigating by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                var isImageLoading by remember { mutableStateOf(true) }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(barbershop.imagen_url)
                        .crossfade(true)
                        .listener(
                            onSuccess = { _, _ -> isImageLoading = false },
                            onError = { _, _ -> isImageLoading = false }
                        )
                        .build(),
                    contentDescription = "Imagen de ${barbershop.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                if (isImageLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditMode) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = onValueChange,
                        label = { Text("Nombre del local", color = Color.White.copy(alpha = 0.7f)) },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = defaultFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFFD9D9D9),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = barbershop.nombre,
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                if (!isNavigating) {
                                    isNavigating = true
                                    navController.navigate("barbershop_reviews/$localId/$isAdmin")
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GoldColor,
                            modifier = Modifier.size(23.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${barbershop.rating} (${barbershop.cantidad_resenas} reseñas)",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: String,
    icon: ImageVector,
    defaultFont: FontFamily,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = content,
                    color = Color.White.copy(alpha = 0.8f),
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 16.sp,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
fun EditableInfoSection(
    title: String,
    content: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    defaultFont: FontFamily,
    iconTint: Color,
    singleLine: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = defaultFont,
                        color = Color.White
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    singleLine = singleLine,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EditableHorarioSection(
    title: String,
    horarioApertura: String,
    horarioCierre: String,
    onAperturaChange: (String) -> Unit,
    onCierreChange: (String) -> Unit,
    defaultFont: FontFamily,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = horarioApertura,
                        onValueChange = onAperturaChange,
                        label = { Text("Apertura", color = Color.White.copy(alpha = 0.7f)) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = defaultFont,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFFD9D9D9),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "-",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = horarioCierre,
                        onValueChange = onCierreChange,
                        label = { Text("Cierre", color = Color.White.copy(alpha = 0.7f)) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = defaultFont,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFFD9D9D9),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun fullTimeFormat(time: String): String {
    return if (time.length == 5) "$time:00" else time
}