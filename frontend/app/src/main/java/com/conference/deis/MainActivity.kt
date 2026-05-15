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
import com.conference.deis.ui.screens.AccesoDenegadoScreen
import com.conference.deis.ui.screens.AdminHomeScreen
import com.conference.deis.ui.screens.CrearBancoScreen
import com.conference.deis.ui.screens.CrearPreguntaScreen
import com.conference.deis.ui.screens.DetallesBancoScreen
import com.conference.deis.ui.screens.ListaBancosScreen
import com.conference.deis.ui.screens.ListaPreguntasScreen
import com.conference.deis.ui.screens.LoginScreen
import com.conference.deis.ui.screens.OrganizarPreguntaScreen
import com.conference.deis.ui.screens.RegisterScreen
import com.conference.deis.ui.screens.ResolverPreguntaScreen
import com.conference.deis.ui.screens.SplashScreen
import com.conference.deis.ui.screens.SuccessLoadingScreen
import com.conference.deis.ui.screens.esAdministrador

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
                    if (esAdministrador()) {
                        ListaPreguntasScreen(navController)
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede ver, editar, organizar o eliminar preguntas."
                        )
                    }
                }

                composable("lista_bancos/{titulo}") { backStackEntry ->
                    val titulo = backStackEntry.arguments?.getString("titulo")

                    ListaBancosScreen(
                        navController = navController,
                        tituloPersonalizado = titulo
                    )
                }

                composable("lista_bancos") {
                    ListaBancosScreen(
                        navController = navController,
                        tituloPersonalizado = null
                    )
                }

                composable("detalles_banco/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")

                    if (id != null) {
                        DetallesBancoScreen(
                            navController = navController,
                            bancoId = id
                        )
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "No se pudo identificar el banco de preguntas."
                        )
                    }
                }

                composable("crear_pregunta") {
                    if (esAdministrador()) {
                        CrearPreguntaScreen(navController)
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede crear preguntas."
                        )
                    }
                }

                composable("crear_banco") {
                    if (esAdministrador()) {
                        CrearBancoScreen(navController)
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede crear bancos de preguntas."
                        )
                    }
                }

                composable("resolver_simulacro/{simulacroId}") { backStackEntry ->
                    val simulacroId = backStackEntry.arguments?.getString("simulacroId")

                    ResolverPreguntaScreen(
                        navController = navController,
                        simulacroId = simulacroId
                    )
                }

                composable("resolver_pregunta/{bancoId}/{tiempoMinutos}") { backStackEntry ->
                    val bancoId = backStackEntry.arguments?.getString("bancoId")
                    val tiempoMinutos = backStackEntry.arguments
                        ?.getString("tiempoMinutos")
                        ?.toIntOrNull()

                    ResolverPreguntaScreen(
                        navController = navController,
                        bancoId = bancoId,
                        tiempoMinutosInicial = tiempoMinutos
                    )
                }

                composable("resolver_pregunta/{bancoId}") { backStackEntry ->
                    val bancoId = backStackEntry.arguments?.getString("bancoId")

                    ResolverPreguntaScreen(
                        navController = navController,
                        bancoId = bancoId
                    )
                }

                composable("resolver_pregunta") {
                    ResolverPreguntaScreen(
                        navController = navController
                    )
                }

                composable("editar_pregunta/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")

                    if (esAdministrador()) {
                        CrearPreguntaScreen(
                            navController = navController,
                            preguntaId = id
                        )
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede editar preguntas."
                        )
                    }
                }

                composable("organizar_pregunta/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")

                    if (esAdministrador() && id != null) {
                        OrganizarPreguntaScreen(
                            navController = navController,
                            preguntaId = id
                        )
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede organizar preguntas en bancos."
                        )
                    }
                }
            }
        }
    }
}
