package com.conference.deis.network

import com.conference.deis.network.model.*
import retrofit2.Response
import retrofit2.http.*

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
    ): Response<IntentoSimulacro>

    @POST("api/simulacros")
    suspend fun crearSimulacro(
        @Body request: CrearSimulacroRequest
    ): Response<Simulacro>

    @GET("api/simulacros/{id}")
    suspend fun obtenerSimulacroPorId(
        @Path("id") id: String
    ): Response<Simulacro>


}
