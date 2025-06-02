package com.example.homebudget

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPref.edit().putString("AUTH_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return sharedPref.getString("AUTH_TOKEN", null)
    }

    fun clearAuth() {
        sharedPref.edit().remove("AUTH_TOKEN").apply()
    }
}