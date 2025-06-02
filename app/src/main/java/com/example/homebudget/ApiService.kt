package com.example.homebudget

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<String> // Получаем JWT токен
    @POST("register") // Уточните endpoint для регистрации
    suspend fun register(@Body request: RegisterRequest): Response<String>

    @POST("getAllOperations")
    suspend fun getOperations(
        @Header("Authorization") token: String,
        @Body request: GetRequest
    ): Response<OperationsResponse>

    @POST("user")
    suspend fun getUserId(
        @Body request: GetUserId
    ): Response<Int>

    @POST("createOperation")
    suspend fun createOperation(
        @Header("Authorization") token: String,
        @Body request: CreateOperationRequest
    ): Response<CreateOperationResponse>
}