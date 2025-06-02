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
    private var selectedPeriodDays = 30 // По умолчанию показываем за 30 дней

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        setupRecyclerView()
        setupPeriodButtons()
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

    private fun setupPeriodButtons() {
        binding.periodDay.setOnClickListener { setPeriod(1) }
        binding.periodWeek.setOnClickListener { setPeriod(7) }
        binding.periodMonth.setOnClickListener { setPeriod(30) }
        binding.periodYear.setOnClickListener { setPeriod(365) }
        binding.periodAll.setOnClickListener { setPeriod(0) } // 0 = все данные
    }

    private fun setPeriod(days: Int) {
        selectedPeriodDays = days
        loadOperations()
    }

    private fun loadOperations() {
        val token = authManager.getToken() ?: run {
            showToastAndFinish("Требуется авторизация")
            return
        }

        val userId = authManager.getUserId() ?: run {
            showToastAndFinish("ID пользователя не найдено")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val endDate = Date()
        val startDate = if (selectedPeriodDays > 0) {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -selectedPeriodDays) }.time
        } else {
            // Если выбран период "Все", используем очень старую дату
            Calendar.getInstance().apply { set(1970, 0, 1) }.time
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
                            adapter.updateOperations(operations)
                            updateBalance(operations)
                            updatePeriodTitle()
                        }
                    } else {
                        showToast("Ошибка загрузки данных: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка сети: ${e.message}")
                }
            }
        }
    }

    private fun updatePeriodTitle() {
        val title = when (selectedPeriodDays) {
            1 -> "За сегодня"
            7 -> "За неделю"
            30 -> "За месяц"
            365 -> "За год"
            0 -> "Все операции"
            else -> "Операции"
        }
        binding.periodTitle.text = title
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