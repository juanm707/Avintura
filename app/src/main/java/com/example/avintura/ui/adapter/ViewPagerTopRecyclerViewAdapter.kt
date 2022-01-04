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
import com.example.avintura.util.getStarRatingRegularDrawable


class ViewPagerTopRecyclerViewAdapter(private val businesses: List<AvinturaBusiness>,
                                      private val context: Context,
                                      private val onBusinessClickListener: OnBusinessClickListener) : RecyclerView.Adapter<ViewPagerTopRecyclerViewAdapter.TopBusinessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopBusinessViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_top_item, parent, false)
        return TopBusinessViewHolder(layout, onBusinessClickListener)
    }

    override fun onBindViewHolder(holder: TopBusinessViewHolder, position: Int) {
        val business = businesses[position]
        holder.name.text = business.name
        holder.starRating.setImageDrawable(business.rating.getStarRatingRegularDrawable(context))
        holder.reviewCount.text = "${business.reviewCount} Reviews"
        holder.picture.load(business.imageUrl) {
            crossfade(true)
            crossfade(500)
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


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onBusinessClickListener.onBusinessClick(adapterPosition)
        }
    }

    interface OnBusinessClickListener {
        fun onBusinessClick(position: Int)
    }
}