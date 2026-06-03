package com.deis.backend.repository

import com.deis.backend.model.EquipamientoRecompensa
import org.springframework.data.mongodb.repository.MongoRepository

interface EquipamientoRecompensaRepository : MongoRepository<EquipamientoRecompensa, String> {
    fun findByUsuarioId(usuarioId: String): EquipamientoRecompensa?
}