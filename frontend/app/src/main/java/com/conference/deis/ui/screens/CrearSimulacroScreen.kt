package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.CrearSimulacroRequest
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSimulacroScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    val preguntasSeleccionadas = remember { mutableStateListOf<String>() }
    var cargandoPreguntas by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            cargandoPreguntas = true
            val response = RetrofitInstance.api.obtenerPreguntas()
            if (response.isSuccessful) {
                preguntas = response.body().orEmpty()
            } else {
                Toast.makeText(context, "No se pudieron cargar las preguntas", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cargandoPreguntas = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crear simulacro") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Datos del simulacro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del simulacro") },
                    placeholder = { Text("Ej: Simulacro general #1") },
                    enabled = !guardando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha") },
                    placeholder = { Text("YYYY-MM-DD") },
                    enabled = !guardando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Inicio") },
                        placeholder = { Text("14:00") },
                        enabled = !guardando,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Fin") },
                        placeholder = { Text("14:30") },
                        enabled = !guardando,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Selecciona preguntas (${preguntasSeleccionadas.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            if (cargandoPreguntas) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando preguntas...")
                    }
                }
            } else if (preguntas.isEmpty()) {
                item {
                    Text(
                        text = "No hay preguntas registradas. Primero crea preguntas para poder armar un simulacro.",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(preguntas) { pregunta ->
                    PreguntaSeleccionableCard(
                        pregunta = pregunta,
                        seleccionada = pregunta.id in preguntasSeleccionadas,
                        enabled = !guardando,
                        onToggle = {
                            if (pregunta.id in preguntasSeleccionadas) {
                                preguntasSeleccionadas.remove(pregunta.id)
                            } else {
                                preguntasSeleccionadas.add(pregunta.id)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val nombreLimpio = nombre.trim()
                        val fechaLimpia = fecha.trim()
                        val inicioLimpio = horaInicio.trim()
                        val finLimpio = horaFin.trim()

                        when {
                            nombreLimpio.isBlank() -> Toast.makeText(context, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
                            !fechaLimpia.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> Toast.makeText(context, "Use fecha con formato YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                            !inicioLimpio.matches(Regex("\\d{2}:\\d{2}")) -> Toast.makeText(context, "Use hora de inicio HH:mm", Toast.LENGTH_SHORT).show()
                            !finLimpio.matches(Regex("\\d{2}:\\d{2}")) -> Toast.makeText(context, "Use hora final HH:mm", Toast.LENGTH_SHORT).show()
                            preguntasSeleccionadas.isEmpty() -> Toast.makeText(context, "Seleccione al menos una pregunta", Toast.LENGTH_SHORT).show()
                            else -> {
                                scope.launch {
                                    guardando = true
                                    try {
                                        val request = CrearSimulacroRequest(
                                            nombre = nombreLimpio,
                                            fechaInicio = "${fechaLimpia}T${inicioLimpio}:00",
                                            fechaFin = "${fechaLimpia}T${finLimpio}:00",
                                            preguntaIds = preguntasSeleccionadas.toList(),
                                            creadoPor = UserSession.user?.id?.toString(),
                                            zonaHorariaCreador = TimeZone.getDefault().id
                                        )

                                        val response = RetrofitInstance.api.crearSimulacro(request)
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Simulacro creado", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "No se pudo crear el simulacro", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        guardando = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar simulacro")
                }
            }
        }
    }
}

@Composable
private fun PreguntaSeleccionableCard(
    pregunta: Question,
    seleccionada: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = seleccionada,
                onCheckedChange = { onToggle() },
                enabled = enabled
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pregunta.enunciado,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${pregunta.categoria.nombre} · ${pregunta.dificultad}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
