package com.conference.deis.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.conference.deis.network.model.Option
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class RespuestaPractica(
    val preguntaId: String,
    val enunciado: String,
    val opcionSeleccionada: String,
    val esCorrecta: Boolean
)

private data class ResultadoPractica(
    val totalPreguntas: Int,
    val respuestasRegistradas: Int,
    val correctas: Int,
    val incorrectas: Int,
    val sinResponder: Int,
    val nota: Int
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

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var respuestaCorrecta by remember { mutableStateOf<Boolean?>(null) }
    var enviandoRespuesta by remember { mutableStateOf(false) }
    var calculandoNota by remember { mutableStateOf(false) }
    var errorCalculoNota by remember { mutableStateOf<String?>(null) }
    var practicaFinalizada by remember { mutableStateOf(false) }
    var resultadoPractica by remember { mutableStateOf<ResultadoPractica?>(null) }

    val respuestasPractica = remember { mutableStateListOf<RespuestaPractica>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun limpiarEstadoPractica() {
        preguntaActualIndex = 0
        opcionSeleccionadaIndex = null
        mensajeValidacion = null
        respuestaEnviada = false
        respuestaCorrecta = null
        enviandoRespuesta = false
        calculandoNota = false
        errorCalculoNota = null
        resultadoPractica = null
        practicaFinalizada = false
        respuestasPractica.clear()
    }

    fun limpiarEstadoPregunta() {
        opcionSeleccionadaIndex = null
        mensajeValidacion = null
        respuestaEnviada = false
        respuestaCorrecta = null
        enviandoRespuesta = false
    }

    fun iniciarCalculoNota() {
        if (preguntas.isEmpty()) {
            practicaFinalizada = false
            resultadoPractica = null
            errorCalculoNota = "No se puede calcular la nota porque no hay preguntas disponibles."
            return
        }

        if (preguntaActualIndex != preguntas.lastIndex) {
            practicaFinalizada = false
            resultadoPractica = null
            errorCalculoNota = null
            mensajeValidacion = "Debes finalizar la práctica antes de calcular la nota."
            return
        }

        if (!calculandoNota) {
            calculandoNota = true
            errorCalculoNota = null
            resultadoPractica = null
            practicaFinalizada = false

            scope.launch {
                try {
                    delay(1200)

                    resultadoPractica = calcularResultadoPractica(
                        totalPreguntas = preguntas.size,
                        respuestas = respuestasPractica
                    )

                    calculandoNota = false
                    practicaFinalizada = true
                } catch (e: Exception) {
                    calculandoNota = false
                    practicaFinalizada = false
                    resultadoPractica = null
                    errorCalculoNota = "No se pudo calcular la nota. Intenta nuevamente."
                }
            }
        }
    }

    LaunchedEffect(bancoId) {
        try {
            cargando = true

            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()

            if (responsePreguntas.isSuccessful) {
                val todas = responsePreguntas.body().orEmpty()

                preguntas = if (bancoId != null) {
                    val responseBanco = RetrofitInstance.api.obtenerBancoPorId(bancoId)

                    if (responseBanco.isSuccessful) {
                        val preguntaIds = responseBanco.body()?.preguntaIds ?: emptyList()

                        todas.filter { pregunta ->
                            pregunta.id?.let { id -> id in preguntaIds } == true
                        }
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

                limpiarEstadoPractica()
            } else {
                Toast.makeText(
                    context,
                    "Error al cargar las preguntas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error de conexión",
                Toast.LENGTH_SHORT
            ).show()
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
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                calculandoNota -> {
                    CalculandoNotaContenido(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorCalculoNota != null -> {
                    ErrorCalculoNotaContenido(
                        mensaje = errorCalculoNota.orEmpty(),
                        modifier = Modifier.align(Alignment.Center),
                        onReintentar = {
                            iniciarCalculoNota()
                        },
                        onVolver = {
                            navController.popBackStack()
                        }
                    )
                }

                preguntas.isEmpty() -> {
                    PracticaSinPreguntas(
                        modifier = Modifier.align(Alignment.Center),
                        onVolver = { navController.popBackStack() }
                    )
                }

                practicaFinalizada && resultadoPractica != null -> {
                    ResultadoPracticaContenido(
                        resultado = resultadoPractica!!,
                        onReintentar = {
                            limpiarEstadoPractica()
                        },
                        onVolver = {
                            navController.popBackStack()
                        }
                    )
                }

                else -> {
                    val preguntaActual = preguntas[preguntaActualIndex]

                    PreguntaPracticaContenido(
                        pregunta = preguntaActual,
                        opcionSeleccionadaIndex = opcionSeleccionadaIndex,
                        mensajeValidacion = mensajeValidacion,
                        respuestaEnviada = respuestaEnviada,
                        respuestaCorrecta = respuestaCorrecta,
                        enviandoRespuesta = enviandoRespuesta,
                        cantidadRespuestasRegistradas = respuestasPractica.size,
                        preguntaNumero = preguntaActualIndex + 1,
                        totalPreguntas = preguntas.size,
                        puedeAvanzar = respuestaEnviada && preguntaActualIndex < preguntas.lastIndex,
                        puedeFinalizar = respuestaEnviada && preguntaActualIndex == preguntas.lastIndex,
                        onSiguientePregunta = {
                            if (preguntaActualIndex < preguntas.lastIndex) {
                                preguntaActualIndex++
                                limpiarEstadoPregunta()
                            }
                        },
                        onFinalizarPractica = {
                            iniciarCalculoNota()
                        },
                        onOpcionSeleccionada = { index ->
                            if (!respuestaEnviada && !enviandoRespuesta) {
                                opcionSeleccionadaIndex = index
                                mensajeValidacion = null
                                respuestaCorrecta = null
                            }
                        },
                        onEnviarRespuesta = {
                            if (!enviandoRespuesta && !respuestaEnviada) {
                                val indiceSeleccionado = opcionSeleccionadaIndex

                                if (preguntaActual.opciones.isEmpty()) {
                                    mensajeValidacion = "Esta pregunta no tiene opciones para responder"
                                    respuestaEnviada = false
                                    respuestaCorrecta = null
                                } else if (indiceSeleccionado == null) {
                                    mensajeValidacion = "Selecciona una opción antes de enviar tu respuesta"
                                    respuestaEnviada = false
                                    respuestaCorrecta = null
                                } else if (!hayConexionInternet(context)) {
                                    mensajeValidacion = "Sin conexión. Revisa tu internet e intenta nuevamente"
                                    respuestaEnviada = false
                                    respuestaCorrecta = null
                                    enviandoRespuesta = false
                                } else {
                                    enviandoRespuesta = true
                                    mensajeValidacion = null

                                    scope.launch {
                                        try {
                                            delay(700)

                                            val opcionSeleccionada =
                                                preguntaActual.opciones.getOrNull(indiceSeleccionado)
                                                    ?: throw IllegalStateException("La opción seleccionada no existe")

                                            val esCorrecta = opcionSeleccionada.esCorrecta

                                            respuestaCorrecta = esCorrecta
                                            respuestaEnviada = true

                                            respuestasPractica.removeAll {
                                                it.preguntaId == preguntaActual.id.orEmpty()
                                            }

                                            respuestasPractica.add(
                                                RespuestaPractica(
                                                    preguntaId = preguntaActual.id.orEmpty(),
                                                    enunciado = preguntaActual.enunciado,
                                                    opcionSeleccionada = opcionSeleccionada.texto,
                                                    esCorrecta = esCorrecta
                                                )
                                            )

                                            mensajeValidacion = if (esCorrecta) {
                                                "Respuesta correcta"
                                            } else {
                                                "Respuesta incorrecta"
                                            }
                                        } catch (e: Exception) {
                                            respuestaCorrecta = null
                                            respuestaEnviada = false
                                            mensajeValidacion =
                                                "No se pudo validar la respuesta. Intenta nuevamente"
                                        } finally {
                                            enviandoRespuesta = false
                                        }
                                    }
                                }
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
    mensajeValidacion: String?,
    respuestaEnviada: Boolean,
    respuestaCorrecta: Boolean?,
    enviandoRespuesta: Boolean,
    cantidadRespuestasRegistradas: Int,
    preguntaNumero: Int,
    totalPreguntas: Int,
    puedeAvanzar: Boolean,
    puedeFinalizar: Boolean,
    onSiguientePregunta: () -> Unit,
    onFinalizarPractica: () -> Unit,
    onOpcionSeleccionada: (Int) -> Unit,
    onEnviarRespuesta: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pregunta $preguntaNumero de $totalPreguntas",
                fontSize = 18.sp,
                color = BlueBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                text = "Opciones disponibles",
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
                        text = "Esta pregunta no tiene opciones registradas. Puedes continuar sin responderla.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            itemsIndexed(pregunta.opciones) { index: Int, opcion: Option ->
                OpcionDisponibleItem(
                    index = index,
                    opcion = opcion,
                    seleccionada = opcionSeleccionadaIndex == index,
                    habilitada = !respuestaEnviada && !enviandoRespuesta,
                    onClick = {
                        onOpcionSeleccionada(index)
                    }
                )
            }
        }

        item {
            if (pregunta.opciones.isNotEmpty()) {
                Button(
                    onClick = onEnviarRespuesta,
                    enabled = !enviandoRespuesta && !respuestaEnviada,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    if (enviandoRespuesta) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Enviar respuesta")
                    }
                }
            }

            if (mensajeValidacion != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = mensajeValidacion,
                    color = when {
                        respuestaEnviada && respuestaCorrecta == true -> BlueBackground
                        respuestaEnviada && respuestaCorrecta == false -> Color.Red
                        else -> Color.Red
                    },
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Progreso de la práctica: $cantidadRespuestasRegistradas de $totalPreguntas",
                color = Color.Gray,
                fontSize = 13.sp
            )

            if (pregunta.opciones.isEmpty() && preguntaNumero < totalPreguntas) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSiguientePregunta,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Siguiente pregunta")
                }
            }

            if (pregunta.opciones.isEmpty() && preguntaNumero == totalPreguntas) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onFinalizarPractica,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Finalizar práctica")
                }
            }

            if (puedeAvanzar) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSiguientePregunta,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Siguiente pregunta")
                }
            }

            if (puedeFinalizar) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onFinalizarPractica,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Finalizar práctica")
                }
            }
        }
    }
}

