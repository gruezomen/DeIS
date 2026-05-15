package com.deis.backend.repository

import com.deis.backend.model.BancoPregunta
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

interface BancoPreguntaRepository : MongoRepository<BancoPregunta, String>