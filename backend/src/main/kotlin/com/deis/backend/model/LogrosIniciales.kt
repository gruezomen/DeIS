package com.deis.backend.model

object LogrosIniciales {
    val lista = listOf(
        Logro(
            codigo = "PRIMERA_PRACTICA",
            titulo = "Primer paso",
            descripcion = "Completa por primera vez la práctica general.",
            tipo = "PRACTICA_COMPLETADA",
            condicionValor = 1
        ),
        Logro(
            codigo = "CINCO_PRACTICAS",
            titulo = "Constante",
            descripcion = "Completa 5 veces la práctica general.",
            tipo = "PRACTICA_COMPLETADA",
            condicionValor = 5
        ),
        Logro(
            codigo = "PRECISION_ALTA",
            titulo = "Precisión alta",
            descripcion = "Obtén 90% o más de aciertos en la práctica general.",
            tipo = "PORCENTAJE_ACIERTOS",
            condicionValor = 90
        ),
        Logro(
            codigo = "PRIMER_SIMULACRO",
            titulo = "Primera prueba",
            descripcion = "Completa tu primer simulacro.",
            tipo = "SIMULACRO_COMPLETADO",
            condicionValor = 1
        ),
        Logro(
            codigo = "TRES_SIMULACROS",
            titulo = "Preparado",
            descripcion = "Completa 3 simulacros.",
            tipo = "SIMULACRO_COMPLETADO",
            condicionValor = 3
        ),
        Logro(
            codigo = "RACHA_3_DIAS",
            titulo = "En racha",
            descripcion = "Practica durante 3 días seguidos.",
            tipo = "RACHA_DIAS",
            condicionValor = 3
        )
    )
}