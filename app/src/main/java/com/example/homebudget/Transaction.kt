package com.example.homebudget

data class Transaction(
    val id: String = "",
    val date: String = "",
    val title: String,
    val type: String, // "income" или "expense"
    val amount: Double,
    val userId: String = ""
)