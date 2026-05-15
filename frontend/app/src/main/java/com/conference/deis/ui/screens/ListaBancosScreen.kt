package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.conference.deis.network.model.BancoPregunta
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaBancosScreen(navController: NavHostController, tituloPersonalizado: String? = null) {
    var bancos by remember { mutableStateOf<List<BancoPregunta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarBancos() {
        scope.launch {
            cargando = true
            try {
                val response = RetrofitInstance.api.obtenerBancosPreguntas()
                if (response.isSuccessful) {
                    bancos = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar bancos de preguntas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarBancos()
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                                    navController.navigate("resolver_pregunta/${banco.id}")
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
    onPracticarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = FieldBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Banco de Preguntas",
                fontSize = 16.sp,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )

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
