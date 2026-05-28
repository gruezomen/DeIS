package com.conference.deis.network

import com.conference.deis.network.model.*
import retrofit2.Response
import retrofit2.http.*
import com.conference.deis.network.model.DetalleRespuestaIntentoResponse

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

    @POST("api/simulacros")
    suspend fun crearSimulacro(
        @Body request: CrearSimulacroRequest
    ): Response<Simulacro>

    @GET("api/simulacros/{id}")
    suspend fun obtenerSimulacroPorId(
        @Path("id") id: String
    ): Response<Simulacro>

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
}
