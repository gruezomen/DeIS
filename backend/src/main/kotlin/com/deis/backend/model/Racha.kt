package com.deis.backend.model

import java.time.LocalDate

data class Racha(
    val diasConsecutivos: Int = 0,
    val ultimaPractica: LocalDate? = null
)