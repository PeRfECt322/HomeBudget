package com.example.homebudget

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Авторизация
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Регистрация
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    // Операции
    @GET("operations")
    suspend fun getOperations(
        @Header("Authorization") token: String,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("userId") userId: Int
    ): Response<OperationsResponse>

    @POST("operations")
    suspend fun createOperation(
        @Header("Authorization") token: String,
        @Body request: CreateOperationRequest
    ): Response<CreateOperationResponse>

    @DELETE("operations/{id}")
    suspend fun deleteOperation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}