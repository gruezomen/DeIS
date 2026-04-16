package com.deis.backend.repository

import com.deis.backend.model.Preuniversitario
import org.springframework.data.mongodb.repository.MongoRepository

interface PreuniversitarioRepository : MongoRepository<Preuniversitario, String>