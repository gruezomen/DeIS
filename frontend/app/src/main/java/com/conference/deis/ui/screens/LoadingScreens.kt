package com.conference.deis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.conference.deis.R
import com.conference.deis.ui.components.LogoSection
import com.conference.deis.ui.theme.BlueBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1800)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground),
        contentAlignment = Alignment.Center
    ) {
        LogoSection()
    }
}

@Composable
fun SuccessLoadingScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1600)
        navController.navigate("home") {
            popUpTo("login") { inclusive = false }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.delfin),
                contentDescription = "Logo Delfín",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicio de sesión exitoso!\nCargando...",
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}
