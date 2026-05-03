package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPreguntasScreen(navController: NavHostController) {
    var bancos by remember { mutableStateOf<List<BancoPregunta>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.obtenerBancosPreguntas()

            if (response.isSuccessful) {
                bancos = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Error al cargar bancos", Toast.LENGTH_SHORT).show()
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
                title = { Text("Bancos de preguntas") },
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
                    onClick = { },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
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
                            CardBancoPregunta(
                                banco = banco,
                                onClick = {
                                    navController.navigate("detalle_banco/${banco.id}")
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
fun CardBancoPregunta(
    banco: BancoPregunta,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Banco de preguntas",
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Facultad: ${banco.facultadId}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Administrador: ${banco.administradorId}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Preguntas asociadas: ${banco.preguntaIds.size}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ver detalle",
                fontSize = 13.sp,
                color = BlueBackground
            )
        }
    }
}