package com.haircloud.screens.barber

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.haircloud.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarberManualScreen(navController: NavController) {
    val blackWhiteGradient = Brush.verticalGradient(colors = listOf(Color(0xFF212121), Color(0xFF666F77)))
    val defaultFont = FontFamily(Font(R.font.default_font, FontWeight.Normal))

    Scaffold(
        modifier = Modifier
            .background(brush = blackWhiteGradient)
            .fillMaxSize(),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFD9D9D9),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF3B3B3B),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.app_lightlogo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(50.dp)
                            .wrapContentHeight(),
                        contentScale = ContentScale.Inside
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Manual de Usuario",
                        color = Color.White,
                        style = TextStyle(fontFamily = defaultFont),
                        fontSize = 45.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Navegación",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Logo: Accede a este manual\n• Foto de perfil: Accede a tu perfil personal\n• Menú inferior: Navega entre secciones",
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Home",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Fecha: Selecciona un día en el calendario\n• Citas: Gestiona tus citas del día seleccionado\n• Filtro: Muestra solo las citas pendientes/completadas/no completadas/canceladas\n" +
                                            "• Ve la información del cliente y de su reserva, y cambia el estado de la cita",
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Calendario",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Navegación: Navega hasta 2 meses anteriores, y 4 meses posteriores a hoy\n• Fechas en rojo: Días que no trabajas\n• Fechas con punto azul: Días en los que tienes al menos una cita",
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Ajustes",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Info del local: Ve la info y reseñas del local, y modifica los datos si eres admin\n• Servicios: Ve los servicios del local, y añade/edita/elimina si eres admin\n• Peluqueros: Ve los peluqueros del local, y añade/elimina u otorga permisos de semi-admin si eres admin\n• Peluqueros > Añadir: Añade un peluquero nuevo desde cero, o añade un peluquero que ya estuvo anteriormente",
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Estadísticas",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Selector de fecha: Elige un rango de fechas para ver las estadísticas. Por defecto verás las del mes actual\n" +
                                            "• Opciones: Elige entre ver las estadísticas de hoy, esta semana, este mes, este año, o personalizado\n" +
                                            "• Guardar datos: Recibe las estadísticas generadas por correo electrónico"
                                    ,
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xE6242424)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Perfil",
                                    color = Color.White,
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Datos personales: Edita tu información\n• Horario de trabajo: Ve tu horario laboral o modifícalo\n" +
                                            "• Cerrar sesión: Cierra tu sesión con el botón rojo",
                                    color = Color(0xFFE0E0E0),
                                    style = TextStyle(fontFamily = defaultFont),
                                    fontSize = 22.sp,
                                    lineHeight = 30.sp
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "© HairCloud 2025",
                            color = Color(0xFFB0B0B0),
                            style = TextStyle(fontFamily = defaultFont),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}