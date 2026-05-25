package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "logros_desbloqueados")
data class LogroDesbloqueado(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val logroCodigo: String,
    val fechaDesbloqueo: LocalDateTime = LocalDateTime.now()
)