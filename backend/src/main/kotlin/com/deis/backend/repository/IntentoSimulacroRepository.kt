package com.deis.backend.repository

import com.deis.backend.model.IntentoSimulacro
import org.springframework.data.mongodb.repository.MongoRepository

interface IntentoSimulacroRepository : MongoRepository<IntentoSimulacro, String>
