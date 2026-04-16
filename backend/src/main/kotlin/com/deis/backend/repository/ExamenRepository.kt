package com.deis.backend.repository

import com.deis.backend.model.Examen
import org.springframework.data.mongodb.repository.MongoRepository

interface ExamenRepository : MongoRepository<Examen, String>