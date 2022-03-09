package com.example.avintura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.avintura.R
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.network.YelpBusiness
import com.example.avintura.util.getStarRatingRegularDrawable
import com.example.avintura.util.metersToMiles


// TODO list adapter with diffutil or pagination
class CategoryResultListRecyclerViewAdapter(
                                            private val context: Context,
                                            private val onBusinessClickListener: ViewPagerTopRecyclerViewAdapter.OnBusinessClickListener) : PagingDataAdapter<YelpBusiness, CategoryResultListRecyclerViewAdapter.CategoryItemHolder>(YelpBusinessComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)
        return CategoryItemHolder(layout, onBusinessClickListener)
    }

    override fun onBindViewHolder(holder: CategoryItemHolder, position: Int) {
        val business = getItem(position)
        if (business != null) {
            holder.name.text = "${position + 1}. ${business.name}"
            holder.picture.load(business.imageUrl) {
                crossfade(true)
                crossfade(500)
                error(R.drawable.ic_baseline_broken_image_24)
            }
            val price = if (business.price == "") {
                "No Price"
            } else
                business.price

            var dist = "Distance n/a"
            if (business.distance != null)
                dist = metersToMiles(business.distance)
            holder.priceAndMiles.text = "$price • ${dist}"

            holder.starRating.setImageDrawable(business.rating.getStarRatingRegularDrawable(context))
            holder.reviewCount.text = "${business.reviewCount}"
            holder.city.text = "${business.location.city}"
        }
    }

    class CategoryItemHolder(itemView: View, private val onBusinessClickListener: ViewPagerTopRecyclerViewAdapter.OnBusinessClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val name: TextView = itemView.findViewById(R.id.business_name)
        val picture: ImageView = itemView.findViewById(R.id.business_image)
        val starRating: ImageView = itemView.findViewById(R.id.business_rating)
        val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        val city: TextView = itemView.findViewById(R.id.business_city)
        val priceAndMiles: TextView = itemView.findViewById(R.id.price_and_miles)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onBusinessClickListener.onBusinessClick(adapterPosition)
        }
    }

    object YelpBusinessComparator : DiffUtil.ItemCallback<YelpBusiness>() {
        override fun areItemsTheSame(oldItem: YelpBusiness, newItem: YelpBusiness) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: YelpBusiness, newItem: YelpBusiness) =
            oldItem == newItem
    }
}