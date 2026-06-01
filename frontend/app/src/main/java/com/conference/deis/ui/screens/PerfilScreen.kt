package com.conference.deis.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.Facultad
import com.conference.deis.network.model.RegisterRequest
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController) {
    val user = UserSession.user
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var facultadesSeleccionadas by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var facultadesDisponibles by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var fotoPerfilUrl by remember { mutableStateOf(user?.fotoPerfilUrl) }
    var fotoGoogleUrl by remember { mutableStateOf(user?.fotoGoogleUrl) }

val fotoMostrada = fotoPerfilUrl ?: fotoGoogleUrl
val inicialUsuario = user?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

val selectorImagen = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri ?: return@rememberLauncherForActivityResult

    scope.launch {
        cargando = true

        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            }

            if (bytes == null) {
                Toast.makeText(context, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
                cargando = false
                return@launch
            }

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

            val parteFoto = MultipartBody.Part.createFormData(
                name = "foto",
                filename = "perfil_${user?.id}.jpg",
                body = requestBody
            )

            val idUsuario = user?.id ?: ""

            val response = RetrofitInstance.api.actualizarFotoPerfil(
                id = idUsuario,
                foto = parteFoto
            )

            if (response.isSuccessful) {
                val usuarioActualizado = response.body()

                fotoPerfilUrl = usuarioActualizado?.fotoPerfilUrl
                fotoGoogleUrl = usuarioActualizado?.fotoGoogleUrl
                UserSession.user = usuarioActualizado ?: UserSession.user

                Toast.makeText(
                context,
                "Foto: ${usuarioActualizado?.fotoPerfilUrl ?: "sin url"}",
                 Toast.LENGTH_LONG
                 ).show()
            } else {
                Toast.makeText(context, "No se pudo actualizar la foto", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
        } finally {
            cargando = false
        }
    }
}
    LaunchedEffect(Unit) {
        try {
            val responseFacultades = RetrofitInstance.api.obtenerFacultades()
            if (responseFacultades.isSuccessful) {
                facultadesDisponibles = responseFacultades.body() ?: emptyList()
                
                // Marcar las facultades que el usuario ya tiene
                val idsUsuario = user?.facultadesIds ?: emptyList()
                facultadesSeleccionadas = facultadesDisponibles.filter { it.id in idsUsuario }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueBackground)
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
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(FieldBackground),
        contentAlignment = Alignment.Center
    ) {
        if (!fotoMostrada.isNullOrBlank()) {
            AsyncImage(
                model = fotoMostrada,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = inicialUsuario,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { selectorImagen.launch("image/*") },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = BlueBackground)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cambiar")
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    cargando = true

                    try {
                        val idUsuario = user?.id ?: ""

                        val response = RetrofitInstance.api.eliminarFotoPerfil(idUsuario)

                        if (response.isSuccessful) {
                            val usuarioActualizado = response.body()

                            fotoPerfilUrl = usuarioActualizado?.fotoPerfilUrl
                            fotoGoogleUrl = usuarioActualizado?.fotoGoogleUrl
                            UserSession.user = usuarioActualizado ?: UserSession.user

                            Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No se pudo eliminar la foto", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando && fotoPerfilUrl != null
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Borrar")
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Facultades", fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val textoSeleccion = if (facultadesSeleccionadas.isEmpty()) {
                    "Selecciona tus facultades"
                } else {
                    facultadesSeleccionadas.joinToString(", ") { it.nombre }
                }

                OutlinedTextField(
                    value = textoSeleccion,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FieldBackground,
                        unfocusedContainerColor = FieldBackground
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    facultadesDisponibles.forEach { facultad ->
                        val isSelected = facultadesSeleccionadas.any { it.id == facultad.id }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isSelected, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(facultad.nombre)
                                }
                            },
                            onClick = {
                                facultadesSeleccionadas = if (isSelected) {
                                    facultadesSeleccionadas.filter { it.id != facultad.id }
                                } else {
                                    facultadesSeleccionadas + facultad
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nombre.isBlank() || facultadesSeleccionadas.isEmpty()) {
                        Toast.makeText(context, "El nombre y al menos una facultad son obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        cargando = true
                        try {
                            val idUsuario = user?.id ?: ""
                            val response = RetrofitInstance.api.actualizarPerfil(
                                idUsuario,
                                RegisterRequest(
                                    nombre = nombre,
                                    correo = user?.gmail ?: "",
                                    contrasena = "", // No cambiamos contraseña aquí por ahora
                                    facultadesIds = facultadesSeleccionadas.mapNotNull { it.id }
                                )
                            )

                            if (response.isSuccessful) {
                                // Actualizar sesión local
                                UserSession.user = UserSession.user?.copy(
                                    nombre = nombre,
                                    facultadesIds = facultadesSeleccionadas.mapNotNull { it.id }
                                )
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios", color = Color.White)
                }
            }
        }
    }
}