package com.aioa.app.data.api

import com.aioa.app.data.model.LoginRequest
import com.aioa.app.data.model.LoginResponse
import com.aioa.app.data.model.User
import retrofit2.Response
import retrofit2.http.*

/**
 * AI-OA API service interface
 */
interface AioaApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<User>
}