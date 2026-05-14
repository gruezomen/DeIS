package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateListOf

private data class RespuestaPractica(
    val preguntaId: String,
    val opcionSeleccionadaIndex: Int,
    val esCorrecta: Boolean,
    val mensaje: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolverPreguntaScreen(navController: NavHostController, bancoId: String? = null) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var preguntaActualIndex by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    
    // Estado de la pregunta actual
    var opcionSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }
    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var respuestaCorrecta by remember { mutableStateOf<Boolean?>(null) }
    var enviandoRespuesta by remember { mutableStateOf(false) }
    
    // Registro de respuestas para persistencia durante la navegación
    val historialRespuestas = remember { mutableStateMapOf<String, RespuestaPractica>() }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Función para cargar el estado de una pregunta desde el historial
    fun cargarEstadoPregunta(index: Int) {
        val preguntaId = preguntas.getOrNull(index)?.id ?: return
        val respuestaGuardada = historialRespuestas[preguntaId]
        
        if (respuestaGuardada != null) {
            opcionSeleccionadaIndex = respuestaGuardada.opcionSeleccionadaIndex
            mensajeValidacion = respuestaGuardada.mensaje
            respuestaEnviada = true
            respuestaCorrecta = respuestaGuardada.esCorrecta
        } else {
            opcionSeleccionadaIndex = null
            mensajeValidacion = null
            respuestaEnviada = false
            respuestaCorrecta = null
        }
        enviandoRespuesta = false
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
                      preguntaNumero = preguntaActualIndex + 1,
                      totalPreguntas = preguntas.size,
                      onSiguientePregunta = {
                          if (preguntaActualIndex < preguntas.lastIndex) {
                              preguntaActualIndex++
                              cargarEstadoPregunta(preguntaActualIndex)
                          }
                      },
                      onAnteriorPregunta = {
                          if (preguntaActualIndex > 0) {
                              preguntaActualIndex--
                              cargarEstadoPregunta(preguntaActualIndex)
                          }
                      },
                      onOpcionSeleccionada = { index ->
                        if (!respuestaEnviada && !enviandoRespuesta) {
                             opcionSeleccionadaIndex = index
                             mensajeValidacion = null
                        }
                    },
                    onEnviarRespuesta = {
                        if (!enviandoRespuesta && !respuestaEnviada) {
                            val indiceSeleccionado = opcionSeleccionadaIndex
                            if (indiceSeleccionado == null) {
                                mensajeValidacion = "Selecciona una opción antes de enviar"
                            } else if (!hayConexionInternet(context)) {
                                mensajeValidacion = "Sin conexión a internet"
                            } else {
                                enviandoRespuesta = true
                                scope.launch {
                                    try {
                                        delay(600)
                                        val opcion = preguntaActual.opciones[indiceSeleccionado]
                                        val esCorrecta = opcion.esCorrecta
                                        
                                        respuestaCorrecta = esCorrecta
                                        respuestaEnviada = true
                                        mensajeValidacion = if (esCorrecta) "¡Correcto!" else "Incorrecto"
                                        
                                        // Guardar en historial
                                        historialRespuestas[preguntaActual.id!!] = RespuestaPractica(
                                            preguntaId = preguntaActual.id!!,
                                            opcionSeleccionadaIndex = indiceSeleccionado,
                                            esCorrecta = esCorrecta,
                                            mensaje = mensajeValidacion!!
                                        )
                                    } catch (e: Exception) {
                                        mensajeValidacion = "Error al validar"
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
    preguntaNumero: Int,
    totalPreguntas: Int,
    onSiguientePregunta: () -> Unit,
    onAnteriorPregunta: () -> Unit,
    onOpcionSeleccionada: (Int) -> Unit,
    onEnviarRespuesta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera con navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAnteriorPregunta,
                enabled = preguntaNumero > 1
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Anterior", tint = if (preguntaNumero > 1) BlueBackground else Color.Gray)
            }
            
            Text(
                text = "Pregunta $preguntaNumero de $totalPreguntas",
                fontSize = 18.sp,
                color = BlueBackground
            )
            
            IconButton(
                onClick = onSiguientePregunta,
                enabled = preguntaNumero < totalPreguntas
            ) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Siguiente", tint = if (preguntaNumero < totalPreguntas) BlueBackground else Color.Gray)
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
                        Text(text = pregunta.categoria.nombre, fontSize = 12.sp, color = BlueBackground)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = pregunta.enunciado, fontSize = 16.sp, color = Color.Black)
                    }
                }
            }

            item {
                Text(text = "Opciones", fontSize = 16.sp, color = Color.Black)
            }

            itemsIndexed(pregunta.opciones) { index, opcion ->
                OpcionDisponibleItem(
                    index = index,
                    opcion = opcion,
                    seleccionada = opcionSeleccionadaIndex == index,
                    habilitada = !respuestaEnviada && !enviandoRespuesta,
                    esCorrecta = if (respuestaEnviada) opcion.esCorrecta else null,
                    onClick = { onOpcionSeleccionada(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje y Botón de envío
        if (mensajeValidacion != null) {
            Text(
                text = mensajeValidacion,
                color = if (respuestaCorrecta == true) BlueBackground else Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onEnviarRespuesta,
            enabled = !enviandoRespuesta && !respuestaEnviada,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
        ) {
            if (enviandoRespuesta) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(if (respuestaEnviada) "Respondida" else "Enviar respuesta")
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
    esCorrecta: Boolean?,
    onClick: () -> Unit
) {
    val letra = ('A'.code + index).toChar()
    val backgroundColor = when {
        esCorrecta == true -> Color(0xFFC8E6C9) // Verde si es la correcta
        seleccionada && esCorrecta == false -> Color(0xFFFFCDD2) // Rojo si seleccionó mal
        seleccionada -> FieldBackground
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitada) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (seleccionada) 4.dp else 1.dp)
    ) {
        Text(
            text = "$letra. ${opcion.texto}",
            fontSize = 15.sp,
            color = Color.Black,
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
