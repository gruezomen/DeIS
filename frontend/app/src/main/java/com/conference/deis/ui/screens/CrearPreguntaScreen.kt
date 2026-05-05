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
import com.conference.deis.ui.components.*
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPreguntaScreen(
    navController: NavHostController,
    preguntaId: String? = null
) {
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var enunciado by remember { mutableStateOf("") }
    var opcionA by remember { mutableStateOf("") }
    var opcionB by remember { mutableStateOf("") }
    var opcionC by remember { mutableStateOf("") }
    var opcionD by remember { mutableStateOf("") }
    var explicacion by remember { mutableStateOf("") }
    var dificultadSeleccionada by remember { mutableStateOf("") }
    var indiceCorrecta by remember { mutableStateOf(-1) }
    var cargando by remember { mutableStateOf(false) }
    var cargandoDatos by remember { mutableStateOf(preguntaId != null) }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    val esEdicion = preguntaId != null
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Función para ejecutar la actualización/creación
    val ejecutarAccion = {
        scope.launch {
            cargando = true
            try {
                val request = CreateQuestionRequest(
                    enunciado = enunciado.trim(),
                    solucion = explicacion.trim(),
                    dificultad = dificultadSeleccionada,
                    categoria = categoriaSeleccionada,
                    opciones = listOf(
                        opcionA.trim(),
                        opcionB.trim(),
                        opcionC.trim(),
                        opcionD.trim()
                    ),
                    indiceCorrecta = indiceCorrecta
                )

                val response = if (esEdicion) {
                    RetrofitInstance.api.actualizarPregunta(preguntaId!!, request)
                } else {
                    RetrofitInstance.api.crearPregunta(request)
                }

                if (response.isSuccessful) {
                    val mensaje = if (esEdicion) "Pregunta actualizada" else "Pregunta creada"
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

                    if (esEdicion) {
                        navController.popBackStack()
                    } else {
                        categoriaSeleccionada = ""
                        dificultadSeleccionada = ""
                        enunciado = ""
                        opcionA = ""
                        opcionB = ""
                        opcionC = ""
                        opcionD = ""
                        explicacion = ""
                        indiceCorrecta = -1
                    }
                } else {
                    Toast.makeText(context, "Error en el servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar actualización") },
            text = { Text("¿Estás seguro de que deseas guardar los cambios realizados en esta pregunta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        ejecutarAccion()
                    }
                ) {
                    Text("Confirmar", color = BlueBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray
        )
    }

    // Cargar datos si es edición
    LaunchedEffect(preguntaId) {
        if (preguntaId != null) {
            try {
                val response = RetrofitInstance.api.obtenerPreguntaPorId(preguntaId)
                if (response.isSuccessful && response.body() != null) {
                    val p = response.body()!!
                    categoriaSeleccionada = p.categoria.nombre
                    enunciado = p.enunciado
                    explicacion = p.solucion
                    dificultadSeleccionada = p.dificultad
                    
                    if (p.opciones.size >= 4) {
                        opcionA = p.opciones[0].texto
                        opcionB = p.opciones[1].texto
                        opcionC = p.opciones[2].texto
                        opcionD = p.opciones[3].texto
                        indiceCorrecta = p.opciones.indexOfFirst { it.esCorrecta }
                    }
                } else {
                    Toast.makeText(context, "No se pudo cargar la pregunta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al conectar para cargar datos", Toast.LENGTH_SHORT).show()
            } finally {
                cargandoDatos = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DelIS") },
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
                    selected = false,
                    onClick = { },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
        }
    ) { paddingValues ->
        if (cargandoDatos) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(if (esEdicion) "Editar Pregunta" else "Crear Pregunta", fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Categoria", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

                Text("Dificultad", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotonDificultad(
                        texto = "Facil",
                        seleccionado = dificultadSeleccionada == "FACIL",
                        onClick = { dificultadSeleccionada = "FACIL" },
                        modifier = Modifier.weight(1f)
                    )
                    BotonDificultad(
                        texto = "Medio",
                        seleccionado = dificultadSeleccionada == "MEDIO",
                        onClick = { dificultadSeleccionada = "MEDIO" },
                        modifier = Modifier.weight(1f)
                    )
                    BotonDificultad(
                        texto = "Dificil",
                        seleccionado = dificultadSeleccionada == "DIFICIL",
                        onClick = { dificultadSeleccionada = "DIFICIL" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Enunciado", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                CampoGris(
                    valor = enunciado,
                    placeholder = "Escribe el enunciado",
                    onValueChange = { enunciado = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Opciones (marca la correcta)", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                OpcionEditable(
                    texto = opcionA,
                    seleccionada = indiceCorrecta == 0,
                    onTextoChange = { opcionA = it },
                    onClick = { indiceCorrecta = 0 }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OpcionEditable(
                    texto = opcionB,
                    seleccionada = indiceCorrecta == 1,
                    onTextoChange = { opcionB = it },
                    onClick = { indiceCorrecta = 1 }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OpcionEditable(
                    texto = opcionC,
                    seleccionada = indiceCorrecta == 2,
                    onTextoChange = { opcionC = it },
                    onClick = { indiceCorrecta = 2 }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OpcionEditable(
                    texto = opcionD,
                    seleccionada = indiceCorrecta == 3,
                    onTextoChange = { opcionD = it },
                    onClick = { indiceCorrecta = 3 }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Explicacion", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                CampoGris(
                    valor = explicacion,
                    placeholder = "Escribe la explicacion",
                    onValueChange = { explicacion = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (categoriaSeleccionada.isBlank()) {
                            Toast.makeText(context, "Selecciona una categoria", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (dificultadSeleccionada.isBlank()) {
                            Toast.makeText(context, "Selecciona una dificultad", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (enunciado.isBlank()) {
                            Toast.makeText(context, "El enunciado es obligatorio", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (opcionA.isBlank() || opcionB.isBlank() || opcionC.isBlank() || opcionD.isBlank()) {
                            Toast.makeText(context, "Completa las 4 opciones", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (indiceCorrecta !in 0..3) {
                            Toast.makeText(context, "Selecciona la opcion correcta", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (explicacion.isBlank()) {
                            Toast.makeText(context, "La explicacion es obligatoria", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (esEdicion) {
                            mostrarDialogoConfirmacion = true
                        } else {
                            ejecutarAccion()
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (esEdicion) "Actualizar Pregunta" else "Guardar Pregunta", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
