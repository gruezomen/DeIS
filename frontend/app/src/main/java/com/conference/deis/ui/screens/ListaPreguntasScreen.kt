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
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPreguntasScreen(navController: NavHostController) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var eliminando by remember { mutableStateOf(false) }
    var preguntaAEliminar by remember { mutableStateOf<Question?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarPreguntas() {
        scope.launch {
            cargando = true

            try {
                val response = RetrofitInstance.api.obtenerPreguntas()

                if (response.isSuccessful) {
                    preguntas = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar preguntas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    fun confirmarEliminacion(pregunta: Question) {
        scope.launch {
            eliminando = true

            try {
                val response = RetrofitInstance.api.eliminarPregunta(pregunta.id)

                if (response.isSuccessful) {
                    preguntas = preguntas.filterNot { it.id == pregunta.id }
                    preguntaAEliminar = null
                    Toast.makeText(
                        context,
                        "Pregunta eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (response.code() == 404) {
                    preguntas = preguntas.filterNot { it.id == pregunta.id }
                    preguntaAEliminar = null
                    Toast.makeText(
                        context,
                        "La pregunta ya no existe",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "No se pudo eliminar la pregunta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error de conexión al eliminar",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                eliminando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarPreguntas()
    }

    preguntaAEliminar?.let { pregunta ->
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    preguntaAEliminar = null
                }
            },
            title = {
                Text("Confirmar eliminación")
            },
            text = {
                Text("¿Seguro que deseas eliminar esta pregunta? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmarEliminacion(pregunta)
                    },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (eliminando) "Eliminando..." else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        preguntaAEliminar = null
                    },
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
                title = {
                    Text("Lista de Preguntas")
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

                preguntas.isEmpty() -> {
                    Text(
                        text = "No hay preguntas registradas",
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
                        items(preguntas) { pregunta ->
                            CardPregunta(
                                pregunta = pregunta,
                                onEditarClick = {
                                    navController.navigate("editar_pregunta/${pregunta.id}")
                                },
                                onOrganizarClick = {
                                    navController.navigate("organizar_pregunta/${pregunta.id}")
                                },
                                onEliminarClick = {
                                    preguntaAEliminar = pregunta
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
fun CardPregunta(
    pregunta: Question,
    onEditarClick: () -> Unit,
    onOrganizarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEditarClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = FieldBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = pregunta.categoria.nombre,
                fontSize = 12.sp,
                color = BlueBackground,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pregunta.enunciado,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Dificultad: ${pregunta.dificultad}",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOrganizarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Organizar banco", fontSize = 12.sp)
                }

                Button(
                    onClick = onEditarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text("Editar categoría", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onEliminarClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Eliminar", fontSize = 12.sp)
            }
        }
    }
}