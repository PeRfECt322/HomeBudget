package com.example.homebudget

import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        // Настройка Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.typeSpinner.adapter = adapter
        }

        binding.saveButton.setOnClickListener {
            val name = binding.titleEditText.text.toString()
            val amountText = binding.amountEditText.text.toString()
            val category = if (binding.typeSpinner.selectedItemPosition == 0) "in" else "out"

            if (name.isEmpty() || amountText.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            val amount = amountText.toIntOrNull() ?: run {
                showToast("Введите корректную сумму")
                return@setOnClickListener
            }

            createOperation(name, category, amount)
        }
    }

    private fun createOperation(name: String, category: String, sum: Int) {
        val token = authManager.getToken() ?: run {
            showToast("Требуется авторизация")
            finish()
            return
        }
        val parts = token.split(".")
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val payloadJson = JSONObject(String(decodedBytes))
        val username = payloadJson.optString("username", null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uId = NetworkClient.apiService.getUserId(GetUserId(username))
                val request = CreateOperationRequest(
                    name = name,
                    category = category,
                    sum = sum,
                    user_id = uId.body() ?: 0
                )

                val response = NetworkClient.apiService.createOperation(
                    "Bearer $token",
                    request
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("Операция добавлена (ID: ${response.body()?.id})")
                        finish()
                    } else {
                        showToast("Ошибка: ${response.message()}")
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