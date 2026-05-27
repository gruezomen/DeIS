package com.deis.backend.model

object RecompensasIniciales {
    val lista = listOf(
        Recompensa(
            codigo = "RECOMPENSA_BAJA",
            titulo = "Sigue intentando",
            descripcion = "Obtuviste un resultado bajo, pero cada práctica cuenta.",
            tipo = "BAJO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_MEDIA",
            titulo = "Vas por buen camino",
            descripcion = "Obtuviste un resultado medio. Estás mejorando.",
            tipo = "MEDIO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_ALTA",
            titulo = "Excelente resultado",
            descripcion = "Obtuviste un resultado alto. Sigue así.",
            tipo = "ALTO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_RACHA",
            titulo = "Constancia activa",
            descripcion = "Mantienes una racha activa de prácticas.",
            tipo = "RACHA"
        )
    )
}