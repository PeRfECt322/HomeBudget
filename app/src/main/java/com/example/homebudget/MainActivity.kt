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
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import kotlin.math.log

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
        val parts = token.split(".")
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val payloadJson = JSONObject(String(decodedBytes))
        val username = payloadJson.optString("username", null)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val endDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, +1)
        }.time
        val startDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.time

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uId = NetworkClient.apiService.getUserId(GetUserId(username))
                val response = NetworkClient.apiService.getOperations(
                    token = "Bearer $token",
                    GetRequest(
                        start = dateFormat.format(startDate),
                        end = dateFormat.format(endDate),
                        user_id = uId.body() ?: 0
                        //Заменить на реальный айдишник
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