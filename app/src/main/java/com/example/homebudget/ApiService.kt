package com.example.homebudget

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<String> // Получаем JWT токен
    @POST("register") // Уточните endpoint для регистрации
    suspend fun register(@Body request: RegisterRequest): Response<String>

    @GET("getAllOperations")
    suspend fun getOperations(
        @Header("Authorization") token: String,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("user_id") userId: Int
    ): Response<OperationsResponse>

    @POST("createOperation")
    suspend fun createOperation(
        @Header("Authorization") token: String,
        @Body request: CreateOperationRequest
    ): Response<CreateOperationResponse>
}