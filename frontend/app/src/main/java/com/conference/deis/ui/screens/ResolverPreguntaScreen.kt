package com.conference.deis.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.IntentoSimulacro
import com.conference.deis.network.model.Option
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

private data class EstadoPregunta(
    val preguntaId: String,
    val opcionSeleccionadaIndex: Int?,
    val respondida: Boolean = false,
    val esCorrecta: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolverPreguntaScreen(
    navController: NavHostController,
    bancoId: String? = null
) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var preguntaActualIndex by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var opcionSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }

    val historialEstados = remember { mutableStateMapOf<String, EstadoPregunta>() }

    var practicaFinalizada by remember { mutableStateOf(false) }
    var puntuacion by remember { mutableStateOf(0) }
    var preguntaRevisionIndex by remember { mutableStateOf(0) }
    var mostrarConfirmacionFinalizar by remember { mutableStateOf(false) }

    var guardandoResultado by remember { mutableStateOf(false) }
    var errorGuardado by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun guardarEstadoActual(opcionSeleccionada: Int? = opcionSeleccionadaIndex) {
        val preguntaId = preguntas.getOrNull(preguntaActualIndex)?.id ?: return
        val estadoPrevio = historialEstados[preguntaId]

        historialEstados[preguntaId] = EstadoPregunta(
            preguntaId = preguntaId,
            opcionSeleccionadaIndex = opcionSeleccionada,
            respondida = estadoPrevio?.respondida ?: false,
            esCorrecta = estadoPrevio?.esCorrecta
        )
    }

    fun cargarEstadoPregunta(index: Int) {
        val preguntaId = preguntas.getOrNull(index)?.id ?: return
        val estadoGuardado = historialEstados[preguntaId]
        opcionSeleccionadaIndex = estadoGuardado?.opcionSeleccionadaIndex
    }

    fun limpiarPractica() {
        historialEstados.clear()
        preguntaActualIndex = 0
        preguntaRevisionIndex = 0
        opcionSeleccionadaIndex = null
        practicaFinalizada = false
        puntuacion = 0
        mostrarConfirmacionFinalizar = false
        guardandoResultado = false
        errorGuardado = false
    }

    fun intentarGuardarEnBackend(puntajeFinal: Int) {
        scope.launch {
            guardandoResultado = true
            errorGuardado = false

            try {
                if (!hayConexionInternet(context)) {
                    errorGuardado = true
                    return@launch
                }

                val usuarioId = UserSession.user?.id ?: "usuario_anonimo"
                val bancoIdFinal = bancoId ?: "practica_general"

                val response = RetrofitInstance.api.guardarIntentoSimulacro(
                    IntentoSimulacro(
                        usuarioId = usuarioId,
                        bancoId = bancoIdFinal,
                        puntaje = puntajeFinal,
                        totalPreguntas = preguntas.size
                    )
                )

                if (!response.isSuccessful) {
                    errorGuardado = true
                }
            } catch (e: Exception) {
                errorGuardado = true
            } finally {
                guardandoResultado = false
            }
        }
    }

    fun finalizarPracticaConfirmada() {
        mostrarConfirmacionFinalizar = false
        guardarEstadoActual()

        var correctas = 0

        preguntas.forEach { pregunta ->
            val estadoPrevio = historialEstados[pregunta.id]
            val seleccion = estadoPrevio?.opcionSeleccionadaIndex
            val esCorrecta = seleccion?.let { index ->
                pregunta.opciones.getOrNull(index)?.esCorrecta == true
            }

            if (esCorrecta == true) {
                correctas++
            }

            historialEstados[pregunta.id] = EstadoPregunta(
                preguntaId = pregunta.id,
                opcionSeleccionadaIndex = seleccion,
                respondida = seleccion != null,
                esCorrecta = esCorrecta
            )
        }

        puntuacion = correctas
        preguntaRevisionIndex = 0
        practicaFinalizada = true
        intentarGuardarEnBackend(correctas)
    }

    fun contarPreguntasSinResponder(): Int {
        val preguntaActualId = preguntas.getOrNull(preguntaActualIndex)?.id

        return preguntas.count { pregunta ->
            val seleccion = if (pregunta.id == preguntaActualId) {
                opcionSeleccionadaIndex
            } else {
                historialEstados[pregunta.id]?.opcionSeleccionadaIndex
            }

            seleccion == null
        }
    }

    LaunchedEffect(bancoId) {
        try {
            cargando = true
            limpiarPractica()

            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()

            if (responsePreguntas.isSuccessful) {
                val todas = responsePreguntas.body().orEmpty()

                preguntas = if (bancoId != null) {
                    val responseBanco = RetrofitInstance.api.obtenerBancoPorId(bancoId)

                    if (responseBanco.isSuccessful) {
                        val preguntaIds = responseBanco.body()?.preguntaIds ?: emptyList()
                        todas.filter { pregunta -> pregunta.id in preguntaIds }
                    } else {
                        Toast.makeText(
                            context,
                            "Error al cargar el banco",
                            Toast.LENGTH_SHORT
                        ).show()
                        emptyList()
                    }
                } else {
                    todas
                }
            } else {
                Toast.makeText(
                    context,
                    "Error al cargar las preguntas",
                    Toast.LENGTH_SHORT
                ).show()
                preguntas = emptyList()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error de conexión",
                Toast.LENGTH_SHORT
            ).show()
            preguntas = emptyList()
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (bancoId != null) {
                            "Práctica de Banco"
                        } else {
                            "Práctica General"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (mostrarConfirmacionFinalizar) {
            val preguntasSinResponder = contarPreguntasSinResponder()
            val mensajeAlerta = if (preguntasSinResponder > 0) {
                "Tienes $preguntasSinResponder pregunta(s) sin responder. ¿Estás seguro de que deseas finalizar?"
            } else {
                "¿Estás seguro de que deseas terminar? Se calculará tu puntuación final."
            }

            AlertDialog(
                onDismissRequest = { mostrarConfirmacionFinalizar = false },
                title = { Text("¿Finalizar práctica?") },
                text = { Text(mensajeAlerta) },
                confirmButton = {
                    Button(
                        onClick = { finalizarPracticaConfirmada() },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacionFinalizar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                preguntas.isEmpty() -> {
                    PracticaSinPreguntas(
                        modifier = Modifier.align(Alignment.Center),
                        onVolver = { navController.popBackStack() }
                    )
                }

                practicaFinalizada -> {
                    ResultadosPanel(
                        puntuacion = puntuacion,
                        total = preguntas.size,
                        preguntas = preguntas,
                        historialEstados = historialEstados,
                        preguntaRevisionIndex = preguntaRevisionIndex,
                        guardando = guardandoResultado,
                        errorGuardado = errorGuardado,
                        onPreguntaRevisionChange = { nuevoIndex ->
                            preguntaRevisionIndex = nuevoIndex.coerceIn(0, preguntas.lastIndex)
                        },
                        onReintentarGuardado = { intentarGuardarEnBackend(puntuacion) },
                        onReintentarPractica = { limpiarPractica() },
                        onSalir = { navController.popBackStack() }
                    )
                }

                else -> {
                    val preguntaActual = preguntas[preguntaActualIndex]
                    val estadoActual = historialEstados[preguntaActual.id]

                    PreguntaPracticaContenido(
                        pregunta = preguntaActual,
                        opcionSeleccionadaIndex = opcionSeleccionadaIndex,
                        preguntaNumero = preguntaActualIndex + 1,
                        totalPreguntas = preguntas.size,
                        esRespondida = estadoActual?.respondida ?: false,
                        onSiguientePregunta = {
                            if (preguntaActualIndex < preguntas.lastIndex) {
                                guardarEstadoActual()
                                preguntaActualIndex++
                                cargarEstadoPregunta(preguntaActualIndex)
                            }
                        },
                        onAnteriorPregunta = {
                            if (preguntaActualIndex > 0) {
                                guardarEstadoActual()
                                preguntaActualIndex--
                                cargarEstadoPregunta(preguntaActualIndex)
                            }
                        },
                        onOpcionSeleccionada = { index ->
                            if (!(estadoActual?.respondida ?: false)) {
                                opcionSeleccionadaIndex = index
                                guardarEstadoActual(index)
                            }
                        },
                        onFinalizar = {
                            mostrarConfirmacionFinalizar = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreguntaPracticaContenido(
    pregunta: Question,
    opcionSeleccionadaIndex: Int?,
    preguntaNumero: Int,
    totalPreguntas: Int,
    esRespondida: Boolean,
    onSiguientePregunta: () -> Unit,
    onAnteriorPregunta: () -> Unit,
    onOpcionSeleccionada: (Int) -> Unit,
    onFinalizar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAnteriorPregunta,
                enabled = preguntaNumero > 1
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Anterior",
                    tint = if (preguntaNumero > 1) BlueBackground else Color.Gray
                )
            }

            Text(
                text = "Pregunta $preguntaNumero de $totalPreguntas",
                fontSize = 18.sp,
                color = BlueBackground,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onSiguientePregunta,
                enabled = preguntaNumero < totalPreguntas
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Siguiente",
                    tint = if (preguntaNumero < totalPreguntas) BlueBackground else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = pregunta.categoria.nombre,
                            fontSize = 12.sp,
                            color = BlueBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pregunta.enunciado,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dificultad: ${pregunta.dificultad}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Opciones",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (pregunta.opciones.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = FieldBackground)
                    ) {
                        Text(
                            text = "Esta pregunta no tiene opciones registradas. Puedes finalizarla como pregunta sin responder.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(pregunta.opciones) { index, opcion ->
                    OpcionSimpleItem(
                        index = index,
                        opcion = opcion,
                        seleccionada = opcionSeleccionadaIndex == index,
                        habilitada = !esRespondida,
                        onClick = { onOpcionSeleccionada(index) }
                    )
                }
            }
        }

        if (preguntaNumero == totalPreguntas) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFinalizar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
            ) {
                Text("Finalizar y calificar")
            }
        }
    }
}

@Composable
private fun OpcionSimpleItem(
    index: Int,
    opcion: Option,
    seleccionada: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val letra = ('A'.code + index).toChar()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitada) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) {
                BlueBackground.copy(alpha = 0.1f)
            } else {
                Color.White
            }
        ),
        border = if (seleccionada) {
            BorderStroke(2.dp, BlueBackground)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (seleccionada) 5.dp else 2.dp)
    ) {
        Text(
            text = "$letra. ${opcion.texto}",
            fontSize = 15.sp,
            color = if (seleccionada) BlueBackground else Color.Black,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun ResultadosPanel(
    puntuacion: Int,
    total: Int,
    preguntas: List<Question>,
    historialEstados: Map<String, EstadoPregunta>,
    preguntaRevisionIndex: Int,
    guardando: Boolean,
    errorGuardado: Boolean,
    onPreguntaRevisionChange: (Int) -> Unit,
    onReintentarGuardado: () -> Unit,
    onReintentarPractica: () -> Unit,
    onSalir: () -> Unit
) {
    val porcentaje = if (total == 0) 0 else (puntuacion * 100) / total
    val sinResponder = preguntas.count { pregunta ->
        historialEstados[pregunta.id]?.opcionSeleccionadaIndex == null
    }
    val incorrectas = (total - puntuacion - sinResponder).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "¡Práctica terminada!",
                fontSize = 24.sp,
                color = BlueBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FieldBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tu puntuación",
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "$puntuacion / $total",
                        fontSize = 42.sp,
                        color = BlueBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Nota: $porcentaje/100",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }

        item {
            ResultadoFila(titulo = "Respuestas correctas", valor = puntuacion.toString())
        }

        item {
            ResultadoFila(titulo = "Respuestas incorrectas", valor = incorrectas.toString())
        }

        item {
            ResultadoFila(titulo = "Preguntas sin responder", valor = sinResponder.toString())
        }

        item {
            EstadoGuardadoResultado(
                guardando = guardando,
                errorGuardado = errorGuardado,
                onReintentarGuardado = onReintentarGuardado
            )
        }

        item {
            RevisionPreguntaCard(
                preguntas = preguntas,
                historialEstados = historialEstados,
                preguntaRevisionIndex = preguntaRevisionIndex,
                onPreguntaRevisionChange = onPreguntaRevisionChange
            )
        }

        item {
            Button(
                onClick = onReintentarPractica,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBackground,
                    contentColor = Color.White
                )
            ) {
                Text("Reintentar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSalir,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir")
            }
        }
    }
}

@Composable
private fun EstadoGuardadoResultado(
    guardando: Boolean,
    errorGuardado: Boolean,
    onReintentarGuardado: () -> Unit
) {
    when {
        guardando -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FieldBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BlueBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardando nota...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        errorGuardado -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "!",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No se pudo guardar tu nota.",
                            fontSize = 13.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Revisa tu conexión e intenta de nuevo.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onReintentarGuardado) {
                        Text(
                            text = "Reintentar",
                            color = BlueBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        else -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Nota guardada en tu perfil",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RevisionPreguntaCard(
    preguntas: List<Question>,
    historialEstados: Map<String, EstadoPregunta>,
    preguntaRevisionIndex: Int,
    onPreguntaRevisionChange: (Int) -> Unit
) {
    val preguntaRevision = preguntas.getOrNull(preguntaRevisionIndex)
    val estadoRevision = preguntaRevision?.let { pregunta -> historialEstados[pregunta.id] }
    val opcionElegida = estadoRevision?.opcionSeleccionadaIndex?.let { index ->
        preguntaRevision?.opciones?.getOrNull(index)
    }
    val opcionCorrecta = preguntaRevision?.opciones?.firstOrNull { it.esCorrecta }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Revisión de respuestas",
                color = BlueBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (preguntaRevision == null) {
                Text(
                    text = "No se pudo cargar la resolución de esta pregunta.",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Pregunta ${preguntaRevisionIndex + 1} de ${preguntas.size}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Text(
                    text = preguntaRevision.categoria.nombre,
                    color = BlueBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = preguntaRevision.enunciado,
                    color = Color.Black,
                    fontSize = 15.sp
                )

                val estadoTexto = when {
                    estadoRevision?.opcionSeleccionadaIndex == null -> "Estado: Sin responder"
                    estadoRevision.esCorrecta == true -> "Estado: Respondida correctamente"
                    estadoRevision.esCorrecta == false -> "Estado: Respondida incorrectamente"
                    else -> "Estado: Respondida"
                }

                val estadoColor = when {
                    estadoRevision?.opcionSeleccionadaIndex == null -> Color.Red
                    estadoRevision.esCorrecta == true -> Color(0xFF2E7D32)
                    estadoRevision.esCorrecta == false -> Color.Red
                    else -> BlueBackground
                }

                Text(
                    text = estadoTexto,
                    color = estadoColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Tu respuesta: ${opcionElegida?.texto ?: "Sin responder"}",
                    color = if (opcionElegida == null) Color.Red else Color.Black,
                    fontSize = 14.sp
                )

                Text(
                    text = "Respuesta correcta: ${opcionCorrecta?.texto ?: "No disponible"}",
                    color = if (opcionCorrecta == null) Color.Red else Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (preguntaRevision.opciones.isEmpty()) {
                    Text(
                        text = "No se pudieron cargar las opciones para esta resolución.",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Opciones revisadas",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    preguntaRevision.opciones.forEachIndexed { index, opcion ->
                        OpcionRevisionItem(
                            index = index,
                            opcion = opcion,
                            seleccionada = estadoRevision?.opcionSeleccionadaIndex == index
                        )
                    }
                }

                Text(
                    text = "Explicación",
                    color = BlueBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (preguntaRevision.solucion.isBlank()) {
                        "Esta pregunta no tiene explicación registrada."
                    } else {
                        preguntaRevision.solucion
                    },
                    color = if (preguntaRevision.solucion.isBlank()) Color.Gray else Color.Black,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onPreguntaRevisionChange(preguntaRevisionIndex - 1) },
                        enabled = preguntaRevisionIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anterior")
                    }

                    Button(
                        onClick = { onPreguntaRevisionChange(preguntaRevisionIndex + 1) },
                        enabled = preguntaRevisionIndex < preguntas.lastIndex,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueBackground,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Siguiente")
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcionRevisionItem(
    index: Int,
    opcion: Option,
    seleccionada: Boolean
) {
    val letra = ('A'.code + index).toChar()
    val esCorrecta = opcion.esCorrecta

    val containerColor = when {
        esCorrecta -> Color(0xFFE8F5E9)
        seleccionada -> Color(0xFFFFEBEE)
        else -> Color.White
    }

    val border = when {
        esCorrecta -> BorderStroke(2.dp, Color(0xFF2E7D32))
        seleccionada -> BorderStroke(2.dp, Color.Red)
        else -> null
    }

    val etiqueta = when {
        esCorrecta && seleccionada -> "  ✓ Correcta / Tu respuesta"
        esCorrecta -> "  ✓ Correcta"
        seleccionada -> "  ✗ Tu respuesta"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = "$letra. ${opcion.texto}$etiqueta",
            fontSize = 14.sp,
            color = when {
                esCorrecta -> Color(0xFF2E7D32)
                seleccionada -> Color.Red
                else -> Color.Black
            },
            modifier = Modifier.padding(12.dp),
            fontWeight = if (esCorrecta || seleccionada) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ResultadoFila(
    titulo: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                color = Color.Black,
                fontSize = 15.sp
            )

            Text(
                text = valor,
                color = BlueBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PracticaSinPreguntas(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay preguntas disponibles",
            color = Color.Gray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No se puede iniciar la práctica porque no hay preguntas registradas.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(
                containerColor = BlueBackground,
                contentColor = Color.White
            )
        ) {
            Text("Volver")
        }
    }
}

private fun hayConexionInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
