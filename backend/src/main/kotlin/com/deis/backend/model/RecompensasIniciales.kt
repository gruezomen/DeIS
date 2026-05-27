package com.deis.backend.model

object RecompensasIniciales {
    val lista = listOf(
        Recompensa(
            codigo = "RECOMPENSA_BAJA_PRACTICA",
            titulo = "No te rindas",
            descripcion = "Cada intento te acerca más a tu meta. Sigue practicando, porque mejorar también es avanzar.",
            tipo = "BAJO_PRACTICA"
        ),
        Recompensa(
            codigo = "RECOMPENSA_MEDIA_PRACTICA",
            titulo = "Vas mejorando",
            descripcion = "Lo estás haciendo bien. Mantén el esfuerzo y verás cómo cada práctica te hace más fuerte.",
            tipo = "MEDIO_PRACTICA"
        ),
        Recompensa(
            codigo = "RECOMPENSA_ALTA_PRACTICA",
            titulo = "¡Excelente práctica!",
            descripcion = "Demostraste un gran rendimiento. Sigue así, porque tu constancia está dando resultados.",
            tipo = "ALTO_PRACTICA"
        ),
        Recompensa(
            codigo = "RECOMPENSA_BAJA_SIMULACRO",
            titulo = "Sigue preparándote",
            descripcion = "Este resultado es una oportunidad para aprender. Cada simulacro te ayuda a llegar más listo al siguiente.",
            tipo = "BAJO_SIMULACRO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_MEDIA_SIMULACRO",
            titulo = "Buen avance",
            descripcion = "Vas construyendo una buena base. Sigue entrenando y podrás convertir este avance en un gran resultado.",
            tipo = "MEDIO_SIMULACRO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_ALTA_SIMULACRO",
            titulo = "¡Gran simulacro!",
            descripcion = "Tu esfuerzo se nota. Estás demostrando seguridad, preparación y muy buen desempeño.",
            tipo = "ALTO_SIMULACRO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_RACHA",
            titulo = "Constancia imparable",
            descripcion = "Tu disciplina está marcando la diferencia. Seguir practicando cada día te acerca cada vez más al éxito.",
            tipo = "RACHA"
        )
    )
}