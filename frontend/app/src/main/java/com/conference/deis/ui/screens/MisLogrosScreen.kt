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
import com.conference.deis.network.model.LogroItemResponse
import com.conference.deis.network.model.LogrosUsuarioResponse
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisLogrosScreen(navController: NavHostController) {
    var logrosUsuario by remember { mutableStateOf<LogrosUsuarioResponse?>(null) }
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

            val response = RetrofitInstance.api.obtenerLogrosUsuario(usuarioId)

            if (response.isSuccessful) {
                logrosUsuario = response.body()
            } else {
                error = "No se pudieron cargar los logros."
            }
        } catch (e: Exception) {
            error = "Ocurrió un problema al cargar los logros."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis logros") },
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
                        text = "Cargando logros...",
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

                logrosUsuario != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Logros desbloqueados",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueBackground
                            )
                        }

                        if (logrosUsuario!!.desbloqueados.isEmpty()) {
                            item {
                                TextoVacio("Aún no tienes logros desbloqueados.")
                            }
                        } else {
                            items(logrosUsuario!!.desbloqueados) { logro ->
                                LogroCard(logro = logro, desbloqueado = true)
                            }
                        }

                        item {
                            Text(
                                text = "Logros pendientes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (logrosUsuario!!.pendientes.isEmpty()) {
                            item {
                                TextoVacio("No tienes logros pendientes.")
                            }
                        } else {
                            items(logrosUsuario!!.pendientes) { logro ->
                                LogroCard(logro = logro, desbloqueado = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogroCard(
    logro: LogroItemResponse,
    desbloqueado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (desbloqueado) FieldBackground else Color(0xFFF3F3F3)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = logro.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (desbloqueado) BlueBackground else Color.Gray
            )

            Text(
                text = logro.descripcion,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = if (desbloqueado) "Desbloqueado" else "Pendiente",
                fontSize = 13.sp,
                color = if (desbloqueado) Color(0xFF2E7D32) else Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (desbloqueado && !logro.fechaDesbloqueo.isNullOrBlank()) {
                Text(
                    text = "Fecha: ${logro.fechaDesbloqueo}",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TextoVacio(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Text(
            text = mensaje,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}