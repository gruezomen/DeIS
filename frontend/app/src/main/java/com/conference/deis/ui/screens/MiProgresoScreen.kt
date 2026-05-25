package com.conference.deis.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.ComparacionRendimientoResponse
import com.conference.deis.network.model.IntentoSimulacro
import kotlin.math.roundToInt

@Composable
fun MiProgresoScreen(navController: NavHostController) {
    val usuarioId = UserSession.user?.id?.toString()

    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var comparacion by remember { mutableStateOf<ComparacionRendimientoResponse?>(null) }
    var intentos by remember { mutableStateOf<List<IntentoSimulacro>>(emptyList()) }
    var tabSeleccionado by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (usuarioId == null) {
            error = "No se encontró el usuario en sesión."
            cargando = false
            return@LaunchedEffect
        }

        try {
            cargando = true
            error = null

            val responseComparacion =
                RetrofitInstance.api.obtenerComparacionRendimiento(usuarioId)

            val responseIntentos =
                RetrofitInstance.api.obtenerIntentosPorUsuario(usuarioId)

            if (responseComparacion.isSuccessful) {
                comparacion = responseComparacion.body()
            }

            if (responseIntentos.isSuccessful) {
                intentos = responseIntentos.body().orEmpty()
            }
        } catch (e: Exception) {
            error = "No se pudo cargar el progreso."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = {},
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("lista_bancos/Examen Simulacro") },
                    icon = {},
                    label = { Text("Simulacro") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("lista_bancos") },
                    icon = {},
                    label = { Text("Banco") }
                )
            }
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
                    text = "Mi progreso",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )

                Text(
                    text = "Revisa tu rendimiento en simulacros y prácticas.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            item {
                TabRow(selectedTabIndex = tabSeleccionado) {
                    Tab(
                        selected = tabSeleccionado == 0,
                        onClick = { tabSeleccionado = 0 },
                        text = { Text("Resumen") }
                    )

                    Tab(
                        selected = tabSeleccionado == 1,
                        onClick = { tabSeleccionado = 1 },
                        text = { Text("Estadísticas") }
                    )

                    Tab(
                        selected = tabSeleccionado == 2,
                        onClick = { tabSeleccionado = 2 },
                        text = { Text("Historial") }
                    )
                }
            }

            if (cargando) {
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
            } else if (error != null) {
                item {
                    ProgressCard {
                        Text(
                            text = error ?: "Error desconocido",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                when (tabSeleccionado) {
                    0 -> item { ResumenProgreso(comparacion, intentos) }
                    1 -> item { EstadisticasProgreso(comparacion, intentos) }
                    2 -> item { HistorialProgreso(intentos) }
                }
            }
        }
    }
}

