package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.model.BancoPregunta
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaBancosScreen(
    navController: NavHostController,
    tituloPersonalizado: String? = null
) {
    var bancos by remember { mutableStateOf<List<BancoPregunta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf(false) }

    var mostrarDialogoTiempo by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var bancoSeleccionadoId by remember { mutableStateOf<String?>(null) }
    var tiempoTexto by remember { mutableStateOf("") }
    var errorTiempo by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarBancos() {
        scope.launch {
            cargando = true
            errorCarga = false

            try {
                val response = RetrofitInstance.api.obtenerBancosPreguntas()

                if (response.isSuccessful) {
                    bancos = response.body() ?: emptyList()
                } else {
                    errorCarga = true
                    Toast.makeText(
                        context,
                        "Error del servidor al cargar bancos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                errorCarga = true
                Toast.makeText(
                    context,
                    "Error de conexión: Verifica tu internet",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                cargando = false
            }
        }
    }

    fun eliminarBanco(id: String) {
        scope.launch {
            try {
                val response = RetrofitInstance.api.eliminarBanco(id)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Banco eliminado", Toast.LENGTH_SHORT).show()
                    cargarBancos()
                } else {
                    Toast.makeText(context, "Error al eliminar el banco", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun abrirDialogoTiempo(banco: BancoPregunta) {
        if (banco.id.isBlank()) {
            Toast.makeText(
                context,
                "No se puede iniciar la práctica porque el banco no tiene ID",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        bancoSeleccionadoId = banco.id
        tiempoTexto = ""
        errorTiempo = null
        mostrarDialogoTiempo = true
    }

    fun confirmarTiempoPractica() {
        val minutos = tiempoTexto.toIntOrNull()

        if (minutos == null || minutos <= 0) {
            errorTiempo = "Ingresa un tiempo válido mayor a 0 minutos"
            return
        }

        val idBanco = bancoSeleccionadoId

        if (idBanco.isNullOrBlank()) {
            errorTiempo = "No se pudo identificar el banco de preguntas"
            return
        }

        mostrarDialogoTiempo = false
        errorTiempo = null
        navController.navigate("resolver_pregunta/$idBanco/$minutos")
    }

    LaunchedEffect(Unit) {
        cargarBancos()
    }

    if (mostrarDialogoTiempo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoTiempo = false
                errorTiempo = null
            },
            title = {
                Text("Configurar simulacro")
            },
            text = {
                Column {
                    Text(
                        text = "¿Cuántos minutos quieres para resolver este banco de preguntas?",
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tiempoTexto,
                        onValueChange = { valor ->
                            tiempoTexto = valor.filter { it.isDigit() }
                            errorTiempo = null
                        },
                        label = { Text("Tiempo en minutos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = errorTiempo != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorTiempo != null) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = errorTiempo.orEmpty(),
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { confirmarTiempoPractica() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Iniciar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoTiempo = false
                        errorTiempo = null
                    }
                ) {
                    Text("Cancelar", color = BlueBackground)
                }
            }
        )
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar Banco") },
            text = { Text("¿Estás seguro de que deseas eliminar este banco de preguntas? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bancoSeleccionadoId?.let { eliminarBanco(it) }
                        mostrarDialogoEliminar = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(tituloPersonalizado ?: "Lista de Banco de Preguntas")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                cargando -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = BlueBackground)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Cargando bancos...",
                            color = Color.Gray
                        )
                    }
                }

                errorCarga -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se pudieron cargar los bancos de preguntas.",
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { cargarBancos() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlueBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Reintentar")
                        }
                    }
                }

                bancos.isEmpty() -> {
                    Text(
                        text = "No hay bancos de preguntas registrados",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bancos) { banco ->
                            CardBanco(
                                banco = banco,
                                mostrarPracticar = tituloPersonalizado == "Examen Simulacro",
                                onDetallesClick = {
                                    navController.navigate("detalles_banco/${banco.id}")
                                },
                                onPracticarClick = {
                                    abrirDialogoTiempo(banco)
                                },
                                onEliminarClick = {
                                    bancoSeleccionadoId = banco.id
                                    mostrarDialogoEliminar = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardBanco(
    banco: BancoPregunta,
    mostrarPracticar: Boolean,
    onDetallesClick: () -> Unit,
    onPracticarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = FieldBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Banco de Preguntas",
                    fontSize = 16.sp,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onEliminarClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Banco",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Facultad: ${banco.facultadId}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Preguntas: ${banco.preguntaIds.size}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetallesClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BlueBackground
                    )
                ) {
                    Text("Detalles", fontSize = 14.sp)
                }

                if (mostrarPracticar) {
                    Button(
                        onClick = onPracticarClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueBackground,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Practicar", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
