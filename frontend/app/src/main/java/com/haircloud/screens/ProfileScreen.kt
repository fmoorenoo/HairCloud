package com.haircloud.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.utils.CredentialsValidator
import com.haircloud.viewmodel.ClientViewModel
import com.haircloud.viewmodel.ClientState
import com.haircloud.viewmodel.UpdateState
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, userId: Int?) {
    val clientViewModel: ClientViewModel = viewModel()
    val clientState by clientViewModel.clientState.collectAsState()
    val updateState by clientViewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(userId) {
        userId?.let {
            clientViewModel.getClient(it)
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateState.UpdateSuccess -> {
                scope.launch {
                    snackbarHostState.showTypedSnackbar(
                        (updateState as UpdateState.UpdateSuccess).message,
                        type = SnackbarType.SUCCESS
                    )
                }
                userId?.let { clientViewModel.getClient(it) }
                clientViewModel.resetUpdateState()
            }
            is UpdateState.UpdateError -> {
                scope.launch {
                    snackbarHostState.showTypedSnackbar(
                        (updateState as UpdateState.UpdateError).message,
                        type = SnackbarType.ERROR
                    )
                }
                clientViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = blackWhiteGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Perfil",
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            when (clientState) {
                is ClientState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is ClientState.Success -> {
                    val client = (clientState as ClientState.Success).client

                    var isEditMode by remember { mutableStateOf(false) }

                    var nombre by remember { mutableStateOf(client.nombre) }
                    var email by remember { mutableStateOf(client.email) }
                    var nombreUsuario by remember { mutableStateOf(client.nombreusuario) }
                    var telefono by remember { mutableStateOf(client.telefono ?: "Sin especificar") }

                    var showDialog by remember { mutableStateOf(false) }
                    var dialogMessage by remember { mutableStateOf("") }
                    var dialogTitle by remember { mutableStateOf("") }

                    val isUsernameValid = CredentialsValidator.isUsernameValid(nombreUsuario)
                    val isPhoneValid = CredentialsValidator.isPhoneValid(telefono)

                    val hasChanges = nombre != client.nombre ||
                            email != client.email ||
                            nombreUsuario != client.nombreusuario ||
                            telefono != (client.telefono ?: "Sin especificar")

                    val allFieldsValid = isUsernameValid && nombre.isNotEmpty() && email.isNotEmpty()

                    fun resetValues() {
                        nombre = client.nombre
                        email = client.email
                        nombreUsuario = client.nombreusuario
                        telefono = client.telefono ?: ""
                        isEditMode = false
                    }

                    fun saveChanges() {
                        val phone = if (telefono.trim().isEmpty() || telefono == "Sin especificar") "nulo" else telefono
                        val updateData = mutableMapOf<String, String?>(
                            "nombre" to nombre,
                            "email" to email,
                            "nombreusuario" to nombreUsuario,
                            "telefono" to phone
                        )

                        clientViewModel.updateClient(client.usuarioid, updateData)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.user_profile_1),
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .height(80.dp)
                                    .wrapContentHeight(),
                                contentScale = ContentScale.Inside
                            )
                        }
                    }

                    if (updateState is UpdateState.Updating) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // LazyColumn para las tarjetas
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        // Tarjeta de información personal
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0x4DB6B6B6)),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Información Personal",
                                            fontSize = 28.sp,
                                            fontFamily = defaultFont,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        IconButton(
                                            onClick = {
                                                if (!isEditMode && updateState !is UpdateState.Updating) {
                                                    isEditMode = true
                                                }
                                            },
                                            enabled = updateState !is UpdateState.Updating
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    if (isEditMode) {
                                        EditInfoField(Icons.Default.Person, "Nombre", nombre,
                                            onValueChange = { nombre = it }, defaultFont)

                                        EditInfoField(Icons.Default.Email, "Email", email,
                                            onValueChange = { email = it }, defaultFont)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.AccountCircle,
                                                contentDescription = "Usuario",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    "Usuario",
                                                    fontSize = 22.sp,
                                                    fontFamily = defaultFont,
                                                    color = Color(0xFFD9D9D9)
                                                )
                                                OutlinedTextField(
                                                    value = nombreUsuario,
                                                    onValueChange = { nombreUsuario = it },
                                                    textStyle = TextStyle(
                                                        fontSize = 20.sp,
                                                        fontFamily = defaultFont,
                                                        color = Color.White
                                                    ),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = if (isUsernameValid || nombreUsuario.isEmpty()) Color.White else Color(0xFFB74A5A),
                                                        unfocusedBorderColor = if (isUsernameValid || nombreUsuario.isEmpty()) Color(0xFFD9D9D9) else Color(0xFFB74A5A),
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        cursorColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }

                                        if (!isUsernameValid && nombreUsuario.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Text(
                                                    text = "Ver requisitos",
                                                    style = TextStyle(
                                                        fontFamily = defaultFont,
                                                        color = Color(0xFF9A3939),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )

                                                IconButton(onClick = {
                                                    dialogTitle = "Nombre de usuario"
                                                    dialogMessage = "Entre 6 y 20 caracteres\n" +
                                                            "Solo letras, números, '_' y '.'\n" +
                                                            "No puede contener espacios"
                                                    showDialog = true
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.ChecklistRtl,
                                                        contentDescription = "Información",
                                                        tint = Color(0xFF9A3939),
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = "Teléfono",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    "Teléfono",
                                                    fontSize = 22.sp,
                                                    fontFamily = defaultFont,
                                                    color = Color(0xFFD9D9D9)
                                                )
                                                OutlinedTextField(
                                                    value = telefono,
                                                    onValueChange = { telefono = it },
                                                    textStyle = TextStyle(
                                                        fontSize = 20.sp,
                                                        fontFamily = defaultFont,
                                                        color = Color.White
                                                    ),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = if (isPhoneValid || telefono.isEmpty()) Color.White else Color(0xFFB74A5A),
                                                        unfocusedBorderColor = if (isPhoneValid || telefono.isEmpty()) Color(0xFFD9D9D9) else Color(0xFFB74A5A),
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        cursorColor = Color.White
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }

                                        if (!isPhoneValid && telefono.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Text(
                                                    text = "Ver requisitos",
                                                    style = TextStyle(
                                                        fontFamily = defaultFont,
                                                        color = Color(0xFF9A3939),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )

                                                IconButton(onClick = {
                                                    dialogTitle = "Teléfono"
                                                    dialogMessage = "Solo puede contener dígitos\n" +
                                                            "Longitud entre 9 y 15 caracteres\n" +
                                                            "No puede contener espacios"
                                                    showDialog = true
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.ChecklistRtl,
                                                        contentDescription = "Información",
                                                        tint = Color(0xFF9A3939),
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            }
                                        }



                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Button(
                                                onClick = { resetValues() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFB74A5A)
                                                ),
                                                enabled = updateState !is UpdateState.Updating,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    "Cancelar",
                                                    color = Color.White,
                                                    fontFamily = defaultFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }

                                            Button(
                                                onClick = { saveChanges() },
                                                enabled = hasChanges && allFieldsValid && updateState !is UpdateState.Updating,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF439B3E),
                                                    disabledContainerColor = Color(0x66545454)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 8.dp)
                                            ) {
                                                Text(
                                                    "Guardar",
                                                    color = Color.White,
                                                    fontFamily = defaultFont,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    } else {
                                        InfoRow(Icons.Default.Person, "Nombre", client.nombre, Color.White, defaultFont)
                                        InfoRow(Icons.Default.Email, "Email", client.email, Color.White, defaultFont)
                                        InfoRow(Icons.Default.AccountCircle, "Usuario", client.nombreusuario, Color.White, defaultFont)
                                        InfoRow(Icons.Default.Phone, "Teléfono", client.telefono ?: "No especificado", Color.White, defaultFont)
                                    }
                                }
                            }
                        }

                        // Tarjeta de historial
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0x4DB6B6B6)),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Historial",
                                        fontSize = 28.sp,
                                        fontFamily = defaultFont,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    InfoRow(Icons.Default.DateRange, "Fecha de registro", client.fecharegistro, Color.White, defaultFont)
                                    InfoRow(Icons.Default.Event, "Última cita", client.ultimacita ?: "No hay citas", Color.White, defaultFont)
                                }
                            }
                        }

                        // Botón de cerrar sesión
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            navController.navigate("login")
                                        }
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color(0xA6FF5959)),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Cerrar Sesión",
                                        fontSize = 28.sp,
                                        fontFamily = defaultFont,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    // Diálogo de requisitos
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            confirmButton = {
                                Button(
                                    onClick = { showDialog = false },
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
                                    text = dialogTitle,
                                    style = TextStyle(fontFamily = defaultFont, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF132946),
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = dialogMessage,
                                    style = TextStyle(fontFamily = defaultFont, fontSize = 20.sp, fontWeight = FontWeight.Bold),
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
                }
                is ClientState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Error al cargar el perfil: ${(clientState as ClientState.Error).message}",
                            color = Color.White,
                            fontFamily = defaultFont,
                            fontSize = 22.sp
                        )
                    }
                }
                ClientState.Idle -> {
                }
            }
        }

        // SnackbarHost
        CustomSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
            defaultFont = defaultFont
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    fontFamily: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                label,
                fontSize = 22.sp,
                fontFamily = fontFamily,
                color = Color(0xFFD9D9D9)
            )
            Text(
                value,
                fontSize = 20.sp,
                fontFamily = fontFamily,
                color = textColor
            )
        }
    }
}

@Composable
fun EditInfoField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fontFamily: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                label,
                fontSize = 22.sp,
                fontFamily = fontFamily,
                color = Color(0xFFD9D9D9)
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = fontFamily,
                    color = Color.White
                ),
                enabled = label != "Email",
                trailingIcon = {
                    if (label == "Email")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color(0xFF494949)
                        ) else null
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    disabledBorderColor = Color(0xFFD9D9D9)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}