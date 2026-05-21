package com.deis.backend.repository

import com.deis.backend.model.Facultad
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FacultadRepository : MongoRepository<Facultad, String>