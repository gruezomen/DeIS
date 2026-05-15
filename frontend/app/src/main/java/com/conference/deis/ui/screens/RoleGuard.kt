package com.conference.deis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.network.UserSession
import com.conference.deis.ui.theme.BlueBackground

private const val ROL_ADMINISTRADOR = "ADMINISTRADOR"
private const val ROL_PREUNIVERSITARIO = "PREUNIVERSITARIO"

fun esAdministrador(): Boolean {
    return UserSession.user?.rol == ROL_ADMINISTRADOR
}

fun esPreuniversitario(): Boolean {
    return UserSession.user?.rol == ROL_PREUNIVERSITARIO
}

@Composable
fun AccesoDenegadoScreen(
    navController: NavHostController,
    mensaje: String = "No tienes permiso para acceder a esta sección."
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso denegado",
            color = Color.Red,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = mensaje,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.popBackStack() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BlueBackground,
                contentColor = Color.White
            )
        ) {
            Text("Volver")
        }
    }
}