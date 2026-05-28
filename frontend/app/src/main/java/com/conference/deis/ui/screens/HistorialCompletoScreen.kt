package com.conference.deis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.HistorialIntentoResponse
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialCompletoScreen(
    navController: NavHostController
) {
    val usuarioId = UserSession.user?.id?.toString()

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var historial by remember { mutableStateOf<List<HistorialIntentoResponse>>(emptyList()) }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    LaunchedEffect(usuarioId) {
        if (usuarioId == null) {
            error = "No se encontró el usuario en sesión."
            cargando = false
            return@LaunchedEffect
        }

        try {
            cargando = true
            error = null

            val response = RetrofitInstance.api.obtenerHistorialDetallado(usuarioId)

            if (response.isSuccessful) {
                historial = response.body().orEmpty()
            } else {
                error = "No se pudo cargar el historial."
            }
        } catch (e: Exception) {
            error = "Ocurrió un error al cargar el historial."
        } finally {
            cargando = false
        }
    }

    val historialFiltrado = when (filtroSeleccionado) {
        "Simulacros" -> historial.filter { it.tipo.uppercase() == "SIMULACRO" }
        "Prácticas" -> historial.filter { it.tipo.uppercase() == "PRACTICA" }
        else -> historial
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Historial completo",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF007AFF),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Tus prácticas y simulacros",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )

                Text(
                    text = "Consulta todos tus intentos registrados, ordenados del más reciente al más antiguo.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 17.sp
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HistorialFiltroButton(
                        texto = "Todos",
                        seleccionado = filtroSeleccionado == "Todos",
                        onClick = { filtroSeleccionado = "Todos" },
                        modifier = Modifier.weight(1f)
                    )

                    HistorialFiltroButton(
                        texto = "Simulacros",
                        seleccionado = filtroSeleccionado == "Simulacros",
                        onClick = { filtroSeleccionado = "Simulacros" },
                        modifier = Modifier.weight(1f)
                    )

                    HistorialFiltroButton(
                        texto = "Prácticas",
                        seleccionado = filtroSeleccionado == "Prácticas",
                        onClick = { filtroSeleccionado = "Prácticas" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when {
                cargando -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                error != null -> {
                    item {
                        HistorialCard {
                            Text(
                                text = error ?: "Error desconocido",
                                color = Color.Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                historialFiltrado.isEmpty() -> {
                    item {
                        HistorialCard {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aún no tienes historial registrado.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF101828)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Completa una práctica o simulacro para que aparezca aquí.",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        Text(
                            text = "Intentos encontrados: ${historialFiltrado.size}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    items(historialFiltrado) { intento ->
                        HistorialCompletoItem(
                            intento = intento,
                            onClick = {
                                intento.id?.let { id ->
                                    navController.navigate("detalle_intento/$id")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialCompletoItem(
    intento: HistorialIntentoResponse,
    onClick: () -> Unit
) {
    val porcentaje = intento.nota.roundToInt()
    val colorEstado = colorEstadoHistorial(intento.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = intento.tipo.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101828)
                    )

                    Text(
                        text = formatoFechaHistorial(intento.fecha),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "$porcentaje%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (porcentaje / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = colorEstado,
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistorialDatoPequeno(
                    titulo = "Aciertos",
                    valor = intento.respuestasCorrectas.toString()
                )

                HistorialDatoPequeno(
                    titulo = "Errores",
                    valor = intento.respuestasIncorrectas.toString()
                )

                HistorialDatoPequeno(
                    titulo = "Total",
                    valor = intento.totalPreguntas.toString()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .background(colorEstado.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = textoEstadoHistorial(intento.estado),
                    color = colorEstado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Toca para ver el detalle de este intento.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HistorialDatoPequeno(
    titulo: String,
    valor: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Text(
            text = titulo,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun HistorialFiltroButton(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (seleccionado) Color(0xFFE8F2FF) else Color.White
    val textColor = if (seleccionado) Color(0xFF007AFF) else Color.Gray

    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = texto,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun HistorialCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

private fun colorEstadoHistorial(estado: String): Color {
    return when (estado) {
        "ALTO" -> Color(0xFF16A34A)
        "MEDIO" -> Color(0xFF007AFF)
        else -> Color(0xFFFF6D00)
    }
}

private fun textoEstadoHistorial(estado: String): String {
    return when (estado) {
        "ALTO" -> "Buen resultado"
        "MEDIO" -> "En proceso"
        else -> "Debe mejorar"
    }
}

private fun formatoFechaHistorial(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "Sin fecha"

    val partes = fecha.split("T")
    if (partes.size < 2) return fecha

    val fechaPartes = partes[0].split("-")
    val hora = partes[1].take(5)

    if (fechaPartes.size < 3) return fecha

    return "${fechaPartes[2]}/${fechaPartes[1]}/${fechaPartes[0]} · $hora"
}