package com.example.homebudget
// Модели для запросов и ответов
data class LoginRequest(val username: String, val password: String)

data class OperationsResponse(val operations: List<Operation>)

data class Operation(
    val Id: Int,
    val Name: String,
    val Date: String,
    val UserId: Int,
    val Category: String,
    val Sum: Int
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

data class CreateOperationResponse(val id: Int)