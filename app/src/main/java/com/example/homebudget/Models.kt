package com.example.homebudget

data class LoginRequest(val username: String, val password: String)

data class OperationsResponse(val operations: List<Operation> = emptyList())

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

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class GetRequest(
    val start: String,
    val end: String,
    val user_id: Int
)

data class GetUserId(
    val username: String,
)

data class CreateOperationResponse(val id: Int)