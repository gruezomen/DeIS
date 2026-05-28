package com.conference.deis.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.ErrorPreguntaCategoriaResponse
import com.conference.deis.network.model.ErroresPorCategoriaResponse
import com.conference.deis.network.model.RendimientoCategoriaResponse
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.conference.deis.network.model.HistorialIntentoResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEstadisticasScreen(
    navController: NavHostController
) {
    val usuarioId = UserSession.user?.id?.toString()

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var rendimientoCategorias by remember {
        mutableStateOf<List<RendimientoCategoriaResponse>>(emptyList())
    }
    var erroresPorCategoria by remember {
        mutableStateOf<List<ErroresPorCategoriaResponse>>(emptyList())
    }

    var historialIntentos by remember {
        mutableStateOf<List<HistorialIntentoResponse>>(emptyList())
    }

    var areaSeleccionada by remember {
        mutableStateOf<String?>(null)
    }

    var mostrarSimulacros by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(usuarioId) {
        if (usuarioId == null) {
            error = "No se encontró el usuario en sesión."
            cargando = false
            return@LaunchedEffect
        }

        try {
            cargando = true
            error = null

            val responseRendimiento =
                RetrofitInstance.api.obtenerRendimientoPorCategoria(usuarioId)

            val responseErrores =
                RetrofitInstance.api.obtenerErroresPorCategoria(usuarioId)
            val responseHistorial =
                RetrofitInstance.api.obtenerHistorialDetallado(usuarioId)

            if (responseRendimiento.isSuccessful) {
                rendimientoCategorias = responseRendimiento.body().orEmpty()
            }

            if (responseErrores.isSuccessful) {
                erroresPorCategoria = responseErrores.body().orEmpty()
            }
            if (responseHistorial.isSuccessful) {
                historialIntentos = responseHistorial.body().orEmpty()
            }
        } catch (e: Exception) {
            error = "Ocurrió un error al cargar el detalle de estadísticas."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle de estadísticas",
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
        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F7FB))
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F7FB))
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            else -> {
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
                            text = "Rendimiento por área",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101828)
                        )

                        Text(
                            text = "Aquí puedes revisar tus aciertos, errores y preguntas falladas agrupadas por área.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 17.sp
                        )
                    }

                    item {
                        CardDetalle {
                            Text(
                                text = "Resumen por área",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (rendimientoCategorias.isEmpty()) {
                                Text(
                                    text = "Aún no existen estadísticas por área.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            } else {
                                rendimientoCategorias
                                    .sortedBy { it.porcentaje }
                                    .forEach { categoria ->
                                        CategoriaResumenItem(
                                            categoria = categoria,
                                            seleccionada = areaSeleccionada == categoria.categoria,
                                            onClick = {
                                                areaSeleccionada = if (areaSeleccionada == categoria.categoria) {
                                                    null
                                                } else {
                                                    categoria.categoria
                                                }
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
                            }
                        }
                    }

                    item {
                        CardDetalle {
                            Text(
                                text = "Errores del área seleccionada",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val erroresDelArea = areaSeleccionada?.let { seleccion ->
                                erroresPorCategoria.firstOrNull {
                                    it.categoria.equals(seleccion, ignoreCase = true)
                                }
                            }

                            when {
                                areaSeleccionada == null -> {
                                    Text(
                                        text = "Selecciona un área del resumen para ver solo sus errores.",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        lineHeight = 17.sp
                                    )
                                }

                                erroresDelArea == null || erroresDelArea.errores.isEmpty() -> {
                                    Text(
                                        text = "No tienes errores registrados en ${nombreCategoriaVisible(areaSeleccionada ?: "")}.",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        lineHeight = 17.sp
                                    )
                                }

                                else -> {
                                    Text(
                                        text = "${nombreCategoriaVisible(erroresDelArea.categoria)} · ${erroresDelArea.totalErrores} errores",
                                        color = Color(0xFFFF6D00),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    erroresDelArea.errores.take(8).forEach { error ->
                                        ErrorPreguntaItem(error)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (erroresDelArea.errores.size > 8) {
                                        Text(
                                            text = "Y ${erroresDelArea.errores.size - 8} errores más en esta área.",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun CategoriaResumenItem(
    categoria: RendimientoCategoriaResponse,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val porcentaje = categoria.porcentaje.roundToInt()
    val color = colorCategoriaDetalle(categoria.estado)
    val fondo = if (seleccionada) Color(0xFFE8F2FF) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = nombreCategoriaVisible(categoria.categoria),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )

                Text(
                    text = textoEstadoCategoria(categoria.estado),
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "$porcentaje%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (porcentaje / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = color,
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${categoria.totalPreguntas} respondidas · ${categoria.correctas} aciertos · ${categoria.incorrectas} errores",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (seleccionada) {
                "Área seleccionada. Abajo verás sus errores registrados."
            } else {
                "Toca esta área para revisar sus errores."
            },
            fontSize = 11.sp,
            color = if (seleccionada) Color(0xFF007AFF) else Color.Gray
        )
    }
}

@Composable
private fun ErroresCategoriaItem(
    grupo: ErroresPorCategoriaResponse
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${nombreCategoriaVisible(grupo.categoria)} · ${grupo.totalErrores} errores",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6D00)
        )

        Spacer(modifier = Modifier.height(8.dp))

        grupo.errores.take(5).forEach { error ->
            ErrorPreguntaItem(error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (grupo.errores.size > 5) {
            Text(
                text = "Y ${grupo.errores.size - 5} errores más en esta área.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ErrorPreguntaItem(
    error: ErrorPreguntaCategoriaResponse
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF7ED), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = error.enunciado,
            color = Color(0xFF101828),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu respuesta: ${error.respuestaSeleccionada}",
            color = Color(0xFFFF6D00),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Respuesta correcta: ${error.respuestaCorrecta}",
            color = Color(0xFF16A34A),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SimulacroEstadisticaItem(
    intento: HistorialIntentoResponse,
    onClick: () -> Unit
) {
    val porcentaje = intento.nota.roundToInt()
    val color = when (intento.estado) {
        "ALTO" -> Color(0xFF16A34A)
        "MEDIO" -> Color(0xFF007AFF)
        else -> Color(0xFFFF6D00)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = intento.tipo.lowercase().replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF101828)
                )

                Text(
                    text = formatoFechaEstadistica(intento.fecha),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "$porcentaje%",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${intento.respuestasCorrectas} aciertos · ${intento.respuestasIncorrectas} errores · ${intento.totalPreguntas} preguntas",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Toca para ver las preguntas respondidas.",
            color = Color(0xFF007AFF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CardDetalle(
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

private fun colorCategoriaDetalle(estado: String): Color {
    return when (estado) {
        "AREA_FUERTE" -> Color(0xFF16A34A)
        "AREA_DEBIL" -> Color(0xFFFF6D00)
        else -> Color(0xFF007AFF)
    }
}

private fun textoEstadoCategoria(estado: String): String {
    return when (estado) {
        "AREA_FUERTE" -> "Fortaleza"
        "AREA_DEBIL" -> "Área a reforzar"
        else -> "En proceso"
    }
}

private fun nombreCategoriaVisible(nombre: String): String {
    return when (nombre.trim().lowercase()) {
        "matematicas", "matemáticas" -> "Matemáticas"
        "fisica", "física" -> "Física"
        "quimica", "química" -> "Química"
        "biologia", "biología" -> "Biología"
        else -> nombre
    }
}

private fun formatoFechaEstadistica(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "Sin fecha"

    val partes = fecha.split("T")
    if (partes.size < 2) return fecha

    val fechaPartes = partes[0].split("-")
    val hora = partes[1].take(5)

    if (fechaPartes.size < 3) return fecha

    return "${fechaPartes[2]}/${fechaPartes[1]}/${fechaPartes[0]} · $hora"
}