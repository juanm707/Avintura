package com.example.avintura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.avintura.R
import com.example.avintura.database.Business
import com.example.avintura.domain.SearchViewItem
import com.example.avintura.network.Category
import com.example.avintura.network.Term
import com.example.avintura.network.YelpAutocompleteBusiness

class BusinessSearchResultAdapter(private val context: Context) : RecyclerView.Adapter<BusinessSearchResultAdapter.BusinessSearchResultViewHolder>() {
    private var results: List<SearchViewItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessSearchResultViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.search_row_result, parent, false)
        return BusinessSearchResultViewHolder(layout)
    }

    override fun onBindViewHolder(holder: BusinessSearchResultViewHolder, position: Int) {
        val item = results[position]
        holder.name.text = item.getString()
        when (item) {
            is YelpAutocompleteBusiness -> {
                AppCompatResources.getDrawable(context, R.drawable.ic_baseline_location_city_24)
                    ?.let {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(it),
                            context.getColor(R.color.viridian_green)
                        )
                        holder.icon.setImageDrawable(it)
                    }
            }
            is Term -> {
                AppCompatResources.getDrawable(context, R.drawable.ic_baseline_text_snippet_24)
                    ?.let {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(it),
                            context.getColor(R.color.rufous)
                        )
                        holder.icon.setImageDrawable(it)
                    }
            }
            is Category -> {
                AppCompatResources.getDrawable(context, R.drawable.ic_baseline_category_24)
                    ?.let {
                        DrawableCompat.setTint(
                            DrawableCompat.wrap(it),
                            context.getColor(R.color.gamboge)
                        )
                        holder.icon.setImageDrawable(it)
                    }
            }
        }
    }

    // TODO separate by type header view type
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    class BusinessSearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.search_result_name)
        val icon: ImageView = itemView.findViewById(R.id.search_result_icon)
    }

    fun setData(newData: List<SearchViewItem>) {
        results = newData
        notifyDataSetChanged()
    }
}