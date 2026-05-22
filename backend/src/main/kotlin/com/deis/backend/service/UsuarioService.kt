package com.deis.backend.service

import com.deis.backend.dto.*
import com.deis.backend.model.Usuario
import com.deis.backend.repository.UsuarioRepository
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.Collections
import com.deis.backend.model.Facultad
import com.deis.backend.model.Preuniversitario
import com.deis.backend.repository.PreuniversitarioRepository

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val preuniversitarioRepository: PreuniversitarioRepository,
    @org.springframework.beans.factory.annotation.Value("\${google.client.id}")
    private val googleClientId: String
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    private val verifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build()
    }

    fun autenticarConGoogle(request: GoogleLoginRequest): LoginUsuarioResponse {
        val idToken = verifier.verify(request.idToken)
            ?: throw IllegalArgumentException("Token de Google inválido")
        val payload = idToken.payload
        val email = payload.email
        val nombre = payload["name"] as String? ?: "Usuario Google"

        var usuario = usuarioRepository.findByGmail(email)

        if (usuario == null) {
            // Registro automático si no existe
            usuario = usuarioRepository.save(
                Usuario(
                    nombre = nombre,
                    apellido = "",
                    gmail = email,
                    contrasena = "", // No necesaria para usuarios de Google
                    rol = "PREUNIVERSITARIO",
                    facultadesIds = emptyList() // El usuario deberá completarlas después en su perfil
                )
            )
        }
        
        // Asegurar que tenga perfil preuniversitario
        crearPerfilPreuniversitarioSiNoExiste(usuario.id)

        return LoginUsuarioResponse(
            id = usuario.id,
            nombre = usuario.nombre,
            apellido = usuario.apellido,
            gmail = usuario.gmail,
            rol = usuario.rol,
            facultadesIds = usuario.facultadesIds,
            mensaje = "Autenticación con Google exitosa"
        )
    }

    fun registrarUsuario(request: RegistroUsuarioRequest): RegistroUsuarioResponse {
        val gmailNormalizado = request.correo.trim().lowercase()

        if (usuarioRepository.existsByGmail(gmailNormalizado)) {
            throw IllegalArgumentException("Ya existe un usuario registrado con ese correo")
        }

        val usuarioGuardado = usuarioRepository.save(
            Usuario(
                nombre = request.nombre.trim(),
                apellido = "",
                gmail = gmailNormalizado,
                contrasena = passwordEncoder.encode(request.contrasena),
                rol = "PREUNIVERSITARIO",
                facultadesIds = request.facultadesIds
            )
        )
        crearPerfilPreuniversitarioSiNoExiste(usuarioGuardado.id)

        return RegistroUsuarioResponse(
            id = usuarioGuardado.id,
            nombre = usuarioGuardado.nombre,
            apellido = usuarioGuardado.apellido,
            gmail = usuarioGuardado.gmail,
            rol = usuarioGuardado.rol,
            facultadesIds = usuarioGuardado.facultadesIds,
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
            facultadesIds = usuario.facultadesIds,
            mensaje = "Inicio de sesión exitoso"
        )
    }

    private fun crearPerfilPreuniversitarioSiNoExiste(usuarioId: String?) {
        if (usuarioId.isNullOrBlank()) return

        val perfilExistente = preuniversitarioRepository.findByUsuarioId(usuarioId)

        if (perfilExistente != null) return

        preuniversitarioRepository.save(
            Preuniversitario(
                usuarioId = usuarioId,
                facultad = Facultad(
                    nombre = "Ciencias y Tecnología"
                )
            )
        )
    }

    fun actualizarPerfil(id: String, request: ActualizarUsuarioRequest): RegistroUsuarioResponse {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (request.facultadesIds.isEmpty()) {
            throw IllegalArgumentException("Debe seleccionar al menos una facultad")
        }

        val nuevaContrasena = if (!request.contrasena.isNullOrBlank()) {
            passwordEncoder.encode(request.contrasena)
        } else {
            usuario.contrasena
        }

        val usuarioActualizado = usuarioRepository.save(
            usuario.copy(
                nombre = request.nombre.trim(),
                apellido = "",
                contrasena = nuevaContrasena,
                facultadesIds = request.facultadesIds
            )
        )

        return RegistroUsuarioResponse(
            id = usuarioActualizado.id,
            nombre = usuarioActualizado.nombre,
            apellido = usuarioActualizado.apellido,
            gmail = usuarioActualizado.gmail,
            rol = usuarioActualizado.rol,
            facultadesIds = usuarioActualizado.facultadesIds,
            mensaje = "Perfil actualizado correctamente"
        )
    }
}
