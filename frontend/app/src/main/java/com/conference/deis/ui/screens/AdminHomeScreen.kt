package com.conference.deis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.ui.components.ActionBox
import com.conference.deis.ui.components.InfoCard
import com.conference.deis.ui.theme.BlueBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavHostController) {
    var totalPreguntas by remember { mutableStateOf(0) }
    var totalBancos by remember { mutableStateOf(0) }
    var cargandoResumen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            cargandoResumen = true

            val responsePreguntas = RetrofitInstance.api.obtenerPreguntas()
            val responseBancos = RetrofitInstance.api.obtenerBancosPreguntas()

            if (responsePreguntas.isSuccessful) {
                totalPreguntas = responsePreguntas.body().orEmpty().size
            }

            if (responseBancos.isSuccessful) {
                totalBancos = responseBancos.body().orEmpty().size
            }
        } catch (e: Exception) {
            totalPreguntas = 0
            totalBancos = 0
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
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("lista_bancos/Examen Simulacro") },
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

            if (esAdministrador()) {
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
            } else {
                ActionBox(
                    texto = "Ver bancos de preguntas",
                    onClick = { navController.navigate("lista_bancos") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ActionBox(
                    texto = "Iniciar simulacro",
                    onClick = { navController.navigate("lista_bancos/Examen Simulacro") }
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
