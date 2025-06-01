package com.example.homebudget

class BudgetApi {
    private val users = mutableListOf<User>()
    private val transactions = mutableListOf<Transaction>()

    fun register(user: User): Boolean {
        if (users.any { it.email == user.email }) {
            return false
        }
        users.add(user)
        return true
    }

    fun login(email: String, password: String): Boolean {
        return users.any { it.email == email && it.password == password }
    }

    fun addTransaction(transaction: Transaction): Boolean {
        transactions.add(transaction)
        return true
    }

    fun getDailyTransactions(date: String): List<Transaction> {
        return transactions.filter { it.date == date }
    }
}