package com.deis.backend.repository

import com.deis.backend.model.Logro
import org.springframework.data.mongodb.repository.MongoRepository

interface LogroRepository : MongoRepository<Logro, String> {
    fun findByCodigo(codigo: String): Logro?
}