package com.conference.deis.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conference.deis.R
import com.conference.deis.ui.theme.*

@Composable
fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.delfin),
            contentDescription = "Logo Delfín",
            modifier = Modifier.size(140.dp)
        )

        Text(
            text = "DelIS",
            fontSize = 22.sp,
            color = Color.Black
        )
    }
}

@Composable
fun RegisterHeaderIcon() {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.usuario),
            contentDescription = "Icono usuario",
            modifier = Modifier.size(90.dp)
        )
    }
}

@Composable
fun BotonCategoria(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (seleccionado) CardColorBox else ActionBoxColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CampoGris(
    valor: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ActionBoxColor,
            unfocusedContainerColor = ActionBoxColor,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BotonDificultad(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (seleccionado) CardColorBox else ActionBoxColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
fun OpcionEditable(
    texto: String,
    seleccionada: Boolean,
    onTextoChange: (String) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (seleccionada) SuccessGreen else ActionBoxColor,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (seleccionada) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        OutlinedTextField(
            value = texto,
            onValueChange = onTextoChange,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ActionBoxColor,
                unfocusedContainerColor = ActionBoxColor,
                focusedBorderColor = if (seleccionada) SuccessGreen else Color.Transparent,
                unfocusedBorderColor = if (seleccionada) SuccessGreen else Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun InfoCard(texto: String) {
    Card(
        modifier = Modifier.size(width = 90.dp, height = 72.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CardColorBox)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = texto,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActionBox(
    texto: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ActionBoxColor, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontSize = 13.sp
        )
    }
}
