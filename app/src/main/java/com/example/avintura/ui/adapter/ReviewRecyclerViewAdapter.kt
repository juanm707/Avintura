package com.example.avintura.ui.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.avintura.R
import com.example.avintura.domain.AvinturaReview
import com.example.avintura.util.getStarRatingSmallDrawable


class ReviewRecyclerViewAdapter(private val reviews: List<AvinturaReview>, private val context: Context) : RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewItemHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewItemHolder(layout)
    }

    override fun onBindViewHolder(holder: ReviewItemHolder, position: Int) {
        val reviewContent = reviews[position]
        holder.profilePic.load(reviewContent.imageUrl ?: "https://cdn-icons-png.flaticon.com/512/3237/3237472.png") {
            crossfade(500)
            crossfade(true)
        }
        holder.profileName.text = reviewContent.name
        holder.starRating.setImageDrawable(reviewContent.rating.getStarRatingSmallDrawable(context))
        holder.reviewText.text = reviewContent.text
        holder.itemView.setOnClickListener {
            val webpage: Uri = Uri.parse(reviewContent.url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            context.startActivity(Intent.createChooser(intent, "Open with"))
        }
        holder.dateReviewed.text = getReviewData(reviewContent.timeCreated)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ReviewItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        val profileName: TextView = itemView.findViewById(R.id.profileName)
        val starRating: ImageView = itemView.findViewById(R.id.starRating)
        val dateReviewed: TextView = itemView.findViewById(R.id.dateReviewed)
        val reviewText: TextView = itemView.findViewById(R.id.review)
    }

    private fun getReviewData(timeCreated: String): String {
        // "2016-08-29 00:41:13"
        val keep = timeCreated.take(10)
        val year = keep.take(4)
        val dayAndMonth = keep.removeRange(0, 5)
        return "$dayAndMonth-$year"
    }
}
