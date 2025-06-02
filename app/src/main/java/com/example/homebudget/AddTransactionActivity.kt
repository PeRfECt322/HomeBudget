package com.example.homebudget

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        setupSpinner()
        setupSaveButton()
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.typeSpinner.adapter = adapter
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val name = binding.titleEditText.text.toString().trim()
            val amountText = binding.amountEditText.text.toString().trim()
            val category = if (binding.typeSpinner.selectedItemPosition == 0) "in" else "out"

            when {
                name.isEmpty() -> showToast("Введите название")
                amountText.isEmpty() -> showToast("Введите сумму")
                else -> {
                    val amount = amountText.toIntOrNull() ?: run {
                        showToast("Некорректная сумма")
                        return@setOnClickListener
                    }
                    createOperation(name, category, amount)
                }
            }
        }
    }

    private fun createOperation(name: String, category: String, sum: Int) {
        val token = authManager.getToken() ?: run {
            showToastAndFinish("Требуется авторизация")
            return
        }

        val userId = authManager.getUserId()
        if (userId == -1) {
            showToastAndFinish("Ошибка: ID пользователя не найден")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.createOperation(
                    "Bearer $token",
                    CreateOperationRequest(name, category, sum, userId)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showToast("Ошибка: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastAndFinish(message: String) {
        showToast(message)
        finish()
    }
}