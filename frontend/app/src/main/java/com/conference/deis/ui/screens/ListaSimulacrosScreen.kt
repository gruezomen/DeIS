package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.conference.deis.network.model.HistorialIntentoResponse
import com.conference.deis.network.model.Simulacro
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSimulacrosScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val esAdmin = esAdministrador()
    val usuarioId = UserSession.user?.id?.toString()

    var simulacros by remember { mutableStateOf<List<Simulacro>>(emptyList()) }
    var intentos by remember { mutableStateOf<List<HistorialIntentoResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var eliminando by remember { mutableStateOf(false) }
    var simulacroAEliminar by remember { mutableStateOf<Simulacro?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarDatos() {
        scope.launch {
            try {
                cargando = true
                error = null

                val responseSimulacros = RetrofitInstance.api.obtenerSimulacros()
                if (responseSimulacros.isSuccessful) {
                    simulacros = responseSimulacros.body()
                        .orEmpty()
                        .filter { it.programado && !it.eliminado }
                } else {
                    error = "No se pudieron cargar los simulacros."
                }

                if (!usuarioId.isNullOrBlank()) {
                    val responseIntentos = RetrofitInstance.api.obtenerHistorialDetallado(usuarioId)
                    if (responseIntentos.isSuccessful) {
                        intentos = responseIntentos.body().orEmpty()
                    }
                }
            } catch (e: Exception) {
                error = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    val simulacroSeleccionado = simulacroAEliminar
    if (simulacroSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) simulacroAEliminar = null
            },
            title = { Text("Eliminar simulacro") },
            text = {
                Text(
                    "¿Seguro que deseas eliminar \"${simulacroSeleccionado.nombre}\"? " +
                        "El simulacro dejará de mostrarse, pero los intentos y estadísticas de los preuniversitarios se conservarán."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = simulacroSeleccionado.id
                        if (id.isNullOrBlank()) {
                            Toast.makeText(context, "No se pudo identificar el simulacro", Toast.LENGTH_SHORT).show()
                            simulacroAEliminar = null
                            return@Button
                        }

                        scope.launch {
                            eliminando = true
                            try {
                                val response = RetrofitInstance.api.eliminarSimulacro(id)
                                if (response.isSuccessful) {
                                    simulacros = simulacros.filterNot { it.id == id }
                                    Toast.makeText(context, "Simulacro eliminado", Toast.LENGTH_SHORT).show()
                                    simulacroAEliminar = null
                                } else {
                                    Toast.makeText(context, "No se pudo eliminar el simulacro", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                eliminando = false
                            }
                        }
                    },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (eliminando) "Eliminando..." else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { simulacroAEliminar = null },
                    enabled = !eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Simulacros") },
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
        },
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_simulacro") },
                    containerColor = BlueBackground,
                    contentColor = Color.White
                ) {
                    Text("+")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF4F4F4))
                .padding(16.dp)
        ) {
            if (esAdmin) {
                Button(
                    onClick = { navController.navigate("crear_simulacro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                ) {
                    Text("Crear simulacro")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            when {
                cargando -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando simulacros...")
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { cargarDatos() },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                simulacros.isEmpty() -> {
                    Text(
                        text = "Todavía no hay simulacros programados.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(simulacros) { simulacro ->
                            val intento = intentos.firstOrNull {
                                it.tipo == "SIMULACRO" && it.bancoId == simulacro.id
                            }

                            SimulacroCard(
                                simulacro = simulacro,
                                intento = intento,
                                esAdmin = esAdmin,
                                onResolver = {
                                    val id = simulacro.id
                                    if (id != null) {
                                        navController.navigate("resolver_simulacro/$id")
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "No se pudo identificar el simulacro",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onVerResultado = {
                                    val intentoId = intento?.id
                                    if (intentoId != null) {
                                        navController.navigate("detalle_intento/$intentoId")
                                    }
                                },
                                onEliminar = { simulacroAEliminar = simulacro }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulacroCard(
    simulacro: Simulacro,
    intento: HistorialIntentoResponse?,
    esAdmin: Boolean,
    onResolver: () -> Unit,
    onVerResultado: () -> Unit,
    onEliminar: () -> Unit
) {
    val estado = simulacro.estado.uppercase()
    val colorEstado = when (estado) {
        "ACTIVO" -> Color(0xFF1E88E5)
        "FINALIZADO" -> Color(0xFF757575)
        else -> Color(0xFF8E8E8E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = simulacro.nombre.ifBlank { "Simulacro" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = estado.lowercase().replaceFirstChar { it.uppercase() },
                    color = colorEstado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Fecha: ${formatearFechaSimulacro(simulacro.horaInicio)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Hora de inicio: ${formatearHoraSimulacro(simulacro.horaInicio)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Hora final: ${formatearHoraSimulacro(simulacro.horaFin)}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Preguntas: ${simulacro.totalPreguntas}", fontSize = 14.sp, color = Color.Gray)

            if (intento != null && !esAdmin) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Intento realizado: 1/1",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                esAdmin -> {
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Eliminar simulacro")
                    }
                }

                estado == "ACTIVO" && intento == null -> {
                    Button(
                        onClick = onResolver,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                    ) {
                        Text("Resolver Simulacro")
                    }
                }

                estado == "ACTIVO" && intento != null -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ya respondiste")
                    }
                }

                estado == "FINALIZADO" -> {
                    Button(
                        onClick = onVerResultado,
                        enabled = intento != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
                    ) {
                        Text(if (intento != null) "Ver resultado" else "Finalizado")
                    }
                }

                else -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Aún no disponible")
                    }
                }
            }
        }
    }
}

private fun formatearFechaSimulacro(valor: String?): String {
    if (valor.isNullOrBlank() || valor.length < 10) return "--/--/----"
    val partes = valor.substring(0, 10).split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else valor.substring(0, 10)
}

private fun formatearHoraSimulacro(valor: String?): String {
    if (valor.isNullOrBlank() || valor.length < 16) return "--:--"
    return valor.substring(11, 16)
}
