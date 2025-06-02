package com.example.homebudget

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Модели для запросов и ответов
data class LoginRequest(val username: String, val password: String)

data class OperationsResponse(val operations: List<Operation>)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String? = null
)



data class CreateOperationRequest(
    val name: String,
    val category: String,
    val sum: Int,
    val user_id: Int
)
// Добавьте в файл с моделями (или создайте новый)
data class RegisterRequest(
    val username: String,
    val email: String, // если требуется email
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Int,
    val username: String
)
data class Operation(
    val id: Int,
    val name: String,
    val date: String,
    val category: String, // "in" или "out"
    val sum: Int
)

data class OperationsResponse(
    val operations: List<Operation>
)
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

data class CreateOperationResponse(val id: Int)