package com.example.avintura.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.avintura.network.YelpBusiness
import com.example.avintura.ui.viewholder.CategoryItemViewHolder


class CategoryResultListRecyclerViewAdapter(
    private val context: Context,
    private val onBusinessClickListener: CategoryFavoriteListRecyclerViewAdapter.OnBusinessClickListener
) : PagingDataAdapter<YelpBusiness, CategoryItemViewHolder>(YelpBusinessComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        return CategoryItemViewHolder.create(parent, onBusinessClickListener)
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        val business = getItem(position)
        if (business != null) {
            holder.bind(business, context)
        }
    }

    object YelpBusinessComparator : DiffUtil.ItemCallback<YelpBusiness>() {
        override fun areItemsTheSame(oldItem: YelpBusiness, newItem: YelpBusiness): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: YelpBusiness, newItem: YelpBusiness): Boolean {
            return oldItem == newItem
        }
    }
}