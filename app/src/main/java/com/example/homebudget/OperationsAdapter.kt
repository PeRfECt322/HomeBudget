package com.example.homebudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.homebudget.databinding.ItemOperationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OperationsAdapter(
    private val onItemClick: (Operation) -> Unit
) : RecyclerView.Adapter<OperationsAdapter.ViewHolder>() {

    private var operations = emptyList<Operation>()

    inner class ViewHolder(val binding: ItemOperationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOperationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operation = operations[position]
        holder.binding.apply {
            nameTextView.text = operation.name
            amountTextView.text = "${operation.sum} ₽"
            dateTextView.text = operation.date.formatDate()
            categoryTextView.text = if (operation.category == "in") "Доход" else "Расход"

            root.setOnClickListener { onItemClick(operation) }
        }
    }

    override fun getItemCount() = operations.size

    fun submitList(newList: List<Operation>) {
        operations = newList
        notifyDataSetChanged()
    }

    private fun String.formatDate(): String {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(this)
                ?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it) }
                ?: this
        } catch (e: Exception) {
            this
        }
    }
}