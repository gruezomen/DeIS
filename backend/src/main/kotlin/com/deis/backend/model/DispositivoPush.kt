package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "dispositivos_push")
data class DispositivoPush(
    @Id
    val id: String? = null,
    val usuarioId: String,
    val tokenFcm: String,
    val plataforma: String = "ANDROID",
    val activo: Boolean = true,
    val fechaRegistro: String = LocalDateTime.now().toString()
)