@Composable
private fun ResumenProgreso(
    comparacion: ComparacionRendimientoResponse?,
    intentos: List<IntentoSimulacro>
) {
    val ultimo = comparacion?.ultimoResultado ?: 0.0
    val porcentaje = (ultimo.toFloat() / 100f).coerceIn(0f, 1f)

    ProgressCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { porcentaje },
                    modifier = Modifier.size(110.dp),
                    strokeWidth = 10.dp
                )

                Text(
                    text = "${ultimo.roundToInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = "Resultado reciente",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = comparacion?.mensaje ?: "Sin datos disponibles.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                IndicadorRendimiento(comparacion)
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniResumenCard(
            titulo = "Intentos",
            valor = "${comparacion?.totalIntentos ?: intentos.size}",
            modifier = Modifier.weight(1f)
        )

        MiniResumenCard(
            titulo = "Último",
            valor = "${ultimo.roundToInt()}%",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EstadisticasProgreso(
    comparacion: ComparacionRendimientoResponse?,
    intentos: List<IntentoSimulacro>
) {
    if (intentos.isEmpty()) {
        ProgressCard {
            Text(
                text = "Aún no tienes prácticas registradas.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Completa una práctica o simulacro para ver tus estadísticas generales.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        return
    }

    val totalCorrectas = intentos.sumOf { it.respuestasCorrectas }
    val totalIncorrectas = intentos.sumOf { it.respuestasIncorrectas }
    val totalPreguntas = totalCorrectas + totalIncorrectas
    val practicasCompletadas = intentos.size

    val promedio = if (totalPreguntas > 0) {
        (totalCorrectas.toDouble() / totalPreguntas.toDouble()) * 100
    } else {
        0.0
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Estadísticas generales",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EstadisticaCard(
                titulo = "Total de prácticas completadas",
                valor = "$practicasCompletadas",
                modifier = Modifier.weight(1f)
            )

            EstadisticaCard(
                titulo = "Porcentaje general de rendimiento",
                valor = "${promedio.roundToInt()}%",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EstadisticaCard(
                titulo = "Respuestas correctas",
                valor = "$totalCorrectas",
                modifier = Modifier.weight(1f)
            )

            EstadisticaCard(
                titulo = "Respuestas incorrectas",
                valor = "$totalIncorrectas",
                modifier = Modifier.weight(1f)
            )
        }

        ProgressCard {
            Text(
                text = "Correctas vs Incorrectas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Correctas: $totalCorrectas")
            Text("Incorrectas: $totalIncorrectas")
            Text("Total de respuestas: $totalPreguntas")

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (promedio / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        ProgressCard {
            Text(
                text = "Comparación reciente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Resultado anterior: ${comparacion?.resultadoAnterior?.roundToInt() ?: 0}%")
            Text("Último resultado: ${comparacion?.ultimoResultado?.roundToInt() ?: 0}%")
            Text("Diferencia: ${comparacion?.diferencia ?: 0.0}%")

            Spacer(modifier = Modifier.height(10.dp))

            IndicadorRendimiento(comparacion)
        }
    }
}

@Composable
private fun IndicadorRendimiento(comparacion: ComparacionRendimientoResponse?) {
    val estado = comparacion?.estado ?: "SIN_DATOS"

    val texto = when (estado) {
        "MEJORO" -> "Mejoró"
        "SE_MANTUVO_IGUAL" -> "Sin cambio"
        "DISMINUYO" -> "Disminuyó"
        "SIN_COMPARACION" -> "Sin comparación"
        else -> "Sin datos"
    }

    val color = when (estado) {
        "MEJORO" -> Color(0xFF16A34A)
        "SE_MANTUVO_IGUAL" -> Color(0xFFF59E0B)
        "DISMINUYO" -> Color(0xFFDC2626)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = texto,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun HistorialProgreso(intentos: List<IntentoSimulacro>) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val intentosOrdenados = intentos.reversed()
    val ultimosIntentos = intentosOrdenados.takeLast(5)

    val mejorResultado = ultimosIntentos.maxOfOrNull { calcularPorcentaje(it) } ?: 0.0
    val ultimoResultado = ultimosIntentos.lastOrNull()?.let { calcularPorcentaje(it) } ?: 0.0
    val promedioHistorico = if (ultimosIntentos.isNotEmpty()) {
        ultimosIntentos.map { calcularPorcentaje(it) }.average()
    } else {
        0.0
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistorialResumenCard(
                titulo = "Mejor resultado",
                valor = "${mejorResultado.roundToInt()}%",
                detalle = "Mejor intento",
                modifier = Modifier.weight(1f)
            )

            HistorialResumenCard(
                titulo = "Promedio",
                valor = "${promedioHistorico.roundToInt()}%",
                detalle = "Últimos 5 intentos",
                modifier = Modifier.weight(1f)
            )

            HistorialResumenCard(
                titulo = "Último resultado",
                valor = "${ultimoResultado.roundToInt()}%",
                detalle = "Intento reciente",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FiltroHistorialChip(
                texto = "Todos",
                seleccionado = filtroSeleccionado == "Todos",
                onClick = { filtroSeleccionado = "Todos" }
            )

            FiltroHistorialChip(
                texto = "Simulacros",
                seleccionado = filtroSeleccionado == "Simulacros",
                onClick = { filtroSeleccionado = "Simulacros" }
            )

            FiltroHistorialChip(
                texto = "Prácticas",
                seleccionado = filtroSeleccionado == "Prácticas",
                onClick = { filtroSeleccionado = "Prácticas" }
            )
        }

        ProgressCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Historial de resultados",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Últimos 5 intentos",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GraficoHistorial(ultimosIntentos)
        }

        ProgressCard {
            Text(
                text = "Intentos recientes",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (ultimosIntentos.isEmpty()) {
                Text(
                    text = "Aún no tienes intentos registrados.",
                    color = Color.Gray
                )
            } else {
                ultimosIntentos.reversed().forEachIndexed { index, intento ->
                    HistorialIntentoRow(
                        intento = intento,
                        intentoAnterior = ultimosIntentos.getOrNull(ultimosIntentos.size - index - 2)
                    )

                    if (index != ultimosIntentos.lastIndex) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Ver detalle")
                }
            }
        }
    }
}

@Composable
private fun HistorialResumenCard(
    titulo: String,
    valor: String,
    detalle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = valor,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )

            Text(
                text = detalle,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FiltroHistorialChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val background = if (seleccionado) Color(0xFFE8F2FF) else Color.White
    val textColor = if (seleccionado) Color(0xFF007AFF) else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            color = textColor,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GraficoHistorial(intentos: List<IntentoSimulacro>) {
    val valores = intentos.map { calcularPorcentaje(it).toFloat() }

    if (valores.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos para graficar", color = Color.Gray)
        }
        return
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ancho = size.width
                val alto = size.height
                val margenIzquierdo = 36f
                val margenInferior = 28f
                val margenSuperior = 16f
                val areaAncho = ancho - margenIzquierdo - 8f
                val areaAlto = alto - margenSuperior - margenInferior

                for (i in 0..4) {
                    val y = margenSuperior + (areaAlto / 4f) * i
                    drawLine(
                        color = Color(0xFFE5E7EB),
                        start = Offset(margenIzquierdo, y),
                        end = Offset(ancho, y),
                        strokeWidth = 1.5f
                    )
                }

                val puntos = valores.mapIndexed { index, valor ->
                    val x = margenIzquierdo + if (valores.size == 1) {
                        areaAncho / 2f
                    } else {
                        (areaAncho / (valores.size - 1)) * index
                    }

                    val y = margenSuperior + areaAlto - ((valor / 100f) * areaAlto)

                    Offset(x, y)
                }

                puntos.zipWithNext().forEach { (inicio, fin) ->
                    drawLine(
                        color = Color(0xFF007AFF),
                        start = inicio,
                        end = fin,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                puntos.forEach {
                    drawCircle(
                        color = Color(0xFF007AFF),
                        radius = 7f,
                        center = it
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            valores.forEachIndexed { index, _ ->
                Text(
                    text = "S${index + 1}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun HistorialIntentoRow(
    intento: IntentoSimulacro,
    intentoAnterior: IntentoSimulacro?
) {
    val resultado = calcularPorcentaje(intento)
    val anterior = intentoAnterior?.let { calcularPorcentaje(it) }

    val estado = when {
        anterior == null -> "Sin cambio"
        resultado > anterior -> "Mejoró"
        resultado < anterior -> "Disminuyó"
        else -> "Sin cambio"
    }

    val colorEstado = when (estado) {
        "Mejoró" -> Color(0xFF16A34A)
        "Disminuyó" -> Color(0xFFDC2626)
        else -> Color(0xFF2563EB)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.1f)) {
            Text(
                text = intento.fecha ?: "Sin fecha",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Column(modifier = Modifier.weight(1.3f)) {
            Text(
                text = "Simulacro",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "${resultado.roundToInt()}%",
            modifier = Modifier.weight(0.8f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101828)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(colorEstado.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = estado,
                color = colorEstado,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MiniResumenCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = titulo,
                color = Color.Gray,
                fontSize = 13.sp
            )

            Text(
                text = valor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EstadisticaCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
        }
    }
}

@Composable
private fun ProgressCard(content: @Composable ColumnScope.() -> Unit) {
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

private fun calcularPorcentaje(intento: IntentoSimulacro): Double {
    return if (intento.totalPreguntas > 0) {
        (intento.respuestasCorrectas.toDouble() / intento.totalPreguntas.toDouble()) * 100.0
    } else {
        0.0
    }
}
