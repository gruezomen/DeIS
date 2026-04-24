package com.conference.deis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conference.deis.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeISApp()
        }
    }
}

@Composable
fun DeISApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(navController)
                }
                composable("login") {
                    LoginScreen(navController)
                }
                composable("register") {
                    RegisterScreen(navController)
                }
                composable("success") {
                    SuccessLoadingScreen(navController)
                }
                composable("home") {
                    AdminHomeScreen(navController)
                }
                composable("lista_preguntas") {
                    ListaPreguntasScreen(navController)
                }
                composable("crear_pregunta") {
                    CrearPreguntaScreen(navController)
                }
               composable("editar_pregunta/{id}") { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")

    CrearPreguntaScreen(
        navController = navController,
        preguntaId = id
    )
}

composable("organizar_pregunta/{id}") { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")

    if (id != null) {
        OrganizarPreguntaScreen(
            navController = navController,
            preguntaId = id
        )
    }
}
            }
        }
    }
}
