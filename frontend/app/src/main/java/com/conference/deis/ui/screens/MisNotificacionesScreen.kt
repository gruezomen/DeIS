package com.conference.deis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.conference.deis.network.model.Notificacion
import com.conference.deis.ui.theme.BlueBackground
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisNotificacionesScreen(navController: NavHostController) {
    val notificaciones = remember { mutableStateListOf<Notificacion>() }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            cargando = true
            error = null

            val usuarioId = UserSession.user?.id?.toString()
            if (usuarioId.isNullOrBlank()) {
                error = "No se encontró la sesión del usuario."
                return@LaunchedEffect
            }

            val response = RetrofitInstance.api.obtenerNotificaciones(usuarioId)
            if (response.isSuccessful) {
                notificaciones.clear()
                notificaciones.addAll(response.body().orEmpty())
            } else {
                error = "No se pudieron cargar las notificaciones."
            }
        } catch (e: Exception) {
            error = "Ocurrió un problema al cargar las notificaciones."
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis notificaciones") },
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
            when {
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text(
                        text = error.orEmpty(),
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                notificaciones.isEmpty() -> {
                    Text(
                        text = "No tienes notificaciones disponibles.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notificaciones, key = { it.id ?: it.fechaCreacion }) { notificacion ->
                            NotificacionCard(
                                notificacion = notificacion,
                                onMarcarLeida = {
                                    val id = notificacion.id ?: return@NotificacionCard

                                    if (!notificacion.leida) {
                                        scope.launch {
                                            try {
                                                val response = RetrofitInstance.api.marcarNotificacionLeida(id)
                                                if (response.isSuccessful) {
                                                    val actualizada = response.body()
                                                    if (actualizada != null) {
                                                        val index = notificaciones.indexOfFirst { it.id == actualizada.id }
                                                        if (index != -1) {
                                                            notificaciones[index] = actualizada
                                                        }
                                                    }
                                                }
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
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
private fun NotificacionCard(
    notificacion: Notificacion,
    onMarcarLeida: () -> Unit
) {
    val colorFondo = if (notificacion.leida) Color.White else Color(0xFFEAF3FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarcarLeida() },
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notificacion.titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = notificacion.mensaje,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = if (notificacion.leida) "Leída" else "No leída",
                fontSize = 12.sp,
                color = if (notificacion.leida) Color.Gray else BlueBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = notificacion.fechaCreacion,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}