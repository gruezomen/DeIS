package com.deis.backend.repository

import com.deis.backend.model.Administrador
import org.springframework.data.mongodb.repository.MongoRepository

interface AdministradorRepository : MongoRepository<Administrador, String>