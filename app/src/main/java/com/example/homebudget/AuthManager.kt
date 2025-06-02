package com.example.homebudget

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    fun saveUserData(token: String, userId: Int) {
        sharedPref.edit()
            .putString("AUTH_TOKEN", token)
            .putInt("USER_ID", userId)
            .apply()
    }

    fun getToken(): String? {
        return sharedPref.getString("AUTH_TOKEN", null)
    }

    fun getUserId(): Int? {
        return if (sharedPref.contains("USER_ID")) {
            sharedPref.getInt("USER_ID", 0)
        } else {
            null
        }
    }

    fun clearAuth() {
        sharedPref.edit()
            .remove("AUTH_TOKEN")
            .remove("USER_ID")
            .apply()
    }
}