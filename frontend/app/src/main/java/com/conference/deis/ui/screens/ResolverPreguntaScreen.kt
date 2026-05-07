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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolverPreguntaScreen(navController: NavHostController) {
    var pregunta by remember { mutableStateOf<Question?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var opcionSeleccionadaIndex by remember { mutableStateOf<Int?>(null) }
    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var respuestaEnviada by remember { mutableStateOf(false) }
    var respuestaCorrecta by remember { mutableStateOf<Boolean?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.obtenerPreguntas()

            if (response.isSuccessful) {
                pregunta = response.body()?.firstOrNull()
            } else {
                Toast.makeText(
                    context,
                    "Error al cargar la pregunta",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se pudo conectar con el servidor",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Práctica") },
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

                pregunta == null -> {
                    Text(
                        text = "No hay preguntas disponibles",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                   val preguntaActual = pregunta!!

                   PreguntaPracticaContenido(
                      pregunta = preguntaActual,
                      opcionSeleccionadaIndex = opcionSeleccionadaIndex,
                      mensajeValidacion = mensajeValidacion,
                      respuestaEnviada = respuestaEnviada,
                      respuestaCorrecta = respuestaCorrecta,
                      onOpcionSeleccionada = { index ->
                        if (!respuestaEnviada) {
                        opcionSeleccionadaIndex = index
                        mensajeValidacion = null
                        respuestaCorrecta = null
                                                }
                    },
                    onEnviarRespuesta = {
                        val indiceSeleccionado = opcionSeleccionadaIndex
                         if (indiceSeleccionado == null) {
                            mensajeValidacion = "Selecciona una opción antes de enviar tu respuesta"
                            respuestaEnviada = false
                            respuestaCorrecta = null
                         } else {
                               val opcionSeleccionada = preguntaActual.opciones[indiceSeleccionado]
                               val esCorrecta = opcionSeleccionada.esCorrecta
                               respuestaCorrecta = opcionSeleccionada.esCorrecta
                               respuestaEnviada = true
                               mensajeValidacion = if (esCorrecta) {
                                                       "Respuesta correcta"
                                                         } else {
                                                          "Respuesta incorrecta"
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
    onOpcionSeleccionada: (Int) -> Unit,
    onEnviarRespuesta: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pregunta 1",
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
                    habilitada = !respuestaEnviada,
                    onClick = {
                        onOpcionSeleccionada(index)
                     }
                )
            }
        }
        item {
            Button(
                onClick = onEnviarRespuesta,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBackground,
                    contentColor = Color.White
                )
            ) {
                Text("Enviar respuesta")
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