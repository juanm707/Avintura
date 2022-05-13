package com.example.avintura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.avintura.R
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.YelpBusiness
import com.example.avintura.util.*
import com.google.android.material.card.MaterialCardView


class ViewPagerTopRecyclerViewAdapter(
    private val context: Context,
    private val onBusinessClickListener: OnBusinessClickListener,
    private val businesses: List<AvinturaBusiness>) : RecyclerView.Adapter<ViewPagerTopRecyclerViewAdapter.TopBusinessViewHolder>() {

//    companion object DiffCallback : DiffUtil.ItemCallback<AvinturaBusiness>() {
//        override fun areItemsTheSame(oldItem: AvinturaBusiness, newItem: AvinturaBusiness): Boolean {
//            return oldItem.id == newItem.id && oldItem == newItem
//        }
//
//        override fun areContentsTheSame(oldItem: AvinturaBusiness, newItem: AvinturaBusiness): Boolean {
//            return ((oldItem.name == newItem.name) && (oldItem.rating == newItem.rating) &&
//                    (oldItem.city == newItem.city) && (oldItem.reviewCount == newItem.reviewCount) &&
//                    (oldItem.imageUrl == newItem.imageUrl) && (oldItem.favorite == newItem.favorite))
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopBusinessViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_top_item, parent, false)
        return TopBusinessViewHolder(layout, onBusinessClickListener)
    }

    override fun onBindViewHolder(holder: TopBusinessViewHolder, position: Int) {
        val business = businesses[position]

        holder.name.text = "${position+1}. ${business.name}"
        holder.starRating.setImageDrawable(business.rating.getStarRatingRegularDrawable(context))
        holder.reviewCount.text = "${business.reviewCount} Reviews"
        holder.city.text = business.city

        holder.picture.load(business.imageUrl) {
            crossfade(true)
            crossfade(500)
        }

        holder.favoriteIcon.setFavoriteDrawable(context, business.favorite, true)
        holder.favoriteIcon.setOnClickListener {
            holder.favoriteIcon.setFavoriteDrawable(context, business.favorite, false)
            holder.favoriteIcon.scaleHeart()
            onBusinessClickListener.onFavoriteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return businesses.size
    }

    class TopBusinessViewHolder(itemView: View, private val onBusinessClickListener: OnBusinessClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val name: TextView = itemView.findViewById(R.id.business_name)
        val picture: ImageView = itemView.findViewById(R.id.business_image)
        val starRating: ImageView = itemView.findViewById(R.id.business_rating)
        val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        val city: TextView = itemView.findViewById(R.id.business_city)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
        val mainCardView: MaterialCardView = itemView.findViewById(R.id.main_card_view)

        init {
            mainCardView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onBusinessClickListener.onBusinessClick(bindingAdapterPosition, null, null)
        }
    }

    interface OnBusinessClickListener {
        fun onBusinessClick(position: Int, id: String?, name: String?)
        fun onFavoriteClick(position: Int)
    }
}