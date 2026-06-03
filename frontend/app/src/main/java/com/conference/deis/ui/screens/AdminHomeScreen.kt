package com.conference.deis.ui.screens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.ui.components.ActionBox
import com.conference.deis.ui.components.InfoCard
import com.conference.deis.ui.theme.BlueBackground
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.layout.offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavHostController) {
    var totalPreguntas by remember { mutableStateOf(0) }
    var totalBancos by remember { mutableStateOf(0) }
    var cargandoResumen by remember { mutableStateOf(true) }
    var menuExpandido by remember { mutableStateOf(false) }
    var cantidadNotificacionesNoLeidas by remember { mutableStateOf(0) }

    
    val esAdmin = UserSession.user?.rol == "ADMINISTRADOR"

    LaunchedEffect(Unit) {
        try {
            cargandoResumen = true

            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()
            val userId = UserSession.user?.id
            val responseBancos = if (esAdministrador() || userId == null) {
                RetrofitInstance.api.obtenerBancosPreguntas()
            } else {
                RetrofitInstance.api.obtenerBancosPorUsuario(userId)
            }

            if (responsePreguntas.isSuccessful) {
                totalPreguntas = responsePreguntas.body().orEmpty().size
            }

            if (responseBancos.isSuccessful) {
                totalBancos = responseBancos.body().orEmpty().size
            }

            if (!esAdmin) {
                val usuarioId = UserSession.user?.id?.toString()

                if (!usuarioId.isNullOrBlank()) {
                    val responseNotificaciones =
                        RetrofitInstance.api.obtenerNotificaciones(usuarioId)

                    if (responseNotificaciones.isSuccessful) {
                        cantidadNotificacionesNoLeidas =
                            responseNotificaciones.body().orEmpty().count { !it.leida }
                    } else {
                        cantidadNotificacionesNoLeidas = 0
                    }
                } else {
                    cantidadNotificacionesNoLeidas = 0
                }
            } else {
                cantidadNotificacionesNoLeidas = 0
            }

        } catch (e: Exception) {
            totalPreguntas = 0
            totalBancos = 0
            cantidadNotificacionesNoLeidas = 0
        } finally {
            cargandoResumen = false
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
                    if (!esAdmin) {
                        IconButton(
                            onClick = { navController.navigate("mis_notificaciones") }
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )

                                if (cantidadNotificacionesNoLeidas > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 6.dp, y = (-4).dp)
                                            .size(18.dp)
                                            .background(Color.Red, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cantidadNotificacionesNoLeidas > 9) {
                                                "9+"
                                            } else {
                                                cantidadNotificacionesNoLeidas.toString()
                                            },
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    val usuarioActual = UserSession.user
                    val fotoUsuario = usuarioActual?.fotoPerfilUrl ?: usuarioActual?.fotoGoogleUrl
                    val inicialUsuario = usuarioActual?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .background(Color(0xFFE6E6E6), CircleShape)
                            .clickable { menuExpandido = true },
                        contentAlignment = Alignment.Center
                    ) {
                         if (!fotoUsuario.isNullOrBlank()) {
        AsyncImage(
            model = fotoUsuario,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Text(
            text = inicialUsuario,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

                        
                       DropdownMenu(
                       expanded = menuExpandido,
                       onDismissRequest = { menuExpandido = false }
                       ) {
                         DropdownMenuItem(
                         text = { Text("Perfil") },
                          onClick = {
                             menuExpandido = false
                               navController.navigate("perfil")
                             }
                              )

    if (!esAdmin) {
        DropdownMenuItem(
            text = { Text("Mi racha") },
            onClick = {
                menuExpandido = false
                navController.navigate("mi_racha")
            }
        )

        DropdownMenuItem(
            text = { Text("Mis logros") },
            onClick = {
                menuExpandido = false
                navController.navigate("mis_logros")
            }
        )

        DropdownMenuItem(
            text = { Text("Recompensas") },
            onClick = {
                menuExpandido = false
                navController.navigate("mis_recompensas")
            }
        )

        DropdownMenuItem(
            text = { Text("Estadísticas") },
            onClick = {
                menuExpandido = false
                navController.navigate("mi_progreso")
            }
        )

        DropdownMenuItem(
            text = { Text("Historial") },
            onClick = {
                menuExpandido = false
                navController.navigate("mi_progreso")
            }
        )
    }

    DropdownMenuItem(
        text = { Text("Cerrar sesión") },
        onClick = {
            menuExpandido = false
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    )
}
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("lista_simulacros") },
                    icon = { },
                    label = { Text("Simulacro") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("lista_bancos") },
                    icon = { },
                    label = { Text("Banco") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
    text = "Información del sistema",
    fontSize = 14.sp,
    color = Color.Black
)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(
                    if (cargandoResumen) {
                        "...\npreguntas"
                    } else {
                        "$totalPreguntas\npreguntas"
                    }
                )

                InfoCard(
                    if (cargandoResumen) {
                        "...\nbancos"
                    } else {
                        "$totalBancos\nbancos"
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Qué quieres hacer?",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

         if (esAdmin) {
                ActionBox(
                    texto = "Crear pregunta",
                    onClick = { navController.navigate("crear_pregunta") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Crear banco de preguntas",
                    onClick = { navController.navigate("crear_banco") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Actualizar pregunta",
                    onClick = { navController.navigate("lista_preguntas") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Ver lista de banco de preguntas",
                    onClick = { navController.navigate("lista_bancos") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Crear simulacro",
                    onClick = { navController.navigate("crear_simulacro") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Ver simulacros",
                    onClick = { navController.navigate("lista_simulacros") }
                )
            } else {
                ActionBox(
                    texto = "Ver bancos de preguntas",
                    onClick = { navController.navigate("lista_bancos") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Iniciar simulacro",
                    onClick = { navController.navigate("lista_simulacros") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Practicar preguntas",
                    onClick = { navController.navigate("resolver_pregunta") }
                )
            }
        }
    }
}
