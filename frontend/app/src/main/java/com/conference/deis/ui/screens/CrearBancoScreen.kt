package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.CrearBancoRequest
import com.conference.deis.network.model.Facultad
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearBancoScreen(navController: NavHostController) {
    if (!esAdministrador()) {
        AccesoDenegadoScreen(
            navController = navController,
            mensaje = "Solo el administrador puede crear bancos de preguntas."
        )
        return
    }
    var facultades by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var facultadSeleccionada by remember { mutableStateOf<Facultad?>(null) }
    var cargandoFacultades by remember { mutableStateOf(true) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.obtenerFacultades()
            if (response.isSuccessful) {
                facultades = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar facultades", Toast.LENGTH_SHORT).show()
        } finally {
            cargandoFacultades = false
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Nuevo Banco de Preguntas",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selecciona la Facultad",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (!cargandoFacultades) expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = facultadSeleccionada?.nombre ?: if (cargandoFacultades) "Cargando..." else "Selecciona una facultad",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona una facultad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    facultades.forEach { facultad ->
                        DropdownMenuItem(
                            text = { Text(facultad.nombre) },
                            onClick = {
                                facultadSeleccionada = facultad
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val fId = facultadSeleccionada?.nombre ?: ""
                    if (fId.isBlank()) {
                        Toast.makeText(context, "Por favor selecciona una facultad", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val adminId = UserSession.user?.id ?: ""
                    if (adminId.isBlank()) {
                         Toast.makeText(context, "Error: No se encontró sesión de administrador", Toast.LENGTH_SHORT).show()
                         return@Button
                    }

                    scope.launch {
                        cargando = true
                        try {
                            // Por ahora enviamos el nombre como facultadId para mantener compatibilidad con el backend
                            // que espera el nombre en facultadId para el filtrado normalizado.
                            val request = CrearBancoRequest(fId, adminId)
                            val response = RetrofitInstance.api.crearBanco(request)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Banco creado exitosamente", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Error al crear el banco", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando && !cargandoFacultades,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear Banco", fontSize = 16.sp)
                }
            }
        }
    }
}
