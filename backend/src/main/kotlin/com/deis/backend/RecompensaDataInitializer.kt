package com.deis.backend

import com.deis.backend.model.RecompensasIniciales
import com.deis.backend.repository.RecompensaRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class RecompensaDataInitializer(
    private val recompensaRepository: RecompensaRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        RecompensasIniciales.lista.forEach { recompensa ->
            val existe = recompensaRepository.findByCodigo(recompensa.codigo)

            if (existe == null) {
                recompensaRepository.save(recompensa)
                println("Recompensa inicial guardada: ${recompensa.codigo}")
            } else {
                println("Recompensa ya existente: ${recompensa.codigo}")
            }
        }
    }
}