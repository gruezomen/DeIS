package com.conference.deis.ui.utils

import com.conference.deis.network.model.MensajeRecompensa
import com.conference.deis.network.model.TipoRecompensa

object ProveedorRecompensas {

    fun obtenerRecompensa(porcentaje: Int): MensajeRecompensa {
        return when {
            porcentaje <= 39 -> MensajeRecompensa(
                tipo = TipoRecompensa.BAJO,
                titulo = "Sigue intentando",
                mensaje = "Cada práctica cuenta. Sigue avanzando paso a paso."
            )

            porcentaje <= 79 -> MensajeRecompensa(
                tipo = TipoRecompensa.MEDIO,
                titulo = "Vas por buen camino",
                mensaje = "Buen trabajo. Estás mejorando y cada intento suma."
            )

            else -> MensajeRecompensa(
                tipo = TipoRecompensa.ALTO,
                titulo = "Excelente resultado",
                mensaje = "Muy buen desempeño. Sigue así y mantén tu ritmo."
            )
        }
    }
}