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
import com.conference.deis.network.model.HistorialIntentoResponse
import kotlin.math.roundToInt
import com.conference.deis.network.model.DetalleRespuestaIntentoResponse
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIntentoScreen(
    navController: NavHostController,
    intentoId: String
) {
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableStateOf<HistorialIntentoResponse?>(null) }
    var respuestas by remember {
    mutableStateOf<List<DetalleRespuestaIntentoResponse>>(emptyList())
}

    LaunchedEffect(intentoId) {
        try {
            cargando = true
            error = null

            val response = RetrofitInstance.api.obtenerDetalleIntento(intentoId)
            val responseRespuestas = RetrofitInstance.api.obtenerRespuestasDeIntento(intentoId)

            if (response.isSuccessful) {
                intento = response.body()
            } else {
                error = "No se pudo cargar el detalle del intento."
            }
            if (responseRespuestas.isSuccessful) {
                respuestas = responseRespuestas.body().orEmpty()
            }
        } catch (e: Exception) {
            error = "Ocurrió un error al cargar el detalle."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Detalle del intento",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                cargando -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text(
                        text = error ?: "Error desconocido",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                intento == null -> {
                    Text(
                        text = "No existe información para este intento.",
                        color = Color.Gray
                    )
                }

                else -> {
                    DetalleIntentoContent(
                        intento = intento!!,
                        respuestas = respuestas
                    )
                }
            }
        }
    }
}

@Composable
private fun DetalleIntentoContent(
    intento: HistorialIntentoResponse,
    respuestas: List<DetalleRespuestaIntentoResponse>
) {
    val porcentaje = intento.nota.roundToInt()
    val colorEstado = colorEstadoIntento(intento.estado)

    Column(
    modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = intento.tipo.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )

                Text(
                    text = formatoFechaIntento(intento.fecha),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$porcentaje%",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101828)
                        )

                        Text(
                            text = "Nota obtenida",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(colorEstado.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = textoEstadoIntento(intento.estado),
                            color = colorEstado,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { (porcentaje / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = colorEstado,
                    trackColor = Color(0xFFE5E7EB)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = intento.mensaje,
                    fontSize = 13.sp,
                    color = Color(0xFF475467),
                    lineHeight = 17.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Resumen de respuestas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )

                Spacer(modifier = Modifier.height(14.dp))

                DetalleDatoRow("Total de preguntas", intento.totalPreguntas.toString())
                DetalleDatoRow("Aciertos", intento.respuestasCorrectas.toString())
                DetalleDatoRow("Errores", intento.respuestasIncorrectas.toString())
                DetalleDatoRow("Código de banco", intento.bancoId.take(8))
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Preguntas respondidas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
                Text(
                    text = "Total registradas: ${respuestas.size}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(12.dp))

                if (respuestas.isEmpty()) {
                    Text(
                        text = "Este intento no tiene detalle de preguntas registrado.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                } else {
                    respuestas.forEachIndexed { index, respuesta ->
                        PreguntaRespondidaItem(respuesta)

                        if (index != respuestas.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetalleDatoRow(
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = titulo,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Text(
            text = valor,
            color = Color(0xFF101828),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PreguntaRespondidaItem(
    respuesta: DetalleRespuestaIntentoResponse
) {
    val colorEstado = if (respuesta.esCorrecta) {
        Color(0xFF16A34A)
    } else {
        Color(0xFFFF6D00)
    }

    val textoEstado = if (respuesta.esCorrecta) {
        "Correcta"
    } else {
        "Incorrecta"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Pregunta ${respuesta.orden}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF101828)
            )

            Text(
                text = textoEstado,
                color = colorEstado,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Área: ${nombreCategoriaDetalleVisible(respuesta.categoria)}",
            color = Color(0xFF007AFF),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = respuesta.enunciado,
            color = Color(0xFF101828),
            fontSize = 13.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu respuesta: ${respuesta.respuestaSeleccionada}",
            color = if (respuesta.esCorrecta) Color(0xFF16A34A) else Color(0xFFFF6D00),
            fontSize = 12.sp
        )

        if (!respuesta.esCorrecta) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Respuesta correcta: ${respuesta.respuestaCorrecta}",
                color = Color(0xFF16A34A),
                fontSize = 12.sp
            )
        }
    }
}

private fun colorEstadoIntento(estado: String): Color {
    return when (estado) {
        "ALTO" -> Color(0xFF16A34A)
        "MEDIO" -> Color(0xFF007AFF)
        else -> Color(0xFFFF6D00)
    }
}

private fun textoEstadoIntento(estado: String): String {
    return when (estado) {
        "ALTO" -> "Buen rendimiento"
        "MEDIO" -> "En proceso"
        else -> "Debe mejorar"
    }
}

private fun formatoFechaIntento(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "Sin fecha registrada"

    val partes = fecha.split("T")
    if (partes.size < 2) return fecha

    val fechaPartes = partes[0].split("-")
    val hora = partes[1].take(5)

    if (fechaPartes.size < 3) return fecha

    return "${fechaPartes[2]}/${fechaPartes[1]}/${fechaPartes[0]} - $hora"
}

private fun nombreCategoriaDetalleVisible(nombre: String): String {
    return when (nombre.trim().lowercase()) {
        "matematicas", "matemáticas" -> "Matemáticas"
        "fisica", "física" -> "Física"
        "quimica", "química" -> "Química"
        "biologia", "biología" -> "Biología"
        else -> nombre
    }
}