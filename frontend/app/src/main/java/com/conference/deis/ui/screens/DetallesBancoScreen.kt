package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallesBancoScreen(navController: NavHostController, bancoId: String) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(bancoId) {
        scope.launch {
            cargando = true
            try {
                // 1. Obtener los detalles del banco para saber qué preguntas tiene
                val responseBanco = RetrofitInstance.api.obtenerBancoPorId(bancoId)
                
                if (responseBanco.isSuccessful) {
                    val banco = responseBanco.body()
                    val preguntaIds = banco?.preguntaIds ?: emptyList()

                    // 2. Obtener todas las preguntas y filtrar
                    val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()
                    if (responsePreguntas.isSuccessful) {
                        val todas = responsePreguntas.body() ?: emptyList()
                        // Filtramos para mostrar solo las que pertenecen a este banco
                        preguntas = todas.filter { it.id in preguntaIds }
                    } else {
                        Toast.makeText(context, "Error al cargar preguntas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error al obtener detalles del banco", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Preguntas del Banco") },
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
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (preguntas.isEmpty()) {
                Text(
                    text = "Este banco no tiene preguntas asociadas",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(preguntas) { pregunta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = FieldBackground)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = pregunta.enunciado,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Categoría: ${pregunta.categoria.nombre}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
