package com.deis.backend

import com.deis.backend.model.LogrosIniciales
import com.deis.backend.repository.LogroRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class LogroDataInitializer(
    private val logroRepository: LogroRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        LogrosIniciales.lista.forEach { logro ->
            val existe = logroRepository.findByCodigo(logro.codigo)

            if (existe == null) {
                logroRepository.save(logro)
                println("Logro inicial guardado: ${logro.codigo}")
            } else {
                println("Logro ya existente: ${logro.codigo}")
            }
        }
    }
}