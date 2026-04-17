package com.deis.backend.repository

import com.deis.backend.model.Usuario
import org.springframework.data.mongodb.repository.MongoRepository

interface UsuarioRepository : MongoRepository<Usuario, String> {
    fun findByGmail(gmail: String): Usuario?
    fun existsByGmail(gmail: String): Boolean
}