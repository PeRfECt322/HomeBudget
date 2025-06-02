package com.example.homebudget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityLoginBinding
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        if (authManager.isLoggedIn()) {
            startMainActivity()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            when {
                username.isEmpty() -> showToast("Введите имя пользователя")
                password.isEmpty() -> showToast("Введите пароль")
                else -> loginUser(username, password)
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.login(LoginRequest(username, password))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { loginResponse ->
                            authManager.saveAuthData(
                                token = loginResponse.token,
                                userId = loginResponse.userId
                            )
                            startMainActivity()
                        } ?: showToast("Ошибка: пустой ответ сервера")
                    } else {
                        showToast(response.errorBody()?.string() ?: "Ошибка авторизации")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Модели данных должны быть в отдельном файле (например, models.kt)
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String? = null
)

class AuthManager(private val context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    fun saveAuthData(token: String, userId: Int) {
        sharedPref.edit().apply {
            putString("AUTH_TOKEN", token)
            putInt("USER_ID", userId)
            apply()
        }
    }

    fun getToken(): String? = sharedPref.getString("AUTH_TOKEN", null)

    fun getUserId(): Int = sharedPref.getInt("USER_ID", -1)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clearAuth() {
        sharedPref.edit().apply {
            remove("AUTH_TOKEN")
            remove("USER_ID")
            apply()
        }
    }
}

// Интерфейс API (обычно в отдельном файле ApiService.kt)


// NetworkClient (обычно в отдельном файле)
object NetworkClient {
    lateinit var apiService: ApiService
    // Инициализация Retrofit должна быть здесь
}