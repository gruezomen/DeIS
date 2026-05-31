package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.CrearSimulacroRequest
import com.conference.deis.network.model.Facultad
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSimulacroScreen(navController: NavHostController) {
    if (!esAdministrador()) {
        AccesoDenegadoScreen(
            navController = navController,
            mensaje = "Solo el administrador puede crear simulacros."
        )
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var tiempoLimiteMinutos by remember { mutableStateOf("") }

    var mostrarCalendario by remember { mutableStateOf(false) }
    var mostrarSelectorInicio by remember { mutableStateOf(false) }
    var mostrarSelectorFin by remember { mutableStateOf(false) }

    var facultades by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var facultadSeleccionada by remember { mutableStateOf<Facultad?>(null) }
    var facultadExpandida by remember { mutableStateOf(false) }

    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    val preguntasSeleccionadas = remember { mutableStateListOf<String>() }

    var cargandoDatos by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    val inicioDiaActualMillis = remember { obtenerInicioDiaActualMillis() }

    if (mostrarCalendario) {
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = convertirFechaIsoAMillis(fecha)
                ?.takeIf { it >= inicioDiaActualMillis }
                ?: inicioDiaActualMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= inicioDiaActualMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fechaSeleccionada = estadoCalendario.selectedDateMillis

                        if (fechaSeleccionada != null && fechaSeleccionada >= inicioDiaActualMillis) {
                            fecha = convertirMillisAFechaIso(fechaSeleccionada)
                            val minimaInicio = calcularHoraMinimaInicio(fecha)

                            if (minimaInicio != null && esHoraMenorQue(horaInicio, minimaInicio)) {
                                horaInicio = minimaInicio
                            }

                            val limite = tiempoLimiteMinutos.trim().toIntOrNull()
                            val minimaFin = calcularHoraMinimaFin(fecha, horaInicio, limite)
                            if (minimaFin != null && esHoraMenorQue(horaFin, minimaFin)) {
                                horaFin = minimaFin
                            }

                            mostrarCalendario = false
                        } else {
                            Toast.makeText(
                                context,
                                "No puede seleccionar una fecha anterior",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }

    if (mostrarSelectorInicio) {
        SelectorHoraDialog(
            titulo = "Hora de inicio",
            horaActual = horaInicio,
            horaMinima = calcularHoraMinimaInicio(fecha),
            mensajeAyuda = "No se permiten horas anteriores a la actual cuando la fecha es hoy.",
            onCancelar = { mostrarSelectorInicio = false },
            onAceptar = { horaElegida ->
                horaInicio = horaElegida

                val limite = tiempoLimiteMinutos.trim().toIntOrNull()
                val minimaFin = calcularHoraMinimaFin(fecha, horaInicio, limite)
                if (minimaFin != null && esHoraMenorQue(horaFin, minimaFin)) {
                    horaFin = minimaFin
                }

                mostrarSelectorInicio = false
            }
        )
    }

    if (mostrarSelectorFin) {
        val limite = tiempoLimiteMinutos.trim().toIntOrNull()
        val horaMinimaFin = calcularHoraMinimaFin(fecha, horaInicio, limite)

        SelectorHoraDialog(
            titulo = "Hora final",
            horaActual = horaFin,
            horaMinima = horaMinimaFin,
            mensajeAyuda = if (horaMinimaFin != null) {
                "La hora final debe permitir al menos el tiempo límite configurado."
            } else {
                "Seleccione primero la fecha, hora de inicio y tiempo límite."
            },
            onCancelar = { mostrarSelectorFin = false },
            onAceptar = { horaElegida ->
                horaFin = horaElegida
                mostrarSelectorFin = false
            }
        )
    }

    LaunchedEffect(Unit) {
        try {
            cargandoDatos = true

            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()
            if (responsePreguntas.isSuccessful) {
                preguntas = responsePreguntas.body().orEmpty()
            } else {
                Toast.makeText(
                    context,
                    "No se pudieron cargar las preguntas",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val responseFacultades = RetrofitInstance.api.obtenerFacultades()
            if (responseFacultades.isSuccessful) {
                facultades = responseFacultades.body().orEmpty()
            } else {
                Toast.makeText(
                    context,
                    "No se pudieron cargar las facultades",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error de conexión: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            cargandoDatos = false
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
                ExposedDropdownMenuBox(
                    expanded = facultadExpandida,
                    onExpandedChange = {
                        if (!guardando && !cargandoDatos) {
                            facultadExpandida = !facultadExpandida
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = facultadSeleccionada?.nombre.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = !guardando && !cargandoDatos,
                        label = { Text("Facultad") },
                        placeholder = { Text("Selecciona la facultad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = facultadExpandida
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = facultadExpandida,
                        onDismissRequest = { facultadExpandida = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        facultades.forEach { facultad ->
                            DropdownMenuItem(
                                text = { Text(facultad.nombre) },
                                onClick = {
                                    facultadSeleccionada = facultad
                                    facultadExpandida = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    placeholder = { Text("Selecciona una fecha") },
                    readOnly = true,
                    enabled = !guardando,
                    singleLine = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { mostrarCalendario = true },
                            enabled = !guardando
                        ) {
                            Text("Elegir")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !guardando) {
                            mostrarCalendario = true
                        }
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = {},
                        label = { Text("Inicio") },
                        placeholder = { Text("14:00") },
                        readOnly = true,
                        enabled = !guardando,
                        singleLine = true,
                        trailingIcon = {
                            TextButton(
                                onClick = { mostrarSelectorInicio = true },
                                enabled = !guardando
                            ) {
                                Text("Elegir")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !guardando) {
                                mostrarSelectorInicio = true
                            }
                    )

                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = {},
                        label = { Text("Fin") },
                        placeholder = { Text("14:30") },
                        readOnly = true,
                        enabled = !guardando,
                        singleLine = true,
                        trailingIcon = {
                            TextButton(
                                onClick = { mostrarSelectorFin = true },
                                enabled = !guardando
                            ) {
                                Text("Elegir")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !guardando) {
                                mostrarSelectorFin = true
                            }
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = tiempoLimiteMinutos,
                    onValueChange = { nuevoValor ->
                        tiempoLimiteMinutos = nuevoValor.filter { it.isDigit() }

                        val limite = nuevoValor.filter { it.isDigit() }.toIntOrNull()
                        val minimaFin = calcularHoraMinimaFin(fecha, horaInicio, limite)
                        if (minimaFin != null && esHoraMenorQue(horaFin, minimaFin)) {
                            horaFin = minimaFin
                        }
                    },
                    label = { Text("Tiempo límite de la prueba") },
                    placeholder = { Text("Ej: 20") },
                    supportingText = {
                        Text("Tiempo máximo para responder, en minutos.")
                    },
                    enabled = !guardando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Selecciona preguntas (${preguntasSeleccionadas.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            if (cargandoDatos) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando datos...")
                    }
                }
            } else if (facultades.isEmpty()) {
                item {
                    Text(
                        text = "No hay facultades registradas. Primero registra facultades en el sistema.",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        val fechaSeleccionadaGuardado = parsearFechaIso(fechaLimpia)
                        val inicioFechaHora = parsearFechaHora(fechaLimpia, inicioLimpio)
                        val finFechaHora = parsearFechaHora(fechaLimpia, finLimpio)
                        val limiteMinutos = tiempoLimiteMinutos.trim().toIntOrNull()
                        val facultad = facultadSeleccionada
                        val facultadId = facultad?.id?.trim().orEmpty()
                        val facultadNombre = facultad?.nombre?.trim().orEmpty()

                        when {
                            nombreLimpio.isBlank() -> Toast.makeText(
                                context,
                                "Ingrese un nombre",
                                Toast.LENGTH_SHORT
                            ).show()

                            facultad == null || facultadId.isBlank() -> Toast.makeText(
                                context,
                                "Seleccione una facultad válida",
                                Toast.LENGTH_SHORT
                            ).show()

                            fechaLimpia.isBlank() -> Toast.makeText(
                                context,
                                "Seleccione una fecha",
                                Toast.LENGTH_SHORT
                            ).show()

                            fechaSeleccionadaGuardado == null -> Toast.makeText(
                                context,
                                "Seleccione una fecha válida",
                                Toast.LENGTH_SHORT
                            ).show()

                            fechaSeleccionadaGuardado.isBefore(LocalDate.now()) -> Toast.makeText(
                                context,
                                "No puede seleccionar una fecha anterior",
                                Toast.LENGTH_SHORT
                            ).show()

                            inicioLimpio.isBlank() -> Toast.makeText(
                                context,
                                "Seleccione hora de inicio",
                                Toast.LENGTH_SHORT
                            ).show()

                            finLimpio.isBlank() -> Toast.makeText(
                                context,
                                "Seleccione hora final",
                                Toast.LENGTH_SHORT
                            ).show()

                            inicioFechaHora == null -> Toast.makeText(
                                context,
                                "Seleccione una hora de inicio válida",
                                Toast.LENGTH_SHORT
                            ).show()

                            finFechaHora == null -> Toast.makeText(
                                context,
                                "Seleccione una hora final válida",
                                Toast.LENGTH_SHORT
                            ).show()

                            inicioFechaHora.isBefore(LocalDateTime.now()) -> Toast.makeText(
                                context,
                                "La hora de inicio no puede ser anterior a la hora actual",
                                Toast.LENGTH_SHORT
                            ).show()

                            limiteMinutos == null || limiteMinutos <= 0 -> Toast.makeText(
                                context,
                                "Ingrese un tiempo límite mayor a cero",
                                Toast.LENGTH_SHORT
                            ).show()

                            !finFechaHora.isAfter(inicioFechaHora) -> Toast.makeText(
                                context,
                                "La hora final debe ser posterior a la hora de inicio",
                                Toast.LENGTH_SHORT
                            ).show()

                            Duration.between(inicioFechaHora, finFechaHora).toMinutes() < limiteMinutos -> Toast.makeText(
                                context,
                                "La hora final debe permitir al menos $limiteMinutos minutos de prueba",
                                Toast.LENGTH_SHORT
                            ).show()

                            preguntasSeleccionadas.isEmpty() -> Toast.makeText(
                                context,
                                "Seleccione al menos una pregunta",
                                Toast.LENGTH_SHORT
                            ).show()

                            else -> {
                                scope.launch {
                                    guardando = true

                                    try {
                                        val request = CrearSimulacroRequest(
                                            nombre = nombreLimpio,
                                            fechaInicio = "${fechaLimpia}T${inicioLimpio}:00",
                                            fechaFin = "${fechaLimpia}T${finLimpio}:00",
                                            tiempoLimiteMinutos = limiteMinutos,
                                            preguntaIds = preguntasSeleccionadas.toList(),
                                            facultadId = facultadId,
                                            facultadNombre = facultadNombre,
                                            creadoPor = UserSession.user?.id?.toString(),
                                            zonaHorariaCreador = TimeZone.getDefault().id
                                        )

                                        val response = RetrofitInstance.api.crearSimulacro(request)

                                        if (response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Simulacro creado",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No se pudo crear el simulacro",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error de conexión: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        guardando = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !guardando && !cargandoDatos,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground
                    )
                ) {
                    Text(
                        text = if (guardando) "Guardando..." else "Guardar simulacro"
                    )
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7F7F7)
        )
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

@Composable
private fun SelectorHoraDialog(
    titulo: String,
    horaActual: String,
    horaMinima: String?,
    mensajeAyuda: String,
    onCancelar: () -> Unit,
    onAceptar: (String) -> Unit
) {
    val horaInicial = obtenerHoraMinutoConMinimo(horaActual, horaMinima)
    val horaMinimaPar = obtenerHoraMinutoONull(horaMinima)

    var horaSeleccionada by remember(titulo, horaActual, horaMinima) {
        mutableStateOf(horaInicial.first)
    }
    var minutoSeleccionado by remember(titulo, horaActual, horaMinima) {
        mutableStateOf(horaInicial.second)
    }

    val horasDisponibles = remember(horaMinima) {
        val inicio = horaMinimaPar?.first ?: 0
        (inicio..23).toList()
    }

    val minutosDisponibles = remember(horaSeleccionada, horaMinima) {
        val minutoInicio = if (horaMinimaPar != null && horaSeleccionada == horaMinimaPar.first) {
            horaMinimaPar.second
        } else {
            0
        }

        (minutoInicio..59).toList()
    }

    if (minutoSeleccionado !in minutosDisponibles) {
        minutoSeleccionado = minutosDisponibles.firstOrNull() ?: 0
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mensajeAyuda,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = String.format(Locale.US, "%02d : %02d", horaSeleccionada, minutoSeleccionado),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ColumnaSelectorNumero(
                        titulo = "Hora",
                        valores = horasDisponibles,
                        valorSeleccionado = horaSeleccionada,
                        onSeleccionar = { nuevaHora ->
                            horaSeleccionada = nuevaHora

                            if (minutoSeleccionado !in minutosDisponibles) {
                                minutoSeleccionado = minutosDisponibles.firstOrNull() ?: 0
                            }
                        }
                    )

                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    ColumnaSelectorNumero(
                        titulo = "Min",
                        valores = minutosDisponibles,
                        valorSeleccionado = minutoSeleccionado,
                        onSeleccionar = { minutoSeleccionado = it }
                    )

                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAceptar(formatearHora(horaSeleccionada, minutoSeleccionado))
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ColumnaSelectorNumero(
    titulo: String,
    valores: List<Int>,
    valorSeleccionado: Int,
    onSeleccionar: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .width(82.dp)
                .height(168.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(valores) { valor ->
                    val seleccionado = valor == valorSeleccionado

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onSeleccionar(valor) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%02d", valor),
                            fontSize = if (seleccionado) 26.sp else 20.sp,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (seleccionado) Color.White else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

private fun obtenerInicioDiaActualMillis(): Long {
    return LocalDate
        .now()
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}

private fun parsearFechaIso(fecha: String): LocalDate? {
    return try {
        LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        null
    }
}

private fun parsearHora(hora: String?): LocalTime? {
    if (hora.isNullOrBlank()) return null

    return try {
        LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        null
    }
}

private fun parsearFechaHora(fecha: String, hora: String): LocalDateTime? {
    val fechaParseada = parsearFechaIso(fecha)
    val horaParseada = parsearHora(hora)

    if (fechaParseada == null || horaParseada == null) return null

    return LocalDateTime.of(fechaParseada, horaParseada)
}

private fun convertirMillisAFechaIso(millis: Long): String {
    val fecha = Instant
        .ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

    return fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

private fun convertirFechaIsoAMillis(fecha: String): Long? {
    return try {
        LocalDate
            .parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    } catch (_: Exception) {
        null
    }
}

private fun obtenerHoraMinutoONull(hora: String?): Pair<Int, Int>? {
    val horaParseada = parsearHora(hora) ?: return null
    return horaParseada.hour to horaParseada.minute
}

private fun obtenerHoraMinutoConMinimo(hora: String, horaMinima: String?): Pair<Int, Int> {
    val horaBase = parsearHora(hora)
    val minima = parsearHora(horaMinima)

    val resultado = when {
        horaBase == null && minima == null -> LocalTime.of(14, 0)
        horaBase == null -> minima!!
        minima == null -> horaBase
        horaBase.isBefore(minima) -> minima
        else -> horaBase
    }

    return resultado.hour to resultado.minute
}

private fun calcularHoraMinimaInicio(fecha: String): String? {
    val fechaParseada = parsearFechaIso(fecha) ?: return null

    if (!fechaParseada.isEqual(LocalDate.now())) {
        return null
    }

    val ahora = LocalTime.now()
    return formatearHora(ahora.hour, ahora.minute)
}

private fun calcularHoraMinimaFin(
    fecha: String,
    horaInicio: String,
    limiteMinutos: Int?
): String? {
    val inicioFechaHora = parsearFechaHora(fecha, horaInicio)
    val fechaParseada = parsearFechaIso(fecha)

    val minimaPorLimite = if (inicioFechaHora != null && limiteMinutos != null && limiteMinutos > 0) {
        inicioFechaHora.plusMinutes(limiteMinutos.toLong())
    } else {
        null
    }

    val minimaPorAhora = if (fechaParseada != null && fechaParseada.isEqual(LocalDate.now())) {
        LocalDateTime.now()
    } else {
        null
    }

    val minimaFinal = listOfNotNull(minimaPorLimite, minimaPorAhora).maxOrNull() ?: return null

    if (fechaParseada != null && minimaFinal.toLocalDate().isAfter(fechaParseada)) {
        return "23:59"
    }

    return formatearHora(minimaFinal.hour, minimaFinal.minute)
}

private fun esHoraMenorQue(hora: String, horaMinima: String?): Boolean {
    val horaParseada = parsearHora(hora) ?: return true
    val minimaParseada = parsearHora(horaMinima) ?: return false
    return horaParseada.isBefore(minimaParseada)
}

private fun formatearHora(hora: Int, minuto: Int): String {
    return String.format(
        Locale.US,
        "%02d:%02d",
        hora.coerceIn(0, 23),
        minuto.coerceIn(0, 59)
    )
}
