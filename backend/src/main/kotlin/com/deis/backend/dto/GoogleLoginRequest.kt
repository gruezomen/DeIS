package com.deis.backend.dto

import jakarta.validation.constraints.NotBlank

data class GoogleLoginRequest(
    @field:NotBlank(message = "El ID Token es obligatorio")
    val idToken: String
)
