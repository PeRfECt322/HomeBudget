// RegisterActivity.kt
package com.example.homebudget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showToast("Пароли не совпадают")
                return@setOnClickListener
            }

            registerUser(username, email, password)
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.register(
                    RegisterRequest(username, email, password)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Вариант 1: Если API возвращает токен при регистрации
                        val token = response.body()
                        token?.let {
                            authManager.saveToken(it)
                            showToast("Регистрация успешна")
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        } ?: showToast("Ошибка: пустой ответ от сервера")

                        // Вариант 2: Если API просто возвращает сообщение об успехе
                        // showToast("Регистрация успешна")
                        // startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        // finish()
                    } else {
                        showToast("Ошибка регистрации: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}