package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RegisterRequest
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarFacultadScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = UserSession.user ?: return

    val facultadesList = listOf("Ciencias y Tecnología", "Medicina", "Derecho", "Economía", "Arquitectura")
    val facultadesSeleccionadas = remember { mutableStateListOf<String>() }
    var guardando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Completar perfil") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlueBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido, ${user.nombre}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Para continuar, selecciona la facultad o facultades a las que estás postulando:",
                fontSize = 16.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(facultadesList) { facultad ->
                    val isSelected = facultadesSeleccionadas.contains(facultad)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BlueBackground.copy(alpha = 0.1f) else FieldBackground
                        ),
                        onClick = {
                            if (isSelected) {
                                facultadesSeleccionadas.remove(facultad)
                            } else {
                                facultadesSeleccionadas.add(facultad)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) {
                                        facultadesSeleccionadas.remove(facultad)
                                    } else {
                                        facultadesSeleccionadas.add(facultad)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BlueBackground)
                            )
                            Text(
                                text = facultad,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (facultadesSeleccionadas.isEmpty()) {
                        Toast.makeText(context, "Selecciona al menos una facultad", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        guardando = true
                        try {
                            val response = RetrofitInstance.api.actualizarPerfil(
                                user.id!!,
                                RegisterRequest(
                                    nombre = user.nombre,
                                    correo = user.gmail,
                                    contrasena = "", // No se cambia la contraseña aquí
                                    facultadesIds = facultadesSeleccionadas.toList()
                                )
                            )

                            if (response.isSuccessful) {
                                // Actualizar sesión local
                                UserSession.user = UserSession.user?.copy(
                                    facultadesIds = facultadesSeleccionadas.toList()
                                )
                                Toast.makeText(context, "Perfil completado", Toast.LENGTH_SHORT).show()
                                navController.navigate("success") {
                                    popUpTo("seleccionar_facultad") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error al guardar preferencias", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = !guardando && facultadesSeleccionadas.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBackground,
                    contentColor = Color.White
                )
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
