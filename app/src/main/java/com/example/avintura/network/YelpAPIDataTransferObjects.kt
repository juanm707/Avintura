package com.example.avintura.network

import com.example.avintura.database.Business
import com.squareup.moshi.Json

data class YelpBusinessContainer(
    val total: Int,
    @Json(name = "businesses") var businesses: List<YelpBusiness>
)

data class YelpBusiness(
    val id: String,
    val name: String,
    val rating: Float,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "review_count") val reviewCount: Int
)

/**
 * Convert Network results to database objects
 */
fun YelpBusinessContainer.asDatabaseModel(): List<Business> {
    return businesses.map {
        Business(
            it.id,
            it.name,
            it.rating,
            it.imageUrl,
            it.reviewCount
        )
    }
}