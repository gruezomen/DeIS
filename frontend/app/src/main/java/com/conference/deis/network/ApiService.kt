package com.conference.deis.network

import okhttp3.MultipartBody
import com.conference.deis.network.model.*
import retrofit2.Response
import retrofit2.http.*
import com.conference.deis.network.model.DetalleRespuestaIntentoResponse
import com.conference.deis.network.model.ErroresPorCategoriaResponse

interface ApiService {

   @POST("api/usuarios/registro")
    suspend fun registrarUsuario(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(
        @Path("id") id: String
    ): Response<Map<String, String>>

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/usuarios/google")
    suspend fun iniciarSesionGoogle(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(
        @Path("id") id: String,
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/preguntas")
    suspend fun crearPregunta(
        @Body request: CreateQuestionRequest
    ): Response<CreateQuestionResponse>

    @GET("api/preguntas")
    suspend fun obtenerPreguntas(): Response<List<Question>>

    @GET("api/preguntas/{id}")
    suspend fun obtenerPreguntaPorId(
        @Path("id") id: String
    ): Response<Question>

    @PUT("api/preguntas/{id}")
    suspend fun actualizarPregunta(
        @Path("id") id: String,
        @Body request: CreateQuestionRequest
    ): Response<CreateQuestionResponse>

    @GET("api/bancos-preguntas")
    suspend fun obtenerBancosPreguntas(): Response<List<BancoPregunta>>

    @GET("api/bancos-preguntas/{id}")
    suspend fun obtenerBancoPorId(
        @Path("id") id: String
    ): Response<BancoPregunta>

    @GET("api/facultades")
    suspend fun obtenerFacultades(): Response<List<Facultad>>

    @POST("api/bancos-preguntas")
    suspend fun crearBanco(
        @Body request: CrearBancoRequest
    ): Response<BancoPregunta>

    @DELETE("api/bancos-preguntas/{id}")
    suspend fun eliminarBanco(
        @Path("id") id: String
    ): Response<Map<String, String>>

    @PATCH("api/preguntas/{id}/banco")
    suspend fun asociarPreguntaABanco(
        @Path("id") id: String,
        @Body request: AsociarPreguntaBancoRequest
    ): Response<Map<String, Any>>

    @POST("api/simulacros/intentos")
    suspend fun guardarIntentoSimulacro(
        @Body request: IntentoSimulacro
    ): Response<GuardarIntentoResponse>


    @GET("api/simulacros")
    suspend fun obtenerSimulacros(): Response<List<Simulacro>>

    @GET("api/simulacros/usuario/{usuarioId}")
    suspend fun obtenerSimulacrosPorUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<List<Simulacro>>

    @POST("api/simulacros")
    suspend fun crearSimulacro(
        @Body request: CrearSimulacroRequest
    ): Response<Simulacro>

    @GET("api/simulacros/{id}")
    suspend fun obtenerSimulacroPorId(
        @Path("id") id: String
    ): Response<Simulacro>

    @DELETE("api/simulacros/{id}")
    suspend fun eliminarSimulacro(
        @Path("id") id: String
    ): Response<Map<String, String>>

    @GET("api/racha/{usuarioId}")
    suspend fun obtenerRacha(
        @Path("usuarioId") usuarioId: String
    ): Response<RachaResponse>

    @POST("api/racha/{usuarioId}/practica")
    suspend fun registrarPracticaDiaria(
        @Path("usuarioId") usuarioId: String
    ): Response<RachaResponse>

    @GET("api/simulacros/intentos/usuario/{usuarioId}/comparacion-rendimiento")
    suspend fun obtenerComparacionRendimiento(
        @Path("usuarioId") usuarioId: String
    ): Response<ComparacionRendimientoResponse>

    @GET("api/simulacros/intentos/usuario/{usuarioId}")
    suspend fun obtenerIntentosPorUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<List<IntentoSimulacro>>

    @GET("api/logros/usuario/{usuarioId}")
    suspend fun obtenerLogrosUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<LogrosUsuarioResponse> 

    @GET("api/recompensas/usuario/{usuarioId}")
    suspend fun obtenerRecompensasUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<RecompensasUsuarioResponse>
        @GET("api/rendimiento-categorias/usuario/{usuarioId}")
        suspend fun obtenerRendimientoPorCategoria(
            @Path("usuarioId") usuarioId: String
        ): Response<List<RendimientoCategoriaResponse>>
    @GET("api/simulacros/intentos/usuario/{usuarioId}/historial-detallado")
    suspend fun obtenerHistorialDetallado(
        @Path("usuarioId") usuarioId: String
    ): Response<List<HistorialIntentoResponse>>

    @GET("api/simulacros/intentos/{id}/detalle")
        suspend fun obtenerDetalleIntento(
            @Path("id") id: String
        ): Response<HistorialIntentoResponse>
    @GET("api/simulacros/intentos/{id}/respuestas")
        suspend fun obtenerRespuestasDeIntento(
            @Path("id") id: String
        ): Response<List<DetalleRespuestaIntentoResponse>>
    @GET("api/simulacros/intentos/usuario/{usuarioId}/errores-por-categoria")
    suspend fun obtenerErroresPorCategoria(
        @Path("usuarioId") usuarioId: String
    ): Response<List<ErroresPorCategoriaResponse>>
    
    @GET("api/notificaciones/usuario/{usuarioId}")
    suspend fun obtenerNotificaciones(
        @Path("usuarioId") usuarioId: String
    ): Response<List<Notificacion>>

    @PATCH("api/notificaciones/{id}/leer")
    suspend fun marcarNotificacionLeida(
        @Path("id") id: String
    ): Response<Notificacion>

    @Multipart
    @PATCH("api/usuarios/{id}/foto")
    suspend fun actualizarFotoPerfil(
      @Path("id") id: String,
      @Part foto: MultipartBody.Part
    ): Response<LoginResponse>

    @DELETE("api/usuarios/{id}/foto")
      suspend fun eliminarFotoPerfil(
        @Path("id") id: String
    ): Response<LoginResponse>

    @GET("api/recompensas/usuario/{usuarioId}/equipamiento")
suspend fun obtenerEquipamiento(
    @Path("usuarioId") usuarioId: String
): Response<EquipamientoRecompensaResponse>

@PUT("api/recompensas/usuario/{usuarioId}/equipamiento")
suspend fun guardarEquipamiento(
    @Path("usuarioId") usuarioId: String,
    @Body request: EquipamientoRecompensaRequest
): Response<EquipamientoRecompensaResponse>

    @POST("api/push/token")
    suspend fun registrarPushToken(
        @Body request: RegistrarPushTokenRequest
    ): Response<RegistrarPushTokenResponse>
}
