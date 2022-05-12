package com.example.avintura.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
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
import com.example.avintura.domain.Header
import com.example.avintura.domain.SearchViewItem
import com.example.avintura.network.Category
import com.example.avintura.network.Term
import com.example.avintura.network.YelpAutocompleteBusiness

class BusinessSearchResultAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var results: List<SearchViewItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0)
            SearchResultHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_row_header, parent, false))
        else
            BusinessSearchResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_row_result, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val headerViewHolder = holder as SearchResultHeaderViewHolder
                val item = results[position] as Header
                headerViewHolder.title.text = item.getString()
            }
            1 -> {
                val searchResultViewHolder = holder as BusinessSearchResultViewHolder
                val item = results[position]
                searchResultViewHolder.name.text = item.getString()
                when (item) {
                    is YelpAutocompleteBusiness -> {
                        searchResultViewHolder.icon.setImageDrawable(getIcon(R.drawable.ic_baseline_location_city_24, R.color.viridian_green))
                    }
                    is Term -> {
                        searchResultViewHolder.icon.setImageDrawable(getIcon(R.drawable.ic_baseline_text_snippet_24, R.color.rufous))
                    }
                    is Category -> {
                        searchResultViewHolder.icon.setImageDrawable(getIcon(R.drawable.ic_baseline_category_24, R.color.gamboge))
                    }
                }
            }
        }
    }

    // TODO separate by type header view type
    override fun getItemViewType(position: Int): Int {
        return if (results[position] is Header) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    class BusinessSearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.search_result_name)
        val icon: ImageView = itemView.findViewById(R.id.search_result_icon)
    }

    class SearchResultHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.header_text)
    }

    fun setData(newData: List<SearchViewItem>) {
        results = newData
        notifyDataSetChanged()
    }

    private fun getIcon(resId: Int, color: Int): Drawable? {
        val icon = AppCompatResources.getDrawable(context, resId)
        icon?.let {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(it),
                    context.getColor(color)
                )
            }
        return icon
    }
}