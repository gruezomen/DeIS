package com.deis.backend.repository

import com.deis.backend.model.DispositivoPush
import org.springframework.data.mongodb.repository.MongoRepository

interface DispositivoPushRepository : MongoRepository<DispositivoPush, String> {
    fun findByTokenFcm(tokenFcm: String): DispositivoPush?
    fun findByUsuarioIdAndActivoTrue(usuarioId: String): List<DispositivoPush>
    fun findByUsuarioIdInAndActivoTrue(usuarioIds: List<String>): List<DispositivoPush>
}