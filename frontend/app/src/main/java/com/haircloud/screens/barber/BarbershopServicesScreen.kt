package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.haircloud.R
import com.haircloud.data.model.ServiceRequest
import com.haircloud.data.model.ServiceResponse
import com.haircloud.utils.CustomSnackbarHost
import com.haircloud.utils.SnackbarType
import com.haircloud.utils.showTypedSnackbar
import com.haircloud.utils.CredentialsValidator.convertPriceToDouble
import com.haircloud.utils.CredentialsValidator.isPriceInputValid
import com.haircloud.utils.CredentialsValidator.isPriceValid
import com.haircloud.viewmodel.BarberServiceOperationState
import com.haircloud.viewmodel.BarberServiceState
import com.haircloud.viewmodel.ServicesViewModel
import java.text.NumberFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarbershopServicesScreen(navController: NavController, localId: Int, isAdmin: Boolean = false, isSemiAdmin: Boolean = false) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val servicesViewModel = remember { ServicesViewModel() }
    val servicesState by servicesViewModel.servicesState.collectAsState()
    val createState by servicesViewModel.createState.collectAsState()
    val editState by servicesViewModel.editState.collectAsState()
    val deleteState by servicesViewModel.deleteState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    val blackWhiteGradient =
        Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))
    val darkSurface = Color(0xFF1E1E1E)

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<ServiceResponse?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredServices = when (val state = servicesState) {
        is BarberServiceState.Success -> {
            val query = searchQuery.lowercase()
            state.services.filter {
                it.nombre.lowercase().contains(query) ||
                        it.precio.toString().contains(query) ||
                        it.duracion.toString().contains(query)
            }
        }
        else -> emptyList()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(localId) {
        servicesViewModel.getServicesByLocalId(localId)
    }

    LaunchedEffect(editState) {
        when (val state = editState) {
            is BarberServiceOperationState.Success -> {
                snackbarMessage = "Servicio actualizado correctamente"
                snackbarType = SnackbarType.SUCCESS
                servicesViewModel.resetEditState()
                servicesViewModel.getServicesByLocalId(localId)
                showEditDialog = false
                selectedService = null
            }
            is BarberServiceOperationState.Error -> {
                snackbarMessage = "Error al editar: ${state.message}"
                snackbarType = SnackbarType.ERROR
                servicesViewModel.resetEditState()
            }
            else -> Unit
        }
    }


    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is BarberServiceOperationState.Success -> {
                snackbarMessage = "Servicio eliminado correctamente"
                snackbarType = SnackbarType.SUCCESS
                servicesViewModel.resetDeleteState()
                servicesViewModel.getServicesByLocalId(localId)
            }
            is BarberServiceOperationState.Error -> {
                snackbarMessage = "Error al eliminar: ${state.message}"
                snackbarType = SnackbarType.ERROR
                servicesViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(createState) {
        when (createState) {
            is BarberServiceOperationState.Success -> {
                snackbarMessage = (createState as BarberServiceOperationState.Success).message
                snackbarType = SnackbarType.SUCCESS
                servicesViewModel.getServicesByLocalId(localId)
                servicesViewModel.resetCreateState()
            }
            is BarberServiceOperationState.Error -> {
                snackbarMessage = (createState as BarberServiceOperationState.Error).message
                snackbarType = SnackbarType.ERROR
                servicesViewModel.resetCreateState()
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
                            text = if (isAdmin || isSemiAdmin) "Administrar servicios" else "Servicios",
                            color = Color.White,
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isAdmin || isSemiAdmin) {
                        IconButton(
                            onClick = { showCreateDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir servicio",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (servicesState) {
                    is BarberServiceState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is BarberServiceState.Error -> {
                        val errorMessage = (servicesState as BarberServiceState.Error).message
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = TextStyle(fontFamily = defaultFont),
                                fontSize = 16.sp
                            )
                        }
                    }
                    is BarberServiceState.Success -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscador (nombre, precio o duración)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.White
                                )
                            },
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

                        if (filteredServices.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = "Sin resultados",
                                        tint = Color.Gray.copy(alpha = 0.6f),
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se encontraron servicios",
                                        color = Color.Gray.copy(alpha = 0.8f),
                                        fontSize = 19.sp,
                                        fontFamily = defaultFont
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredServices) { service ->
                                    ServiceCard(
                                        service = service,
                                        defaultFont = defaultFont,
                                        darkSurface = darkSurface,
                                        isAdmin = isAdmin,
                                        isSemiAdmin = isSemiAdmin,
                                        onEdit = {
                                            selectedService = it
                                            showEditDialog = true
                                        },
                                        onDelete = { servicioId ->
                                            servicesViewModel.deleteService(servicioId)
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }

                    }
                    else -> {  }
                }
            }

            if (showCreateDialog) {
                CreateServiceDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreateService = { serviceRequest ->
                        servicesViewModel.createService(serviceRequest)
                        showCreateDialog = false
                    },
                    localId = localId,
                    defaultFont = defaultFont
                )
            }
        }
        if (showEditDialog && selectedService != null) {
            EditServiceDialog(
                service = selectedService!!,
                onDismiss = {
                    showEditDialog = false
                    selectedService = null
                    servicesViewModel.resetEditState()
                },
                onEditService = { servicioId, request ->
                    servicesViewModel.editService(servicioId, request)
                },
                defaultFont = defaultFont
            )
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceResponse,
    defaultFont: FontFamily,
    darkSurface: Color,
    isAdmin: Boolean = false,
    isSemiAdmin: Boolean = false,
    onEdit: (ServiceResponse) -> Unit,
    onDelete: (Int) -> Unit
) {
    val locale = Locale("es", "ES")
    val format = NumberFormat.getCurrencyInstance(locale).apply {
        minimumFractionDigits = 0
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var expandedDescription by remember { mutableStateOf(false) }

    val descriptionMaxLines = if (expandedDescription) Int.MAX_VALUE else 2

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = darkSurface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.nombre,
                    color = Color.White,
                    style = TextStyle(fontFamily = defaultFont),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF37628C),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = format.format(service.precio),
                        color = Color(0xFFFFFFFF),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (!service.descripcion.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Text(
                        text = service.descripcion,
                        color = Color.White.copy(alpha = 0.85f),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 15.sp,
                        maxLines = descriptionMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { expandedDescription = !expandedDescription }
                    )

                    if (!expandedDescription && service.descripcion.length > 90) {
                        Text(
                            text = "Mostrar más",
                            color = Color(0xFF90CAF9),
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expandedDescription = true }
                        )
                    } else if (expandedDescription) {
                        Text(
                            text = "Mostrar menos",
                            color = Color(0xFF90CAF9),
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expandedDescription = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Duración",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${service.duracion} minutos",
                        color = Color.White.copy(alpha = 0.8f),
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 17.sp
                    )
                }

                if (isAdmin || isSemiAdmin) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFF2C2C2C))
                        ) {
                            DropdownMenuItem(
                                modifier = Modifier.background(Color(0x8146719D)),
                                text = {
                                    Text(
                                        "Editar",
                                        color = Color.White,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 17.sp
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit(service)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                }
                            )
                            DropdownMenuItem(
                                modifier = Modifier.background(Color(0x94C24747)),
                                text = {
                                    Text(
                                        "Eliminar",
                                        color = Color.White,
                                        style = TextStyle(fontFamily = defaultFont),
                                        fontSize = 17.sp
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete(service.servicioid)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CreateServiceDialog(
    onDismiss: () -> Unit,
    onCreateService: (ServiceRequest) -> Unit,
    localId: Int,
    defaultFont: FontFamily
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var duracionText by remember { mutableStateOf("") }
    var precioError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF212121)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Nuevo servicio",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del servicio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.ContentCut, contentDescription = "Servicio", tint = Color.Gray)
                            },
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.ChatBubble, contentDescription = "Descripción", tint = Color.Gray)
                            },
                            maxLines = 3
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = duracionText,
                            onValueChange = {
                                if (it.isEmpty() || it.all(Char::isDigit)) duracionText = it
                            },
                            label = { Text("Duración (min)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = "Duración", tint = Color.Gray)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = precioText,
                            onValueChange = {
                                precioText = it
                                precioError = if (it.isEmpty() || isPriceInputValid(it)) null
                                else "Formato inválido: Solo números y máx. 2 decimales"
                            },
                            label = { Text("Precio (€)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(error = precioError != null),
                            leadingIcon = {
                                Icon(Icons.Default.Euro, contentDescription = "Precio", tint = Color.Gray)
                            },
                            isError = precioError != null,
                            supportingText = {
                                if (precioError != null)
                                    Text(precioError!!, color = Color(0xFFFF5252))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1D1D1D))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF888888)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFFCCCCCC)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFCCCCCC)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancelar",
                                style = TextStyle(
                                    fontFamily = defaultFont,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val precio = convertPriceToDouble(precioText)
                                val duracion = duracionText.toIntOrNull() ?: 30
                                onCreateService(
                                    ServiceRequest(
                                        nombre = nombre,
                                        descripcion = descripcion.ifEmpty { null },
                                        precio = precio,
                                        duracion = duracion,
                                        localId = localId
                                    )
                                )
                            },
                            enabled = nombre.isNotBlank() && isPriceValid(precioText) && duracionText.isNotBlank(),
                            modifier = Modifier
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar",
                                style = TextStyle(
                                    fontFamily = defaultFont,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun customTextFieldColors(error: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = if (error) Color(0xFFFF5252) else Color(0xFF2196F3),
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = if (error) Color(0xFFFF5252) else Color(0xFF2196F3),
    unfocusedLabelColor = Color.Gray,
    cursorColor = if (error) Color(0xFFFF5252) else Color(0xFF2196F3),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorTextColor = Color.White,
    errorBorderColor = Color(0xFFFF5252),
    errorLabelColor = Color(0xFFFF5252),
    errorCursorColor = Color(0xFFFF5252)
)

@Composable
fun EditServiceDialog(
    service: ServiceResponse,
    onDismiss: () -> Unit,
    onEditService: (Int, ServiceRequest) -> Unit,
    defaultFont: FontFamily
) {
    var nombre by remember { mutableStateOf(service.nombre) }
    var descripcion by remember { mutableStateOf(service.descripcion ?: "") }
    var precioText by remember { mutableStateOf(service.precio.toString()) }
    var duracionText by remember { mutableStateOf(service.duracion.toString()) }
    var precioError by remember { mutableStateOf<String?>(null) }

    val hasChanges = nombre != service.nombre ||
            descripcion != (service.descripcion ?: "") ||
            precioText != service.precio.toString() ||
            duracionText != service.duracion.toString()

    val isFormValid = nombre.isNotBlank() && isPriceValid(precioText) && duracionText.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF212121)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Editar servicio",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del servicio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.ContentCut, contentDescription = "Servicio", tint = Color.Gray)
                            },
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.ChatBubble, contentDescription = "Descripción", tint = Color.Gray)
                            },
                            maxLines = 3
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = duracionText,
                            onValueChange = {
                                if (it.isEmpty() || it.all(Char::isDigit)) duracionText = it
                            },
                            label = { Text("Duración (min)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(),
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = "Duración", tint = Color.Gray)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = precioText,
                            onValueChange = {
                                precioText = it
                                precioError = if (it.isEmpty() || isPriceInputValid(it)) null
                                else "Formato inválido: Solo números y máx. 2 decimales"
                            },
                            label = { Text("Precio (€)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors(error = precioError != null),
                            leadingIcon = {
                                Icon(Icons.Default.Euro, contentDescription = "Precio", tint = Color.Gray)
                            },
                            isError = precioError != null,
                            supportingText = {
                                if (precioError != null)
                                    Text(precioError!!, color = Color(0xFFFF5252))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1D1D1D))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Gray
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancelar", style = TextStyle(fontFamily = defaultFont, fontWeight = FontWeight.Medium))
                        }

                        Button(
                            onClick = {
                                val precio = convertPriceToDouble(precioText)
                                val duracion = duracionText.toIntOrNull() ?: 30
                                onEditService(
                                    service.servicioid,
                                    ServiceRequest(
                                        nombre = nombre,
                                        descripcion = descripcion.ifEmpty { null },
                                        precio = precio,
                                        duracion = duracion,
                                        localId = service.localid
                                    )
                                )
                            },
                            enabled = hasChanges && isFormValid,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar", style = TextStyle(fontFamily = defaultFont, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}