@Composable
private fun ErrorCalculoNotaContenido(
    mensaje: String,
    modifier: Modifier = Modifier,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error al calcular la nota",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReintentar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBackground,
                    contentColor = Color.White
                )
            ) {
                Text("Intentar nuevamente")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver", color = BlueBackground)
            }
        }
    }
}

@Composable
private fun CalculandoNotaContenido(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = BlueBackground,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Calculando nota...",
                color = BlueBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Estamos revisando tus respuestas.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultadoPracticaContenido(
    resultado: ResultadoPractica,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Práctica finalizada",
                color = BlueBackground,
                fontSize = 22.sp,
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Nota: ${resultado.nota}/100",
                        color = BlueBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Cálculo: ${resultado.correctas} correctas de ${resultado.totalPreguntas} preguntas",
                        color = Color.Black,
                        fontSize = 15.sp
                    )

                    if (resultado.respuestasRegistradas == 0) {
                        Text(
                            text = "No se registraron respuestas. La nota se calcula como 0/100.",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            ResultadoFila(
                titulo = "Respuestas correctas",
                valor = resultado.correctas.toString()
            )
        }

        item {
            ResultadoFila(
                titulo = "Respuestas incorrectas",
                valor = resultado.incorrectas.toString()
            )
        }

        if (resultado.sinResponder > 0) {
            item {
                ResultadoFila(
                    titulo = "Preguntas sin responder",
                    valor = resultado.sinResponder.toString()
                )
            }
        }

        item {
            Button(
                onClick = onReintentar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBackground,
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar nueva práctica")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al menú", color = BlueBackground)
            }
        }
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
            text = "No se puede calcular una nota porque la práctica no tiene preguntas ni respuestas registradas.",
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

@Composable
private fun OpcionDisponibleItem(
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
            containerColor = if (seleccionada) FieldBackground else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (seleccionada) 6.dp else 2.dp
        )
    ) {
        Text(
            text = if (seleccionada) {
                "$letra. ${opcion.texto}  ✓"
            } else {
                "$letra. ${opcion.texto}"
            },
            fontSize = 15.sp,
            color = if (seleccionada) BlueBackground else Color.Black,
            modifier = Modifier.padding(14.dp)
        )
    }
}

private fun calcularResultadoPractica(
    totalPreguntas: Int,
    respuestas: List<RespuestaPractica>
): ResultadoPractica {
    val totalSeguro = totalPreguntas.coerceAtLeast(0)
    val respuestasRegistradas = respuestas.size.coerceAtMost(totalSeguro)
    val correctas = respuestas.count { it.esCorrecta }.coerceAtMost(totalSeguro)
    val incorrectasRespondidas = respuestas.count { !it.esCorrecta }.coerceAtMost(totalSeguro)
    val sinResponder = (totalSeguro - respuestasRegistradas).coerceAtLeast(0)
    val incorrectas = incorrectasRespondidas + sinResponder
    val nota = if (totalSeguro == 0) 0 else (correctas * 100) / totalSeguro

    return ResultadoPractica(
        totalPreguntas = totalSeguro,
        respuestasRegistradas = respuestasRegistradas,
        correctas = correctas,
        incorrectas = incorrectas,
        sinResponder = sinResponder,
        nota = nota
    )
}

private fun hayConexionInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
