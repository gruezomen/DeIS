package com.conference.deis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.ui.components.ActionBox
import com.conference.deis.ui.components.InfoCard
import com.conference.deis.ui.theme.BlueBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavHostController) {
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
                text = "Informacion",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("20\npreguntas")
                InfoCard("3\nbancos")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Qué quieres hacer?",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionBox(
                texto = "Crear pregunta",
                onClick = { navController.navigate("crear_pregunta") }
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
                texto = "Practicar preguntas",
                onClick = { navController.navigate("resolver_pregunta") }
            )
        }
    }
}
