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
import com.conference.deis.ui.screens.HistorialCompletoScreen
import com.conference.deis.ui.screens.DetalleEstadisticasScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        solicitarPermisoNotificacionesSiHaceFalta()

        setContent {
            DeISApp()
        }
    }

    private fun solicitarPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permisoConcedido = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permisoConcedido) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
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
                composable("mi_racha") {
                   MiRachaScreen(navController)
                  }
                
                composable("mi_progreso") {
                    MiProgresoScreen(navController)
                }
                composable("detalle_estadisticas") {
                    DetalleEstadisticasScreen(navController)
                }
                composable("historial_completo") {
                    HistorialCompletoScreen(navController)
                }
                composable("detalle_intento/{intentoId}") { backStackEntry ->
                    val intentoId = backStackEntry.arguments?.getString("intentoId").orEmpty()
                    DetalleIntentoScreen(
                        navController = navController,
                        intentoId = intentoId
                    )
                }

                composable("mis_logros") {
                    MisLogrosScreen(navController)
                }
                
                composable("mis_recompensas") {
                    MisRecompensasScreen(navController)
                }

                composable("mis_notificaciones") {
                    MisNotificacionesScreen(navController)
                }

                composable("splash") {
                    SplashScreen(navController)
                }

                composable("login") {
                    LoginScreen(navController)
                }

                composable("register") {
                    RegisterScreen(navController)
                }

                composable("seleccionar_facultad") {
                    SeleccionarFacultadScreen(navController)
                }

                composable("success") {
                    SuccessLoadingScreen(navController)
                }

                composable("home") {
                    AdminHomeScreen(navController)
                }

                composable("lista_simulacros") {
                    ListaSimulacrosScreen(navController)
                }

                composable("crear_simulacro") {
                    if (esAdministrador()) {
                        CrearSimulacroScreen(navController)
                    } else {
                        AccesoDenegadoScreen(
                            navController = navController,
                            mensaje = "Solo el administrador puede crear simulacros."
                        )
                    }
                }

                composable("perfil") {
                    PerfilScreen(navController)
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
