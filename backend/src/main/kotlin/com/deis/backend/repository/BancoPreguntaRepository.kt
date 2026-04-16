package com.deis.backend.repository

import com.deis.backend.model.BancoPregunta
import org.springframework.data.mongodb.repository.MongoRepository

interface BancoPreguntaRepository : MongoRepository<BancoPregunta, String>