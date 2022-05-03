package com.example.avintura.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.avintura.R
import com.example.avintura.database.CategoryBusinessWithFavoriteStatus
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.network.YelpBusiness
import com.example.avintura.ui.adapter.CategoryFavoriteListRecyclerViewAdapter
import com.example.avintura.util.getStarRatingRegularDrawable
import com.example.avintura.util.metersToMiles


class CategoryItemViewHolder(itemView: View, private val onBusinessClickListener: CategoryFavoriteListRecyclerViewAdapter.OnBusinessClickListener) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.business_name)
    val picture: ImageView = itemView.findViewById(R.id.business_image)
    val starRating: ImageView = itemView.findViewById(R.id.business_rating)
    val reviewCount: TextView = itemView.findViewById(R.id.review_count)
    val city: TextView = itemView.findViewById(R.id.business_city)
    val priceAndMiles: TextView = itemView.findViewById(R.id.price_and_miles)

    fun bind(business: YelpBusiness, context: Context) {
        itemView.setOnClickListener {
            onBusinessClickListener.onBusinessClick(business.id, business.name, absoluteAdapterPosition)
        }
        bindViews(
            business.name,
            business.imageUrl,
            business.price,
            business.distance,
            business.rating,
            business.reviewCount,
            business.location.city,
            context
        )
    }

    fun bind(business: AvinturaCategoryBusiness, context: Context) {
        itemView.setOnClickListener {
            onBusinessClickListener.onBusinessClick(business.businessBasic.id, business.businessBasic.name, absoluteAdapterPosition)
        }
        bindViews(
            business.businessBasic.name,
            business.businessBasic.imageUrl,
            business.price,
            business.distance,
            business.businessBasic.rating,
            business.businessBasic.reviewCount,
            business.businessBasic.city,
            context
        )
    }

    private fun bindViews(
        bName: String,
        bImageUrl: String,
        bPrice: String?,
        bDistance: Float?,
        bRating: Float,
        bReviewCount: Int,
        bCity: String,
        context: Context
    ) {
        name.text = bName
        picture.load(bImageUrl) {
            crossfade(true)
            crossfade(500)
            error(R.drawable.ic_baseline_broken_image_24)
        }
        val price = if (bPrice == "") {
            "No Price"
        } else
            bPrice

        var dist = "Distance n/a"
        if (bDistance != null)
            dist = metersToMiles(bDistance)
        priceAndMiles.text = "$price • ${dist}"

        starRating.setImageDrawable(bRating.getStarRatingRegularDrawable(context))
        reviewCount.text = "$bReviewCount"
        city.text = bCity
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onBusinessClickListener: CategoryFavoriteListRecyclerViewAdapter.OnBusinessClickListener
        ): CategoryItemViewHolder {
            val layout = LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)
            return CategoryItemViewHolder(layout, onBusinessClickListener)
        }
    }
}