package com.example.act_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

interface AuthApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Void>
}
