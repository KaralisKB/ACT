package com.example.act.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val success: Boolean, val message: String, val token: String?)

interface AuthApiService {
    @POST("api/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<AuthResponse>
}
