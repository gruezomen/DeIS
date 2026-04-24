package com.conference.deis.ui.screens

import com.conference.deis.network.model.AsociarPreguntaBancoRequest
import com.conference.deis.network.model.BancoPregunta
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
    var bancosPregunta by remember { mutableStateOf<List<BancoPregunta>>(emptyList()) }
    var bancoSeleccionadoId by remember { mutableStateOf("") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mensajeConfirmacion by remember { mutableStateOf("") }

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
            val bancosResponse = RetrofitInstance.api.obtenerBancosPreguntas()

            if (bancosResponse.isSuccessful) {
                bancosPregunta = bancosResponse.body() ?: emptyList()
            } else {
                  Toast.makeText(context, "No se pudieron cargar los bancos de preguntas", Toast.LENGTH_SHORT).show()
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
    
     fun asociarABanco() {
       if (bancoSeleccionadoId.isBlank()) {
           Toast.makeText(context, "Selecciona un banco de preguntas", Toast.LENGTH_SHORT).show()
           return
        }

        scope.launch {
           guardando = true

           try {
               val response = RetrofitInstance.api.asociarPreguntaABanco(
                   preguntaId,
                   AsociarPreguntaBancoRequest(
                      bancoPreguntaId = bancoSeleccionadoId
                    )
                )

                 if (response.isSuccessful) {
                   mensajeConfirmacion = "La categoría fue asignada correctamente a la pregunta."
                   mostrarConfirmacion = true
                 } else {
                    Toast.makeText(context, "No se pudo asignar la categoría", Toast.LENGTH_SHORT).show()
                 }
                   } catch (e: Exception) {
                     Toast.makeText(context, "Error al asociar la pregunta al banco", Toast.LENGTH_SHORT).show()
                 } finally {
                     guardando = false
                }
            }
         }
         if (mostrarConfirmacion) {
              AlertDialog(
                onDismissRequest = {
                  mostrarConfirmacion = false
                   navController.popBackStack()
               },
               title = {
               Text("Organización completada")
               },
               text = {
                  Text(mensajeConfirmacion)
                },
                confirmButton = {
                   TextButton(
                   onClick = {
                      mostrarConfirmacion = false
                      navController.popBackStack()
                }
                ) {
                   Text("Aceptar", color = BlueBackground)
                  }
                },
                     containerColor = Color.White,
                     titleContentColor = Color.Black,
                     textContentColor = Color.DarkGray
                   )
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
                    
                     Spacer(modifier = Modifier.height(20.dp))

Text(
    text = "Asignar banco de preguntas",
    fontSize = 16.sp,
    color = Color.Black
)

Spacer(modifier = Modifier.height(10.dp))

if (bancosPregunta.isEmpty()) {
    Text(
        text = "No hay bancos de preguntas disponibles",
        fontSize = 13.sp,
        color = Color.Gray
    )
} else {
    bancosPregunta.forEach { banco ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (bancoSeleccionadoId == banco.id) {
                    Color(0xFFDDEBFF)
                } else {
                    Color(0xFFF2F2F2)
                }
            ),
            onClick = {
                bancoSeleccionadoId = banco.id
            }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Banco de preguntas",
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Facultad: ${banco.facultadId}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Preguntas asociadas: ${banco.preguntaIds.size}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

Spacer(modifier = Modifier.height(12.dp))

Button(
    onClick = { asociarABanco() },
    enabled = !guardando,
    modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
    shape = RoundedCornerShape(10.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = BlueBackground,
        contentColor = Color.White
    )
) {
    Text("Asociar a banco", fontSize = 18.sp)
}

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