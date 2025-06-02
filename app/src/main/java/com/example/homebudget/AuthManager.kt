package com.example.homebudget

import android.content.Context
import android.content.SharedPreferences

class AuthManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "AuthPrefs"
        private const val TOKEN_KEY = "AUTH_TOKEN"
        private const val USER_ID_KEY = "USER_ID"
        private const val DEFAULT_USER_ID = -1
    }

    private val sharedPref: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Сохранение данных авторизации
     * @param token JWT токен
     * @param userId ID пользователя
     */
    fun saveAuthData(token: String, userId: Int) {
        sharedPref.edit().apply {
            putString(TOKEN_KEY, token)
            putInt(USER_ID_KEY, userId)
            apply() // Используем apply() для асинхронного сохранения
        }
    }

    /**
     * Получение токена авторизации
     * @return JWT токен или null если не авторизован
     */
    fun getToken(): String? {
        return sharedPref.getString(TOKEN_KEY, null)
    }

    /**
     * Получение ID пользователя
     * @return ID пользователя или -1 если не авторизован
     */
    fun getUserId(): Int {
        return sharedPref.getInt(USER_ID_KEY, DEFAULT_USER_ID)
    }

    /**
     * Проверка авторизации пользователя
     * @return true если пользователь авторизован
     */
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    /**
     * Очистка данных авторизации (выход)
     */
    fun clearAuth() {
        sharedPref.edit().apply {
            remove(TOKEN_KEY)
            remove(USER_ID_KEY)
            apply()
        }
    }

    /**
     * Получение авторизационного заголовка
     * @return Строка для заголовка Authorization или null
     */
    fun getAuthHeader(): String? {
        return getToken()?.let { "Bearer $it" }
    }
}