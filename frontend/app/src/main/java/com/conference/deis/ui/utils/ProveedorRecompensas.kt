package com.conference.deis.ui.utils

import com.conference.deis.network.model.MensajeRecompensa
import com.conference.deis.network.model.TipoRecompensa

object ProveedorRecompensas {

    fun obtenerRecompensa(
        porcentaje: Int,
        tipoIntento: String
    ): MensajeRecompensa {
        return when (tipoIntento) {
            "SIMULACRO" -> {
                when {
                    porcentaje <= 39 -> MensajeRecompensa(
                        tipo = TipoRecompensa.BAJO,
                        titulo = "Sigue preparándote",
                        mensaje = "Este resultado es una oportunidad para aprender. Cada simulacro te ayuda a llegar más listo al siguiente."
                    )

                    porcentaje <= 79 -> MensajeRecompensa(
                        tipo = TipoRecompensa.MEDIO,
                        titulo = "Buen avance",
                        mensaje = "Vas construyendo una buena base. Sigue entrenando y podrás convertir este avance en un gran resultado."
                    )

                    else -> MensajeRecompensa(
                        tipo = TipoRecompensa.ALTO,
                        titulo = "¡Gran simulacro!",
                        mensaje = "Tu esfuerzo se nota. Estás demostrando seguridad, preparación y muy buen desempeño."
                    )
                }
            }

            else -> {
                when {
                    porcentaje <= 39 -> MensajeRecompensa(
                        tipo = TipoRecompensa.BAJO,
                        titulo = "No te rindas",
                        mensaje = "Cada intento te acerca más a tu meta. Sigue practicando, porque mejorar también es avanzar."
                    )

                    porcentaje <= 79 -> MensajeRecompensa(
                        tipo = TipoRecompensa.MEDIO,
                        titulo = "Vas mejorando",
                        mensaje = "Lo estás haciendo bien. Mantén el esfuerzo y verás cómo cada práctica te hace más fuerte."
                    )

                    else -> MensajeRecompensa(
                        tipo = TipoRecompensa.ALTO,
                        titulo = "¡Excelente práctica!",
                        mensaje = "Demostraste un gran rendimiento. Sigue así, porque tu constancia está dando resultados."
                    )
                }
            }
        }
    }
}