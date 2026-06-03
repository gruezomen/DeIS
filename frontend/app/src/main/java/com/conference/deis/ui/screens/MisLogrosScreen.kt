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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import com.conference.deis.R

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
    val iconoLogro = obtenerIconoLogro(logro)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (desbloqueado) Color(0xFFF7FBFF) else Color(0xFFF1F1F1)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogroIcono(
                icono = iconoLogro,
                desbloqueado = desbloqueado
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = logro.titulo,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (desbloqueado) BlueBackground else Color(0xFF8A8A8A)
                )

                Text(
                    text = logro.descripcion,
                    fontSize = 14.sp,
                    color = if (desbloqueado) Color(0xFF222222) else Color(0xFF777777),
                    modifier = Modifier.padding(top = 5.dp)
                )

                EstadoLogroBadge(desbloqueado = desbloqueado)

                if (desbloqueado && !logro.fechaDesbloqueo.isNullOrBlank()) {
                    Text(
                        text = "Fecha: ${logro.fechaDesbloqueo}",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
@Composable
private fun LogroIcono(
    icono: Int,
    desbloqueado: Boolean
) {
    val matrizColor = ColorMatrix().apply {
        setToSaturation(if (desbloqueado) 1f else 0f)
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(if (desbloqueado) Color(0xFFEAF7FF) else Color(0xFFE0E0E0))
            .border(
                width = 2.dp,
                color = if (desbloqueado) BlueBackground else Color(0xFFBDBDBD),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icono),
            contentDescription = null,
            modifier = Modifier
                .size(86.dp)
                .alpha(if (desbloqueado) 1f else 0.35f),
            colorFilter = if (desbloqueado) {
                null
            } else {
                ColorFilter.colorMatrix(matrizColor)
            }
        )
    }
}

@Composable
private fun EstadoLogroBadge(
    desbloqueado: Boolean
) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .background(
                color = if (desbloqueado) Color(0xFFE5F6E8) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = if (desbloqueado) "Desbloqueado" else "Pendiente",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (desbloqueado) Color(0xFF2E7D32) else Color(0xFF777777)
        )
    }
}

private fun obtenerIconoLogro(logro: LogroItemResponse): Int {
    val codigo = logro.codigo.trim().lowercase()
    val titulo = logro.titulo.trim().lowercase()

    return when {
        codigo.contains("primera_practica") ||
            codigo.contains("primer_paso") ||
            titulo.contains("primer paso") -> R.drawable.logro_primer_paso

        codigo.contains("cinco_practicas") ||
            titulo.contains("constante") -> R.drawable.logro_constante

        codigo.contains("primer_simulacro") ||
            titulo.contains("primera prueba") -> R.drawable.logro_primera_prueba

        codigo.contains("tres_simulacros") ||
            titulo.contains("preparado") -> R.drawable.logro_preparado

        codigo.contains("precision_alta") ||
            titulo.contains("precisión alta") ||
            titulo.contains("precision alta") -> R.drawable.logro_precision_alta

        codigo.contains("sin_errores") ||
            titulo.contains("sin errores") -> R.drawable.logro_sin_errores

        codigo.contains("racha_7_dias") ||
            codigo.contains("racha_fuerte") ||
            titulo.contains("racha fuerte") -> R.drawable.logro_racha_fuerte

        codigo.contains("coleccionista") ||
            titulo.contains("coleccionista") -> R.drawable.logro_coleccionista

        codigo.contains("racha_15_dias") ||
            codigo.contains("disciplina_total") ||
            titulo.contains("disciplina total") -> R.drawable.logro_disciplina_total

        codigo.contains("racha_3_dias") ||
            codigo.contains("en_racha") ||
            titulo.contains("en racha") -> R.drawable.logro_en_racha

        else -> R.drawable.logro_default
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