package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "preuniversitarios")
data class Preuniversitario(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val fechaRegistro: LocalDate = LocalDate.now(),
    val facultad: Facultad,
    val estadistica: Estadistica = Estadistica(),
    val racha: Racha = Racha()
)