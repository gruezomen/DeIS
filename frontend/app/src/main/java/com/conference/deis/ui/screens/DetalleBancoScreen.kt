package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.conference.deis.network.model.BancoPregunta
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBancoScreen(
    navController: NavHostController,
    bancoId: String
) {
    var banco by remember { mutableStateOf<BancoPregunta?>(null) }
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val context = LocalContext.current

    LaunchedEffect(bancoId) {
        try {
            val bancoResponse = RetrofitInstance.api.obtenerBancoPreguntaPorId(bancoId)

            if (bancoResponse.isSuccessful) {
                banco = bancoResponse.body()
            } else {
                Toast.makeText(context, "No se pudo cargar el banco", Toast.LENGTH_SHORT).show()
            }

            val preguntasResponse = RetrofitInstance.api.obtenerPreguntasDelBanco(bancoId)

            if (preguntasResponse.isSuccessful) {
                preguntas = preguntasResponse.body() ?: emptyList()
            } else {
                Toast.makeText(context, "No se pudieron cargar las preguntas del banco", Toast.LENGTH_SHORT).show()
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
                title = { Text("Detalle del banco") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Atrás", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = { },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { },
                    label = { Text("Simulacro") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("lista_bancos") },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
        }
    ) { paddingValues ->
        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            banco == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Banco no encontrado", color = Color.Gray)
                }
            }

            else -> {
                val bancoActual = banco!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = FieldBackground)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Información del banco",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "ID: ${bancoActual.id}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )

                                Text(
                                    text = "Facultad: ${bancoActual.facultadId}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Text(
                                    text = "Administrador: ${bancoActual.administradorId}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Text(
                                    text = "Total de preguntas: ${preguntas.size}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Preguntas del banco",
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }

                    if (preguntas.isEmpty()) {
                        item {
                            Text(
                                text = "Este banco todavía no tiene preguntas asociadas",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        items(preguntas) { pregunta ->
                            CardPreguntaDelBanco(pregunta = pregunta)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardPreguntaDelBanco(pregunta: Question) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = pregunta.enunciado,
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Categoría: ${pregunta.categoria.nombre}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Dificultad: ${pregunta.dificultad}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Solución: ${pregunta.solucion}",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}