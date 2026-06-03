package com.deis.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "facultades")
data class Facultad(
    @Id
    val id: String? = null,
    val nombre: String
)