package com.conference.deis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RachaResponse
import com.conference.deis.ui.theme.BlueBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiRachaScreen(navController: NavHostController) {
    var racha by remember { mutableStateOf<RachaResponse?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            cargando = true
            mensajeError = null

            val usuarioId = UserSession.user?.id

            if (usuarioId.isNullOrBlank()) {
                mensajeError = "No se encontró la sesión del usuario."
                return@LaunchedEffect
            }

            val response = RetrofitInstance.api.obtenerRacha(usuarioId)

            if (response.isSuccessful) {
                racha = response.body()
            } else {
                mensajeError = "No se pudo obtener la racha."
            }
        } catch (e: Exception) {
            mensajeError = "Ocurrió un problema al cargar la racha."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi racha") },
                navigationIcon = {
                    Text(
                        text = "←",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { navController.popBackStack() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                cargando -> {
                    Text(
                        text = "Cargando racha...",
                        fontSize = 18.sp
                    )
                }

                mensajeError != null -> {
                    Text(
                        text = mensajeError ?: "Error desconocido.",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                racha != null -> {
                    RachaDetalleCard(racha = racha!!)
                }
            }
        }
    }
}

@Composable
private fun RachaDetalleCard(racha: RachaResponse) {
    val estadoVisual = when (racha.estadoDelfin) {
        "FELIZ" -> "Feliz"
        "DESPIERTO" -> "Despierto"
        else -> "Dormido"
    }

    val mensajeEstado = when (racha.estadoDelfin) {
        "FELIZ" -> "¡Excelente! Cumpliste una semana o más de constancia."
        "DESPIERTO" -> "Tu racha está activa. Sigue practicando cada día."
        else -> "Tu delfín está dormido. Practica hoy para activar tu racha."
    }

    val ultimaPracticaTexto = racha.ultimaPractica ?: "Sin práctica registrada"

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF3FF)
        ),
        modifier = Modifier.padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.delfin),
                contentDescription = "Delfín de racha",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "${racha.diasConsecutivos} días",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = BlueBackground
            )

            Text(
                text = estadoVisual,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensajeEstado,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Última práctica: $ultimaPracticaTexto",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}