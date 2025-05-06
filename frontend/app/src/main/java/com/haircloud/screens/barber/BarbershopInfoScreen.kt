package com.haircloud.screens.barber

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

private val DarkSurface = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00B0FF)
private val GoldColor = Color(0xFFFFC107)
private val blackWhiteGradient =
    Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BarbershopInfoScreen(navController: NavController, localId: Int, userId: Int) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isNavigating by remember { mutableStateOf(false) }
    val barbershopViewModel = remember { BarbershopViewModel() }
    val singleBarbershopState by barbershopViewModel.singleBarbershopState.collectAsState()
    val scrollState = rememberScrollState()

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showTypedSnackbar(it, type = snackbarType)
            snackbarMessage = null
        }
    }

    LaunchedEffect(localId, userId) {
        barbershopViewModel.getBarbershopById(userId, localId)
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
                    defaultFont = defaultFont
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
                        SuccessState(barbershop = barbershop, defaultFont = defaultFont)
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
    defaultFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.8f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun SuccessState(barbershop: BarbershopResponse, defaultFont: FontFamily) {
    BarbershopHeaderCard(barbershop = barbershop, defaultFont = defaultFont)

    Spacer(modifier = Modifier.height(24.dp))

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

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun BarbershopHeaderCard(barbershop: BarbershopResponse, defaultFont: FontFamily) {
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



@Composable
fun InfoSection(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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