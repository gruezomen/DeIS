package com.conference.deis.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer

private const val DURACION_SIMULACRO_SEGUNDOS = 600

private data class EstadoPregunta(
    val preguntaId: String,
    val opcionSeleccionadaIndex: Int?,
    val respondida: Boolean = false,
    val esCorrecta: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolverPreguntaScreen(navController: NavHostController, bancoId: String? = null) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var preguntaActualIndex by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    // Estado de la pregunta actual
    var opcionSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }

    // Registro de estado completo de cada pregunta
    val historialEstados = remember { mutableStateMapOf<String, EstadoPregunta>() }

    // Estado global de finalización
    var practicaFinalizada by remember { mutableStateOf(false) }
    var puntuacion by remember { mutableStateOf(0) }
    var mostrarConfirmacionFinalizar by remember { mutableStateOf(false) }
    var finalizadoPorTiempo by remember { mutableStateOf(false) }
    var tiempoRestanteSegundos by remember(bancoId) { mutableStateOf(DURACION_SIMULACRO_SEGUNDOS) }

    // Estados de guardado en backend
    var guardandoResultado by remember { mutableStateOf(false) }
    var errorGuardado by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun guardarEstadoActual() {
        val preguntaId = preguntas.getOrNull(preguntaActualIndex)?.id ?: return
        val estadoPrevio = historialEstados[preguntaId]
        historialEstados[preguntaId] = EstadoPregunta(
            preguntaId = preguntaId,
            opcionSeleccionadaIndex = opcionSeleccionadaIndex,
            respondida = estadoPrevio?.respondida ?: false,
            esCorrecta = estadoPrevio?.esCorrecta
        )
    }

    fun cargarEstadoPregunta(index: Int) {
        val preguntaId = preguntas.getOrNull(index)?.id ?: return
        val estadoGuardado = historialEstados[preguntaId]
        opcionSeleccionadaIndex = estadoGuardado?.opcionSeleccionadaIndex
    }

    fun intentarGuardarEnBackend(puntajeFinal: Int) {
        scope.launch {
            guardandoResultado = true
            errorGuardado = false
            try {
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

    fun finalizarSimulacro(tiempoTerminado: Boolean) {
        if (practicaFinalizada) return

        mostrarConfirmacionFinalizar = false
        guardarEstadoActual()

        var correctas = 0
        preguntas.forEach { pregunta ->
            val estado = historialEstados[pregunta.id]
            val seleccion = estado?.opcionSeleccionadaIndex

            if (seleccion != null && seleccion in pregunta.opciones.indices) {
                val esOk = pregunta.opciones[seleccion].esCorrecta
                if (esOk) correctas++

                historialEstados[pregunta.id] = (estado ?: EstadoPregunta(pregunta.id, seleccion)).copy(
                    respondida = true,
                    esCorrecta = esOk
                )
            }
        }

        puntuacion = correctas
        finalizadoPorTiempo = tiempoTerminado
        practicaFinalizada = true
        intentarGuardarEnBackend(correctas)
    }

    LaunchedEffect(cargando, preguntas.size, practicaFinalizada) {
        if (!cargando && preguntas.isNotEmpty() && !practicaFinalizada) {
            while (tiempoRestanteSegundos > 0 && !practicaFinalizada) {
                delay(1000)
                tiempoRestanteSegundos--
            }

            if (tiempoRestanteSegundos <= 0 && !practicaFinalizada) {
                tiempoRestanteSegundos = 0
                finalizarSimulacro(tiempoTerminado = true)
            }
        }
    }

    LaunchedEffect(bancoId) {
        try {
            cargando = true
            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()
            if (responsePreguntas.isSuccessful) {
                val todas = responsePreguntas.body().orEmpty()
                if (bancoId != null) {
                    val responseBanco = RetrofitInstance.api.obtenerBancoPorId(bancoId)
                    if (responseBanco.isSuccessful) {
                        val preguntaIds = responseBanco.body()?.preguntaIds ?: emptyList()
                        preguntas = todas.filter { it.id in preguntaIds }
                    }
                } else {
                    preguntas = todas
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (bancoId != null) "Práctica de Banco" else "Práctica General") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            val preguntasSinResponder = preguntas.count { q ->
                historialEstados[q.id]?.opcionSeleccionadaIndex == null
            }

            val mensajeAlerta = if (preguntasSinResponder > 0) {
                "Tienes $preguntasSinResponder pregunta(s) sin responder. ¿Estás seguro de que deseas finalizar?"
            } else {
                "¿Estás seguro de que deseas terminar? Se calculará tu puntuación final."
            }

            AlertDialog(
                onDismissRequest = { mostrarConfirmacionFinalizar = false },
                title = { Text("¿Finalizar Simulacro?") },
                text = { Text(mensajeAlerta) },
                confirmButton = {
                    Button(
                        onClick = { finalizarSimulacro(tiempoTerminado = false) },
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
                    Text(text = "No hay preguntas", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                }

                practicaFinalizada -> {
                    ResultadosPanel(
                        puntuacion = puntuacion,
                        total = preguntas.size,
                        guardando = guardandoResultado,
                        errorGuardado = errorGuardado,
                        finalizadoPorTiempo = finalizadoPorTiempo,
                        onReintentarGuardado = { intentarGuardarEnBackend(puntuacion) },
                        onReintentarPractica = {
                            tiempoRestanteSegundos = DURACION_SIMULACRO_SEGUNDOS
                            finalizadoPorTiempo = false
                            historialEstados.clear()
                            preguntaActualIndex = 0
                            opcionSeleccionadaIndex = null
                            errorGuardado = false
                            practicaFinalizada = false
                        },
                        onSalir = { navController.popBackStack() }
                    )
                }

                else -> {
                    val preguntaActual = preguntas[preguntaActualIndex]
                    val estadoActual = historialEstados[preguntaActual.id]
                    val tiempoAgotado = tiempoRestanteSegundos <= 0

                    PreguntaPracticaContenido(
                        pregunta = preguntaActual,
                        opcionSeleccionadaIndex = opcionSeleccionadaIndex,
                        preguntaNumero = preguntaActualIndex + 1,
                        totalPreguntas = preguntas.size,
                        tiempoRestanteSegundos = tiempoRestanteSegundos,
                        esRespondida = (estadoActual?.respondida ?: false) || practicaFinalizada,
                        tiempoAgotado = tiempoAgotado,
                        onSiguientePregunta = {
                            if (preguntaActualIndex < preguntas.lastIndex) {
                                if (!practicaFinalizada) guardarEstadoActual()
                                preguntaActualIndex++
                                cargarEstadoPregunta(preguntaActualIndex)
                            }
                        },
                        onAnteriorPregunta = {
                            if (preguntaActualIndex > 0) {
                                if (!practicaFinalizada) guardarEstadoActual()
                                preguntaActualIndex--
                                cargarEstadoPregunta(preguntaActualIndex)
                            }
                        },
                        onOpcionSeleccionada = { index ->
                            if (!(estadoActual?.respondida ?: false) && !practicaFinalizada && !tiempoAgotado) {
                                opcionSeleccionadaIndex = index
                                guardarEstadoActual()
                            }
                        },
                        onFinalizar = {
                            if (!tiempoAgotado) {
                                mostrarConfirmacionFinalizar = true
                            }
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
    tiempoRestanteSegundos: Int,
    esRespondida: Boolean,
    tiempoAgotado: Boolean,
    onSiguientePregunta: () -> Unit,
    onAnteriorPregunta: () -> Unit,
    onOpcionSeleccionada: (Int) -> Unit,
    onFinalizar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAnteriorPregunta, enabled = preguntaNumero > 1) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Anterior", tint = if (preguntaNumero > 1) BlueBackground else Color.Gray)
            }
            Text(text = "Pregunta $preguntaNumero de $totalPreguntas", fontSize = 18.sp, color = BlueBackground)
            IconButton(onClick = onSiguientePregunta, enabled = preguntaNumero < totalPreguntas) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Siguiente", tint = if (preguntaNumero < totalPreguntas) BlueBackground else Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TiempoRestanteCard(
            tiempoRestanteSegundos = tiempoRestanteSegundos,
            tiempoAgotado = tiempoAgotado
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = FieldBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = pregunta.categoria.nombre, fontSize = 12.sp, color = BlueBackground)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = pregunta.enunciado, fontSize = 16.sp, color = Color.Black)
                    }
                }
            }
            item { Text(text = "Opciones", fontSize = 16.sp, color = Color.Black) }
            itemsIndexed(pregunta.opciones) { index, opcion ->
                OpcionSimpleItem(
                    index = index,
                    opcion = opcion,
                    seleccionada = opcionSeleccionadaIndex == index,
                    bloqueada = esRespondida || tiempoAgotado,
                    onClick = { onOpcionSeleccionada(index) }
                )
            }
        }

        if (preguntaNumero == totalPreguntas) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFinalizar,
                enabled = !tiempoAgotado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
            ) {
                Text("Finalizar y Calificar")
            }
        }
    }
}

