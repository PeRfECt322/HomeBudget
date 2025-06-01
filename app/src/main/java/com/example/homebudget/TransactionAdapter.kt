package com.example.homebudget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.homebudget.databinding.ItemTransactionBinding

class TransactionAdapter(private var transactions: List<Operation>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        with(holder.binding) {
            titleTextView.text = transaction.Name
            amountTextView.text = "${transaction.Sum} ₽"
            dateTextView.text = transaction.Date
            typeTextView.text = if (transaction.Category == "in") "Доход" else "Расход"

            val color = if (transaction.Category == "in") {
                ContextCompat.getColor(root.context, android.R.color.holo_green_light)
            } else {
                ContextCompat.getColor(root.context, android.R.color.holo_red_light)
            }
            amountTextView.setTextColor(color)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Operation>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}