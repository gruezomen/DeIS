package com.conference.deis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RecompensaItemResponse
import com.conference.deis.network.model.RecompensasUsuarioResponse
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisRecompensasScreen(navController: NavHostController) {
    var recompensasUsuario by remember { mutableStateOf<RecompensasUsuarioResponse?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            cargando = true
            error = null

            val usuarioId = UserSession.user?.id

            if (usuarioId.isNullOrBlank()) {
                error = "No se encontró la sesión del usuario."
                return@LaunchedEffect
            }

            val response = RetrofitInstance.api.obtenerRecompensasUsuario(usuarioId)

            if (response.isSuccessful) {
                recompensasUsuario = response.body()
            } else {
                error = "No se pudieron cargar las recompensas."
            }
        } catch (e: Exception) {
            error = "Ocurrió un problema al cargar las recompensas."
        } finally {
            cargando = false
        }
    }

    val recompensasPractica = recompensasUsuario?.recompensas?.filter {
        it.tipo.contains("PRACTICA")
    }.orEmpty()

    val recompensasSimulacro = recompensasUsuario?.recompensas?.filter {
        it.tipo.contains("SIMULACRO")
    }.orEmpty()

    val recompensasRacha = recompensasUsuario?.recompensas?.filter {
        it.tipo == "RACHA"
    }.orEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis recompensas") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver", color = Color.White)
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
                    Text(
                        text = "Cargando recompensas...",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                error != null -> {
                    Text(
                        text = error.orEmpty(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        color = Color.Red
                    )
                }

                recompensasUsuario != null -> {
                    if (
                        recompensasPractica.isEmpty() &&
                        recompensasSimulacro.isEmpty() &&
                        recompensasRacha.isEmpty()
                    ) {
                        TextoVacioRecompensas(
                            "Aún no tienes recompensas registradas."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (recompensasPractica.isNotEmpty()) {
                                item {
                                    TituloSeccionRecompensas("Recompensas de práctica")
                                }

                                items(recompensasPractica) { recompensa ->
                                    RecompensaCard(recompensa)
                                }
                            }

                            if (recompensasSimulacro.isNotEmpty()) {
                                item {
                                    TituloSeccionRecompensas("Recompensas de simulacro")
                                }

                                items(recompensasSimulacro) { recompensa ->
                                    RecompensaCard(recompensa)
                                }
                            }

                            if (recompensasRacha.isNotEmpty()) {
                                item {
                                    TituloSeccionRecompensas("Recompensas de racha")
                                }

                                items(recompensasRacha) { recompensa ->
                                    RecompensaCard(recompensa)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloSeccionRecompensas(titulo: String) {
    Text(
        text = titulo,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = BlueBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun RecompensaCard(recompensa: RecompensaItemResponse) {
    val colorFondo = when (recompensa.tipo) {
        "BAJO_PRACTICA" -> Color(0xFFFFF3E0)
        "MEDIO_PRACTICA" -> Color(0xFFE3F2FD)
        "ALTO_PRACTICA" -> Color(0xFFE8F5E9)
        "BAJO_SIMULACRO" -> Color(0xFFFFF3E0)
        "MEDIO_SIMULACRO" -> Color(0xFFE3F2FD)
        "ALTO_SIMULACRO" -> Color(0xFFE8F5E9)
        "RACHA" -> Color(0xFFF3E5F5)
        else -> FieldBackground
    }

    val colorTexto = when (recompensa.tipo) {
        "BAJO_PRACTICA" -> Color(0xFFE65100)
        "MEDIO_PRACTICA" -> Color(0xFF1565C0)
        "ALTO_PRACTICA" -> Color(0xFF2E7D32)
        "BAJO_SIMULACRO" -> Color(0xFFE65100)
        "MEDIO_SIMULACRO" -> Color(0xFF1565C0)
        "ALTO_SIMULACRO" -> Color(0xFF2E7D32)
        "RACHA" -> Color(0xFF6A1B9A)
        else -> BlueBackground
    }

    val tipoLegible = when (recompensa.tipo) {
        "BAJO_PRACTICA" -> "Resultado bajo en práctica"
        "MEDIO_PRACTICA" -> "Resultado medio en práctica"
        "ALTO_PRACTICA" -> "Resultado alto en práctica"
        "BAJO_SIMULACRO" -> "Resultado bajo en simulacro"
        "MEDIO_SIMULACRO" -> "Resultado medio en simulacro"
        "ALTO_SIMULACRO" -> "Resultado alto en simulacro"
        "RACHA" -> "Constancia por racha"
        else -> recompensa.tipo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recompensa.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )

            Text(
                text = recompensa.descripcion,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Tipo: $tipoLegible",
                fontSize = 13.sp,
                color = colorTexto,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!recompensa.fechaObtencion.isNullOrBlank()) {
                Text(
                    text = "Obtenida: ${recompensa.fechaObtencion}",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TextoVacioRecompensas(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mensaje,
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}