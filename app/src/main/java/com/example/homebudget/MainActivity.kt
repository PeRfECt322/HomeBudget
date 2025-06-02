package com.example.homebudget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var selectedPeriodDays = 30 // По умолчанию месяц

    // Для обновления после добавления операции
    private val addOperationResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadOperations()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        setupRecyclerView()
        setupPeriodButtons()
        loadOperations()

        binding.addButton.setOnClickListener {
            addOperationResult.launch(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = OperationsAdapter { operation ->
            // Обработка клика по операции (если нужно)
            showToast("Выбрана операция: ${operation.name}")
        }
        binding.operationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupPeriodButtons() {
        binding.apply {
            periodDay.setOnClickListener { setPeriod(1) }
            periodWeek.setOnClickListener { setPeriod(7) }
            periodMonth.setOnClickListener { setPeriod(30) }
            periodYear.setOnClickListener { setPeriod(365) }
            periodAll.setOnClickListener { setPeriod(0) }
        }
    }

    private fun setPeriod(days: Int) {
        selectedPeriodDays = days
        loadOperations()
        updatePeriodTitle()
    }

    private fun loadOperations() {
        val token = authManager.getToken() ?: run {
            showToastAndFinish("Требуется авторизация")
            return
        }

        val userId = authManager.getUserId()
        if (userId == -1) {
            showToastAndFinish("Ошибка: ID пользователя не найден")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val endDate = Date()
        val startDate = when (selectedPeriodDays) {
            0 -> Date(0) // Все операции
            else -> Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -selectedPeriodDays)
            }.time
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkClient.apiService.getOperations(
                    token = "Bearer $token",
                    start = dateFormat.format(startDate),
                    end = dateFormat.format(endDate),
                    userId = userId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.operations?.let { operations ->
                            adapter.submitList(operations)
                            updateBalance(operations)
                        }
                    } else {
                        showToast("Ошибка загрузки: ${response.errorBody()?.string()}")
                        if (response.code() == 401) {
                            authManager.clearAuth()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun updatePeriodTitle() {
        binding.periodTitle.text = when (selectedPeriodDays) {
            1 -> "За сегодня"
            7 -> "За неделю"
            30 -> "За месяц"
            365 -> "За год"
            0 -> "Все операции"
            else -> "Операции"
        }
    }

    private fun updateBalance(operations: List<Operation>) {
        val (income, expense) = operations.partition { it.category == "in" }
        val balance = income.sumOf { it.sum } - expense.sumOf { it.sum }
        binding.balanceTextView.text = "Баланс: ${balance} ₽"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastAndFinish(message: String) {
        showToast(message)
        finish()
    }
}