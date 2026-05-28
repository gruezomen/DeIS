package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "respuestas_categoria")
data class RespuestaCategoria(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val intentoId: String? = null,
    val bancoId: String,
    val preguntaId: String,
    val categoria: String,
    val esCorrecta: Boolean,
    val tipo: String = "PRACTICA",
    val fecha: LocalDateTime = LocalDateTime.now()
)