package com.deis.backend.service

import com.deis.backend.dto.RegistroUsuarioRequest
import com.deis.backend.dto.RegistroUsuarioResponse
import com.deis.backend.model.Usuario
import com.deis.backend.repository.UsuarioRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun registrarUsuario(request: RegistroUsuarioRequest): RegistroUsuarioResponse {
        val gmailNormalizado = request.gmail.trim().lowercase()

        if (usuarioRepository.existsByGmail(gmailNormalizado)) {
            throw IllegalArgumentException("Ya existe un usuario registrado con ese correo")
        }

        val usuarioGuardado = usuarioRepository.save(
            Usuario(
                nombre = request.nombre.trim(),
                apellido = request.apellido.trim(),
                gmail = gmailNormalizado,
                contrasena = passwordEncoder.encode(request.contrasena),
                rol = "PREUNIVERSITARIO"
            )
        )

        return RegistroUsuarioResponse(
            id = usuarioGuardado.id,
            nombre = usuarioGuardado.nombre,
            apellido = usuarioGuardado.apellido,
            gmail = usuarioGuardado.gmail,
            rol = usuarioGuardado.rol,
            mensaje = "Usuario registrado correctamente"
        )
    }
}