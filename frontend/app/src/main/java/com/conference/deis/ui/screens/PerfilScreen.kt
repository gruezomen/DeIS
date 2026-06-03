package com.conference.deis.ui.screens

import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.conference.deis.R
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.EquipamientoRecompensaRequest
import com.conference.deis.network.model.EquipamientoRecompensaResponse
import com.conference.deis.network.model.Facultad
import com.conference.deis.network.model.RegisterRequest
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController) {
    val user = UserSession.user
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitInstance.api
    val usuarioId = user?.id.orEmpty()

    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var facultadesSeleccionadas by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var facultadesDisponibles by remember { mutableStateOf<List<Facultad>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var fotoPerfilUrl by remember { mutableStateOf(user?.fotoPerfilUrl) }
    var fotoGoogleUrl by remember { mutableStateOf(user?.fotoGoogleUrl) }
    var equipamiento by remember { mutableStateOf<EquipamientoRecompensaResponse?>(null) }

    val fotoMostrada = fotoPerfilUrl ?: fotoGoogleUrl
    val inicialUsuario = user?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    val imagenMedalla = when (equipamiento?.medallaCodigo) {
        "MEDALLA_ALETA_INICIAL" -> R.drawable.medalla_inicio
        "MEDALLA_SALTO_SEMANAL" -> R.drawable.medalla_constancia
        "MEDALLA_NADO_IMPARABLE" -> R.drawable.medalla_disciplina
        "MEDALLA_DELFIN_DIAMANTE" -> R.drawable.medalla_excelencia
        "MEDALLA_DELFIN_OCULTO" -> R.drawable.dragon_badge
        else -> null
    }

    val imagenMarco = when (equipamiento?.marcoCodigo) {
        "MARCO_OLA" -> R.drawable.plantilla1
        "MARCO_CORAL" -> R.drawable.plantilla2
        "MARCO_OCEANO_PROFUNDO" -> R.drawable.plantilla3
        else -> null
    }

    val tituloEquipado = when (equipamiento?.tituloCodigo) {
        "TITULO_DELFIN_NOVATO" -> "Delfín Novato"
        "TITULO_NADADOR_CONSTANTE" -> "Nadador Constante"
        "TITULO_EXPLORADOR_ARRECIFE" -> "Explorador del Arrecife"
        "TITULO_DELFIN_ACADEMICO" -> "Delfín Académico"
        "TITULO_GUARDIAN_OCEANO" -> "Guardián del Océano"
        else -> null
    }

    fun limpiarEquipamiento(tipo: String) {
        scope.launch {
            try {
                val response = apiService.guardarEquipamiento(
                    usuarioId,
                    EquipamientoRecompensaRequest(limpiarCampo = tipo)
                )

                if (response.isSuccessful) {
                    equipamiento = response.body()
                    Toast.makeText(context, "Se quitó correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No se pudo quitar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

                val response = apiService.actualizarFotoPerfil(
                    id = usuarioId,
                    foto = parteFoto
                )

                if (response.isSuccessful) {
                    val usuarioActualizado = response.body()

                    fotoPerfilUrl = usuarioActualizado?.fotoPerfilUrl
                    fotoGoogleUrl = usuarioActualizado?.fotoGoogleUrl
                    UserSession.user = usuarioActualizado ?: UserSession.user

                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
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
            val responseFacultades = apiService.obtenerFacultades()
            if (responseFacultades.isSuccessful) {
                facultadesDisponibles = responseFacultades.body() ?: emptyList()

                val idsUsuario = user?.facultadesIds ?: emptyList()
                facultadesSeleccionadas = facultadesDisponibles.filter { it.id in idsUsuario }
            }

            if (usuarioId.isNotBlank()) {
                val responseEquipamiento = apiService.obtenerEquipamiento(usuarioId)
                if (responseEquipamiento.isSuccessful) {
                    equipamiento = responseEquipamiento.body()
                }
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
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
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
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

                if (imagenMedalla != null) {
                    Image(
                        painter = painterResource(id = imagenMedalla),
                        contentDescription = "Medalla equipada",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            if (!tituloEquipado.isNullOrBlank()) {
                Text(
                    text = tituloEquipado,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueBackground,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (imagenMarco != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FieldBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = imagenMarco),
                            contentDescription = "Marco equipado",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentScale = ContentScale.Fit
                        )

                        TextButton(
                            onClick = { limpiarEquipamiento("MARCO") }
                        ) {
                            Text("Quitar plantilla")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (imagenMedalla != null || tituloEquipado != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (imagenMedalla != null) {
                        TextButton(onClick = { limpiarEquipamiento("MEDALLA") }) {
                            Text("Quitar medalla")
                        }
                    }

                    if (tituloEquipado != null) {
                        TextButton(onClick = { limpiarEquipamiento("TITULO") }) {
                            Text("Quitar título")
                        }
                    }
                }
            }

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
                                val response = apiService.eliminarFotoPerfil(usuarioId)

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

            Text(
                "Facultades",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
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
                        Toast.makeText(
                            context,
                            "El nombre y al menos una facultad son obligatorios",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    scope.launch {
                        cargando = true

                        try {
                            val response = apiService.actualizarPerfil(
                                usuarioId,
                                RegisterRequest(
                                    nombre = nombre,
                                    correo = user?.gmail ?: "",
                                    contrasena = "",
                                    facultadesIds = facultadesSeleccionadas.mapNotNull { it.id }
                                )
                            )

                            if (response.isSuccessful) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Guardar Cambios", color = Color.White)
                }
            }
        }
    }
}