package com.conference.deis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.model.Question
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPreguntasScreen(navController: NavHostController) {
    var preguntas by remember { mutableStateOf<List<Question>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.api.obtenerPreguntas()
                if (response.isSuccessful) {
                    preguntas = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar preguntas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                cargando = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lista de Preguntas") },
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
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (preguntas.isEmpty()) {
                Text(
                    text = "No hay preguntas registradas",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(preguntas) { pregunta ->
                      CardPregunta(pregunta) {
                         navController.navigate("organizar_pregunta/${pregunta.id}")
                          }
                    }
                }
            }
        }
    }
}

@Composable
fun CardPregunta(pregunta: Question, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = pregunta.categoria.nombre,
                fontSize = 12.sp,
                color = BlueBackground,
                modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pregunta.enunciado,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dificultad: ${pregunta.dificultad}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }

}
