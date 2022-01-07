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
    @Json(name = "review_count") val reviewCount: Int,
    val location: Location
)

data class Location(
    val address1: String,
    val address2: String?,
    val address3: String?,
    val city: String,
    @Json(name = "zip_code") val zipCode: String,
    val country: String,
    val state: String,
    @Json(name = "display_address") val displayAddress: List<String>
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
            it.reviewCount,
            it.location.city
        )
    }
}