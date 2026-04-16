package com.deis.backend.repository

import com.deis.backend.model.Simulacro
import org.springframework.data.mongodb.repository.MongoRepository

interface SimulacroRepository : MongoRepository<Simulacro, String>