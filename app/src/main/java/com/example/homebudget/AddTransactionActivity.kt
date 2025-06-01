package com.example.homebudget

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebudget.databinding.ActivityAddTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private val api = BudgetApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ArrayAdapter.createFromResource(
            this,
            R.array.transaction_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.typeSpinner.adapter = adapter
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val amountText = binding.amountEditText.text.toString()
            val type = if (binding.typeSpinner.selectedItemPosition == 0) "income" else "expense"

            if (title.isEmpty() || amountText.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull() ?: run {
                showToast("Введите корректную сумму")
                return@setOnClickListener
            }

            val transaction = Transaction(
                title = title,
                amount = amount,
                type = type,
                date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            )

            if (api.addTransaction(transaction)) {
                showToast("Транзакция добавлена")
                finish()
            } else {
                showToast("Ошибка сохранения")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}