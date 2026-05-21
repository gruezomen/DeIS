package com.deis.backend.dto

data class RachaResponse(
    val diasConsecutivos: Int,
    val ultimaPractica: String?,
    val estadoDelfin: String,
    val mensaje: String
)