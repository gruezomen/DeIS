package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.model.CreateQuestionRequest
import com.conference.deis.network.model.Question
import com.conference.deis.ui.components.BotonCategoria
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizarPreguntaScreen(
    navController: NavHostController,
    preguntaId: String
) {
    var pregunta by remember { mutableStateOf<Question?>(null) }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var cargandoDatos by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(preguntaId) {
        try {
            val response = RetrofitInstance.api.obtenerPreguntaPorId(preguntaId)

            if (response.isSuccessful && response.body() != null) {
                val preguntaCargada = response.body()!!
                pregunta = preguntaCargada
                categoriaSeleccionada = preguntaCargada.categoria.nombre
            } else {
                Toast.makeText(context, "No se pudo cargar la pregunta", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
        } finally {
            cargandoDatos = false
        }
    }

    fun guardarCategoria() {
        val preguntaActual = pregunta ?: return

        if (categoriaSeleccionada.isBlank()) {
            Toast.makeText(context, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            guardando = true

            try {
                val indiceCorrecta = preguntaActual.opciones.indexOfFirst { it.esCorrecta }

                val request = CreateQuestionRequest(
                    enunciado = preguntaActual.enunciado,
                    solucion = preguntaActual.solucion,
                    dificultad = preguntaActual.dificultad,
                    categoria = categoriaSeleccionada,
                    opciones = preguntaActual.opciones.map { it.texto },
                    indiceCorrecta = indiceCorrecta
                )

                val response = RetrofitInstance.api.actualizarPregunta(preguntaId, request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Categoría asignada correctamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "No se pudo asignar la categoría", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al guardar la organización", Toast.LENGTH_SHORT).show()
            } finally {
                guardando = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DelIS", color = Color.White) },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.delfin),
                        contentDescription = "Logo Delfín",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(32.dp)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .background(Color(0xFFE6E6E6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE6E6E6)) {
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
                    onClick = { navController.navigate("lista_preguntas") },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
        }
    ) { paddingValues ->
        if (cargandoDatos) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val preguntaActual = pregunta

            if (preguntaActual == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pregunta no encontrada", color = Color.Gray)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Organizar pregunta",
                        fontSize = 22.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pregunta seleccionada",
                                fontSize = 14.sp,
                                color = BlueBackground
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = preguntaActual.enunciado,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Categoría actual: ${preguntaActual.categoria.nombre}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Asignar categoría",
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BotonCategoria(
                            texto = "Matematicas",
                            seleccionado = categoriaSeleccionada == "Matematicas",
                            onClick = { categoriaSeleccionada = "Matematicas" },
                            modifier = Modifier.weight(1f)
                        )

                        BotonCategoria(
                            texto = "Fisica",
                            seleccionado = categoriaSeleccionada == "Fisica",
                            onClick = { categoriaSeleccionada = "Fisica" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BotonCategoria(
                            texto = "Quimica",
                            seleccionado = categoriaSeleccionada == "Quimica",
                            onClick = { categoriaSeleccionada = "Quimica" },
                            modifier = Modifier.weight(1f)
                        )

                        BotonCategoria(
                            texto = "Biologia",
                            seleccionado = categoriaSeleccionada == "Biologia",
                            onClick = { categoriaSeleccionada = "Biologia" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { guardarCategoria() },
                        enabled = !guardando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar categoría", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}