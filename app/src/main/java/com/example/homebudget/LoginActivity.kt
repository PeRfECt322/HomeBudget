package com.example.homebudget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Введите логин и пароль")
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val token = response.body()!!
                    authManager.saveToken(token)

                    withContext(Dispatchers.Main) {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Неверный логин или пароль")
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