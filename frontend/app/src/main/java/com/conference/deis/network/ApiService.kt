package com.conference.deis.network

import com.conference.deis.network.model.LoginRequest
import com.conference.deis.network.model.LoginResponse
import com.conference.deis.network.model.RegisterRequest
import com.conference.deis.network.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}