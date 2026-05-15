package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.model.Option
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateListOf

private data class RespuestaPractica(
    val preguntaId: String,
    val opcionSeleccionadaIndex: Int,
    val opcionSeleccionadaTexto: String,
    val esCorrecta: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolverPreguntaScreen(navController: NavHostController, bancoId: String? = null) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var preguntaActualIndex by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var opcionSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var respuestaCorrecta by remember { mutableStateOf<Boolean?>(null) }
    var enviandoRespuesta by remember { mutableStateOf(false) }
    val respuestasPractica = remember { mutableStateListOf<RespuestaPractica>() }
    var errorResolucion by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    } else {
                        Toast.makeText(context, "Error al cargar el banco", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    preguntas = todas
                }
            } else {
                Toast.makeText(context, "Error al cargar las preguntas", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(preguntas, preguntaActualIndex, respuestasPractica.size) {
        val preguntaActual = preguntas.getOrNull(preguntaActualIndex) ?: return@LaunchedEffect
        val respuestaGuardada = respuestasPractica.firstOrNull { it.preguntaId == preguntaActual.id.orEmpty() }

        if (respuestaGuardada != null) {
            opcionSeleccionadaIndex = respuestaGuardada.opcionSeleccionadaIndex
            respuestaEnviada = true
            respuestaCorrecta = respuestaGuardada.esCorrecta
            mensajeValidacion = if (respuestaGuardada.esCorrecta) {
                "Respuesta correcta"
            } else {
                "Respuesta incorrecta"
            }
        } else {
            opcionSeleccionadaIndex = null
            respuestaEnviada = false
            respuestaCorrecta = null
            mensajeValidacion = null
        }

        enviandoRespuesta = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (bancoId != null) "Práctica de Banco" else "Práctica General") },
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

                preguntas.isEmpty() -> {
                    Text(
                        text = "No hay preguntas disponibles",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
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
                      errorResolucion = errorResolucion,
                      puedeAvanzar = respuestaEnviada && preguntaActualIndex < preguntas.lastIndex,
                      onSiguientePregunta = {
                            if (preguntaActualIndex < preguntas.lastIndex) {
                                preguntaActualIndex++
                            }
                        },
                      onOpcionSeleccionada = { index ->
                        if (!respuestaEnviada && !enviandoRespuesta) {
                             opcionSeleccionadaIndex = index
                             mensajeValidacion = null
                             respuestaCorrecta = null
                             errorResolucion = null
                                                }
                    },
                 onEnviarRespuesta = {
    if (!enviandoRespuesta && !respuestaEnviada) {
        val indiceSeleccionado = opcionSeleccionadaIndex

        if (indiceSeleccionado == null) {
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

                    val opcionSeleccionada = preguntaActual.opciones.getOrNull(indiceSeleccionado)
                        ?: throw IllegalStateException("La opción seleccionada no existe")

                    val esCorrecta = opcionSeleccionada.esCorrecta

                    respuestaCorrecta = esCorrecta
                    respuestaEnviada = true

                    respuestasPractica.removeAll { it.preguntaId == preguntaActual.id.orEmpty() }
                    respuestasPractica.add(
                      RespuestaPractica(
                        preguntaId = preguntaActual.id.orEmpty(),
                        opcionSeleccionadaIndex = indiceSeleccionado,
                        opcionSeleccionadaTexto = opcionSeleccionada.texto,
                        esCorrecta = esCorrecta
                                        )
                        )

                    errorResolucion = null

                    if (preguntaActual.enunciado.isBlank()) {
                        throw IllegalStateException("No se pudo cargar la resolución de la pregunta")
                    }

                    mensajeValidacion = if (esCorrecta) {
                       "Respuesta correcta"
                    } else {
                       "Respuesta incorrecta"
                    }
                } catch (e: Exception) {
                    respuestaCorrecta = null
                    respuestaEnviada = false
                    mensajeValidacion = "No se pudo validar la respuesta. Intenta nuevamente"
                    mensajeValidacion = null
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
    errorResolucion: String?,
    onSiguientePregunta: () -> Unit,
    onOpcionSeleccionada: (Int) -> Unit,
    onEnviarRespuesta: () -> Unit
) {
    val opcionCorrecta = pregunta.opciones.firstOrNull { it.esCorrecta }
    val opcionSeleccionada = opcionSeleccionadaIndex?.let { index ->
        pregunta.opciones.getOrNull(index)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pregunta $preguntaNumero de $totalPreguntas",
                fontSize = 18.sp,
                color = BlueBackground
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
                color = Color.Black
            )
        }

        if (pregunta.opciones.isEmpty()) {
            item {
                Text(
                    text = "Esta pregunta no tiene opciones registradas",
                    color = Color.Gray
                )
            }
        } else {
            itemsIndexed(pregunta.opciones) { index, opcion ->
                OpcionDisponibleItem(
                    index = index,
                    opcion = opcion,
                    seleccionada = opcionSeleccionadaIndex == index,
                    habilitada = !respuestaEnviada && !enviandoRespuesta,
                    mostrarResolucion = respuestaEnviada,
                    onClick = {
                        onOpcionSeleccionada(index)
                    }
                )
            }
        }

        item {
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

            if (respuestaEnviada) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resolución",
                            fontSize = 16.sp,
                            color = BlueBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (respuestaCorrecta == true) {
                                "Tu respuesta fue correcta."
                            } else {
                                "Tu respuesta fue incorrecta."
                            },
                            fontSize = 14.sp,
                            color = if (respuestaCorrecta == true) BlueBackground else Color.Red
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tu respuesta: ${opcionSeleccionada?.texto ?: "No disponible"}",
                            fontSize = 14.sp,
                            color = if (respuestaCorrecta == true) BlueBackground else Color.Red
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Respuesta correcta: ${opcionCorrecta?.texto ?: "No disponible"}",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Explicación",
                            fontSize = 15.sp,
                            color = BlueBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (errorResolucion != null) {
                            Text(
                                text = errorResolucion,
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                        } else if (pregunta.solucion.isBlank()) {
                            Text(
                                text = "Esta pregunta no tiene explicación registrada.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = pregunta.solucion,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            if (cantidadRespuestasRegistradas > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Progreso de la práctica: $cantidadRespuestasRegistradas de $totalPreguntas",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
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

            if (respuestaEnviada && preguntaNumero == totalPreguntas) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Práctica finalizada",
                    color = BlueBackground,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun OpcionDisponibleItem(
    index: Int,
    opcion: Option,
    seleccionada: Boolean,
    habilitada: Boolean,
    mostrarResolucion: Boolean,
    onClick: () -> Unit
) {
    val letra = ('A'.code + index).toChar()
    val verdeCorrecta = Color(0xFF2E7D32)

    val containerColor = when {
        mostrarResolucion && opcion.esCorrecta -> Color(0xFFE8F5E9)
        mostrarResolucion && seleccionada && !opcion.esCorrecta -> Color(0xFFFFDAD6)
        seleccionada -> FieldBackground
        else -> Color.White
    }

    val textColor = when {
        mostrarResolucion && opcion.esCorrecta -> verdeCorrecta
        mostrarResolucion && seleccionada && !opcion.esCorrecta -> Color.Red
        seleccionada -> BlueBackground
        else -> Color.Black
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitada) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (seleccionada) 6.dp else 2.dp
        )
    ) {
        Text(
            text = "$letra. ${opcion.texto}",
            fontSize = 15.sp,
            color = textColor,
            modifier = Modifier.padding(14.dp)
        )
    }
}

private fun hayConexionInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