@Composable
private fun TiempoRestanteCard(
    tiempoRestanteSegundos: Int,
    tiempoAgotado: Boolean
) {
    val colorFondo = if (tiempoAgotado || tiempoRestanteSegundos <= 60) Color(0xFFFFEBEE) else FieldBackground
    val colorTexto = if (tiempoAgotado || tiempoRestanteSegundos <= 60) Color.Red else BlueBackground
    val mensaje = if (tiempoAgotado) "Tiempo terminado" else "Tiempo restante"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = mensaje, fontSize = 14.sp, color = colorTexto)
            Text(
                text = formatearTiempo(tiempoRestanteSegundos),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}

@Composable
private fun OpcionSimpleItem(
    index: Int,
    opcion: Option,
    seleccionada: Boolean,
    bloqueada: Boolean,
    onClick: () -> Unit
) {
    val letra = ('A'.code + index).toChar()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !bloqueada) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                seleccionada -> BlueBackground.copy(alpha = 0.1f)
                bloqueada -> Color(0xFFF5F5F5)
                else -> Color.White
            }
        ),
        border = if (seleccionada) androidx.compose.foundation.BorderStroke(2.dp, BlueBackground) else null
    ) {
        Text(
            text = "$letra. ${opcion.texto}",
            fontSize = 15.sp,
            color = if (bloqueada && !seleccionada) Color.Gray else Color.Black,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun ResultadosPanel(
    puntuacion: Int,
    total: Int,
    guardando: Boolean,
    errorGuardado: Boolean,
    finalizadoPorTiempo: Boolean,
    onReintentarGuardado: () -> Unit,
    onReintentarPractica: () -> Unit,
    onSalir: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (finalizadoPorTiempo) "Simulacro finalizado" else "¡Práctica Terminada!",
            fontSize = 24.sp,
            color = BlueBackground,
            textAlign = TextAlign.Center
        )

        if (finalizadoPorTiempo) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "El tiempo terminó. El sistema finalizó el simulacro automáticamente y bloqueó los cambios.",
                    fontSize = 13.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Tu puntuación:", fontSize = 18.sp, color = Color.Black)
        Text(text = "$puntuacion / $total", fontSize = 48.sp, color = BlueBackground)

        Spacer(modifier = Modifier.height(24.dp))

        if (guardando) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Guardando nota...", fontSize = 14.sp, color = Color.Gray)
            }
        } else if (errorGuardado) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "!",
                        fontSize = 20.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "No se pudo guardar tu nota.", fontSize = 13.sp, color = Color.Red)
                        Text(text = "Revisa tu conexión e intenta de nuevo.", fontSize = 12.sp, color = Color.Gray)
                    }
                    TextButton(onClick = onReintentarGuardado) {
                        Text(
                            text = "Reintentar",
                            fontSize = 12.sp,
                            color = BlueBackground
                        )
                    }
                }
            }
        } else {
            Text(text = "✓ Nota guardada en tu perfil", fontSize = 14.sp, color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReintentarPractica, modifier = Modifier.fillMaxWidth()) { Text("Reintentar") }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) { Text("Salir") }
    }
}

private fun formatearTiempo(segundosTotales: Int): String {
    val segundosSeguros = segundosTotales.coerceAtLeast(0)
    val minutos = segundosSeguros / 60
    val segundos = segundosSeguros % 60
    return "%02d:%02d".format(minutos, segundos)
}

private fun hayConexionInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
