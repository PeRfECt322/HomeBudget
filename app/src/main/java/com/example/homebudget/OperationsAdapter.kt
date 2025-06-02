package com.example.homebudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class OperationsAdapter : RecyclerView.Adapter<OperationsAdapter.ViewHolder>() {
    private var operations = emptyList<Operation>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val amountTextView: TextView = view.findViewById(R.id.amountTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operation = operations[position]

        holder.nameTextView.text = operation.Name
        holder.amountTextView.text = "${operation.Sum} ₽"
        holder.dateTextView.text = formatDate(operation.Date)
        holder.categoryTextView.text = if (operation.Category == "in") "Доход" else "Расход"

        val color = if (operation.Category == "in") {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
        } else {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
        }
        holder.amountTextView.setTextColor(color)
    }

    override fun getItemCount() = operations.size

    fun updateOperations(newOperations: List<Operation>) {
        operations = newOperations
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}