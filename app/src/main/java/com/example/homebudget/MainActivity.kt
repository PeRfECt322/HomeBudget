package com.example.homebudget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homebudget.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private lateinit var adapter: OperationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        setupRecyclerView()
        loadOperations()

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = OperationsAdapter()
        binding.operationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun loadOperations() {
        val token = authManager.getToken() ?: run {
            showToastAndFinish("Требуется авторизация")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val endDate = Date()
        val startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.time

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.getOperations(
                    token = "Bearer $token",
                    start = dateFormat.format(startDate),
                    end = dateFormat.format(endDate),
                    userId = 1 // Замените на реальный ID пользователя
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.operations?.let { operations ->
                            adapter.updateOperations(operations)
                            updateBalance(operations)
                        }
                    } else {
                        showToast("Ошибка загрузки данных")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun updateBalance(operations: List<Operation>) {
        val income = operations.filter { it.Category == "in" }.sumOf { it.Sum }
        val expense = operations.filter { it.Category == "out" }.sumOf { it.Sum }
        binding.balanceTextView.text = "Баланс: ${income - expense} ₽"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastAndFinish(message: String) {
        showToast(message)
        finish()
    }
}