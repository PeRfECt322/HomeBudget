package com.example.homebudget

import android.app.DatePickerDialog
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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private lateinit var adapter: OperationsAdapter
    private var startDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time
    private var endDate: Date = Calendar.getInstance().apply { add(Calendar.MONTH, +1) }.time
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)
        setupRecyclerView()
        setupDateRangeButtons()
        loadOperations()

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun setupDateRangeButtons() {
        updateDateButtonsText()

        binding.startDateButton.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.endDateButton.setOnClickListener {
            showDatePickerDialog(false)
        }

        binding.applyDateRangeButton.setOnClickListener {
            loadOperations()
        }
    }

    private fun updateDateButtonsText() {
        binding.startDateButton.text = "С: ${dateFormat.format(startDate)}"
        binding.endDateButton.text = "По: ${dateFormat.format(endDate)}"
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance().apply {
            time = if (isStartDate) startDate else endDate
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }.time

                if (isStartDate) {
                    startDate = newDate
                } else {
                    endDate = newDate
                }

                // Проверка, чтобы начальная дата не была позже конечной
                if (startDate.after(endDate)) {
                    if (isStartDate) {
                        endDate = startDate
                    } else {
                        startDate = endDate
                    }
                }

                updateDateButtonsText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

        val parts = token.split(".")
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val payloadJson = JSONObject(String(decodedBytes))
        val username = payloadJson.optString("username", null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uId = NetworkClient.apiService.getUserId(GetUserId(username))
                val response = NetworkClient.apiService.getOperations(
                    token = "Bearer $token",
                    GetRequest(
                        start = apiDateFormat.format(startDate),
                        end = apiDateFormat.format(endDate),
                        user_id = uId.body() ?: 0
                    )
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
        binding.balanceTextView.text = "Баланс: ${income - expense} ₽ (${dateFormat.format(startDate)} - ${dateFormat.format(endDate)})"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastAndFinish(message: String) {
        showToast(message)
        finish()
    }
}