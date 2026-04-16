package com.deis.backend.config

import com.deis.backend.model.Usuario
import com.deis.backend.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val usuarioRepository: UsuarioRepository,
    private val mongoTemplate: MongoTemplate,
    @Value("\${spring.mongodb.uri}") private val mongoUri: String
) : CommandLineRunner {

    override fun run(vararg args: String) {
        println("URI Mongo usada: $mongoUri")
        println("Base Mongo usada: ${mongoTemplate.db.name}")

        val correo = "admin_${System.currentTimeMillis()}@dels.com"

        usuarioRepository.save(
            Usuario(
                nombre = "Admin",
                apellido = "Principal",
                gmail = correo,
                contrasena = "123456",
                rol = "ADMIN"
            )
        )

        println("Usuario guardado en: ${mongoTemplate.db.name} con correo: $correo")
    }
}