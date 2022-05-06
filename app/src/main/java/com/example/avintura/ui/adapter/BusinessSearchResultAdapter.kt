package com.example.avintura.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.avintura.R
import com.example.avintura.database.Business

class BusinessSearchResultAdapter : RecyclerView.Adapter<BusinessSearchResultAdapter.BusinessSearchResultViewHolder>() {
    private var results: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessSearchResultViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.search_row_result, parent, false)
        return BusinessSearchResultViewHolder(layout)
    }

    override fun onBindViewHolder(holder: BusinessSearchResultViewHolder, position: Int) {
        val business = results[position]
        holder.name.text = business
    }

    override fun getItemCount(): Int {
        return results.size
    }

    class BusinessSearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.search_result_name)
    }

    fun setData(newData: List<String>) {
        results = newData
        notifyDataSetChanged()
    }
}