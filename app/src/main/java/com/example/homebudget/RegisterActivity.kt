package com.example.homebudget

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val api = BudgetApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showToast("Пароли не совпадают")
                return@setOnClickListener
            }

            if (api.register(User(email = email, password = password))) {
                showToast("Регистрация успешна")
                finish()
            } else {
                showToast("Ошибка регистрации: email уже используется")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}