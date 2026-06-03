package com.conference.deis.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conference.deis.R
import com.conference.deis.ui.theme.BlueBackground
import com.conference.deis.ui.theme.FieldBackground

enum class EstadoRachaDelfin {
    DORMIDO,
    DESPIERTO,
    SONRIENTE
}

@DrawableRes
private fun imagenPorEstadoRacha(estado: EstadoRachaDelfin): Int {
    return when (estado) {
        EstadoRachaDelfin.DORMIDO -> R.drawable.delfin_dormido
        EstadoRachaDelfin.DESPIERTO -> R.drawable.delfin_despierto
        EstadoRachaDelfin.SONRIENTE -> R.drawable.delfin_sonriente
    }
}

private fun tituloPorEstadoRacha(estado: EstadoRachaDelfin): String {
    return when (estado) {
        EstadoRachaDelfin.DORMIDO -> "Racha inactiva"
        EstadoRachaDelfin.DESPIERTO -> "Racha activada"
        EstadoRachaDelfin.SONRIENTE -> "Racha en progreso"
    }
}

private fun mensajePorEstadoRacha(estado: EstadoRachaDelfin, diasRacha: Int): String {
    return when (estado) {
        EstadoRachaDelfin.DORMIDO -> "Completa una práctica para despertar tu racha."
        EstadoRachaDelfin.DESPIERTO -> "Buen inicio. Ya activaste tu racha de hoy."
        EstadoRachaDelfin.SONRIENTE -> "Excelente. Llevas $diasRacha días de racha."
    }
}

fun obtenerEstadoRachaDelfin(
    diasRacha: Int,
    rachaActivadaHoy: Boolean
): EstadoRachaDelfin {
    return when {
        diasRacha <= 0 -> EstadoRachaDelfin.DORMIDO
        rachaActivadaHoy && diasRacha == 1 -> EstadoRachaDelfin.DESPIERTO
        rachaActivadaHoy && diasRacha >= 2 -> EstadoRachaDelfin.SONRIENTE
        else -> EstadoRachaDelfin.DORMIDO
    }
}

@Composable
fun RachaDelfinCard(
    diasRacha: Int,
    rachaActivadaHoy: Boolean,
    modifier: Modifier = Modifier
) {
    val estado = obtenerEstadoRachaDelfin(
        diasRacha = diasRacha,
        rachaActivadaHoy = rachaActivadaHoy
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imagenPorEstadoRacha(estado)),
                contentDescription = tituloPorEstadoRacha(estado),
                modifier = Modifier.size(120.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = tituloPorEstadoRacha(estado),
                    color = BlueBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = mensajePorEstadoRacha(estado, diasRacha),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}