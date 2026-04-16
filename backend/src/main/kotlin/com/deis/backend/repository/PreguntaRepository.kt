package com.deis.backend.repository

import com.deis.backend.model.Pregunta
import org.springframework.data.mongodb.repository.MongoRepository

interface PreguntaRepository : MongoRepository<Pregunta, String>