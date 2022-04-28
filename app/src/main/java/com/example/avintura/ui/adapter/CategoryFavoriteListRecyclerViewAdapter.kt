package com.example.avintura.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.ui.viewholder.CategoryItemViewHolder

class CategoryFavoriteListRecyclerViewAdapter(
    private val businesses: List<AvinturaCategoryBusiness>,
    private val context: Context,
    private val onBusinessClickListener: OnBusinessClickListener
) : RecyclerView.Adapter<CategoryItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        return CategoryItemViewHolder.create(parent, onBusinessClickListener)
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        val business = businesses[position]
        holder.bind(business, context)
    }

    override fun getItemCount(): Int {
        return businesses.size
    }

    interface OnBusinessClickListener {
        fun onBusinessClick(id: String, name: String, position: Int)
        //fun onFavoriteClick(position: Int)
    }
}