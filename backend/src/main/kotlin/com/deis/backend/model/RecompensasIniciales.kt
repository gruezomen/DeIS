package com.deis.backend.model

object RecompensasIniciales {

    val lista: List<Recompensa> = listOf(
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
            titulo = "Excelente práctica",
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
            titulo = "Gran simulacro",
            descripcion = "Tu esfuerzo se nota. Estás demostrando seguridad, preparación y muy buen desempeño.",
            tipo = "ALTO_SIMULACRO"
        ),
        Recompensa(
            codigo = "RECOMPENSA_RACHA",
            titulo = "Constancia imparable",
            descripcion = "Tu disciplina está marcando la diferencia. Seguir practicando cada día te acerca cada vez más al éxito.",
            tipo = "RACHA"
        ),

        // Medallitas de delfín
        Recompensa(
            codigo = "MEDALLA_ALETA_INICIAL",
            titulo = "Aleta Inicial",
            descripcion = "Ganaste esta medallita por mantener 2 días seguidos de racha.",
            tipo = "MEDALLA"
        ),
        Recompensa(
            codigo = "MEDALLA_SALTO_SEMANAL",
            titulo = "Salto Semanal",
            descripcion = "Ganaste esta medallita por mantener una semana completa de racha.",
            tipo = "MEDALLA"
        ),
        Recompensa(
            codigo = "MEDALLA_NADO_IMPARABLE",
            titulo = "Nado Imparable",
            descripcion = "Ganaste esta medallita por alcanzar 10 días seguidos de racha.",
            tipo = "MEDALLA"
        ),
        Recompensa(
            codigo = "MEDALLA_DELFIN_DIAMANTE",
            titulo = "Delfín Diamante",
            descripcion = "Ganaste esta medallita especial por obtener 4 simulacros con nota igual o superior a 80.",
            tipo = "MEDALLA"
        ),
        Recompensa(
          codigo = "MEDALLA_DELFIN_OCULTO",
          titulo = "Delfín Oculto",
          descripcion = "Medallita secreta desbloqueada por mantener una racha de 2 días y obtener una nota cercana a 28.",
          tipo = "MEDALLA"
        ),

        // Marcos de perfil
        Recompensa(
            codigo = "MARCO_OLA",
            titulo = "Marco Ola",
            descripcion = "Desbloqueaste este marco por obtener una nota igual o superior a 51.",
            tipo = "MARCO"
        ),
        Recompensa(
            codigo = "MARCO_CORAL",
            titulo = "Marco Coral",
            descripcion = "Desbloqueaste este marco por obtener una nota igual o superior a 80.",
            tipo = "MARCO"
        ),
        Recompensa(
            codigo = "MARCO_OCEANO_PROFUNDO",
            titulo = "Marco Océano Profundo",
            descripcion = "Desbloqueaste este marco por obtener 4 simulacros con nota igual o superior a 80.",
            tipo = "MARCO"
        ),

        // Títulos de delfín
        Recompensa(
            codigo = "TITULO_DELFIN_NOVATO",
            titulo = "Delfín Novato",
            descripcion = "Título desbloqueado por completar tu primera práctica.",
            tipo = "TITULO"
        ),
        Recompensa(
            codigo = "TITULO_NADADOR_CONSTANTE",
            titulo = "Nadador Constante",
            descripcion = "Título desbloqueado por mantener 2 días seguidos de racha.",
            tipo = "TITULO"
        ),
        Recompensa(
            codigo = "TITULO_EXPLORADOR_ARRECIFE",
            titulo = "Explorador del Arrecife",
            descripcion = "Título desbloqueado por mantener una semana completa de racha.",
            tipo = "TITULO"
        ),
        Recompensa(
            codigo = "TITULO_DELFIN_ACADEMICO",
            titulo = "Delfín Académico",
            descripcion = "Título desbloqueado por obtener una nota igual o superior a 80.",
            tipo = "TITULO"
        ),
        Recompensa(
            codigo = "TITULO_GUARDIAN_OCEANO",
            titulo = "Guardián del Océano",
            descripcion = "Título desbloqueado por obtener 4 simulacros con nota igual o superior a 80.",
            tipo = "TITULO"
        )
    )
}