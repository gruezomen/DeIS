package com.deis.backend

import com.deis.backend.model.Facultad
import com.deis.backend.repository.FacultadRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class FacultadDataInitializer(
    private val facultadRepository: FacultadRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (facultadRepository.count() == 0L) {
            val facultades = listOf(
                Facultad(nombre = "Ciencias y Tecnología"),
                Facultad(nombre = "Medicina"),
                Facultad(nombre = "Derecho"),
                Facultad(nombre = "Economía"),
                Facultad(nombre = "Arquitectura"),
                Facultad(nombre = "Humanidades")
            )
            facultadRepository.saveAll(facultades)
            println("Facultades iniciales creadas.")
        }
    }
}
