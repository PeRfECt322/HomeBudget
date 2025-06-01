package com.example.homebudget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.homebudget.databinding.ItemTransactionBinding

class TransactionAdapter(private var transactions: List<Transaction>) :
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
            titleTextView.text = transaction.title
            amountTextView.text = "%.2f руб.".format(transaction.amount)
            dateTextView.text = transaction.date
            typeTextView.text = if (transaction.type == "income") "Доход" else "Расход"

            val color = if (transaction.type == "income") {
                ContextCompat.getColor(root.context, android.R.color.holo_green_light)
            } else {
                ContextCompat.getColor(root.context, android.R.color.holo_red_light)
            }
            amountTextView.setTextColor(color)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}