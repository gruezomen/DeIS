package com.deis.backend.service

import com.deis.backend.dto.RegistroUsuarioRequest
import com.deis.backend.dto.RegistroUsuarioResponse
import com.deis.backend.dto.LoginUsuarioRequest
import com.deis.backend.dto.LoginUsuarioResponse
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
                apellido = "",
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

    fun loginUsuario(request: LoginUsuarioRequest): LoginUsuarioResponse {
        val gmailNormalizado = request.correo.trim().lowercase()

        val usuario = usuarioRepository.findByGmail(gmailNormalizado)
            ?: throw IllegalArgumentException("No existe una cuenta con ese correo")

        val contrasenaValida = passwordEncoder.matches(
            request.contrasena,
            usuario.contrasena
        )

        if (!contrasenaValida) {
            throw IllegalArgumentException("Contraseña incorrecta")
        }

        return LoginUsuarioResponse(
            id = usuario.id,
            nombre = usuario.nombre,
            apellido = usuario.apellido,
            gmail = usuario.gmail,
            rol = usuario.rol,
            mensaje = "Inicio de sesión exitoso"
        )
    }
}
