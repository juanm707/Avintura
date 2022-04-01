package com.example.avintura.network

import com.example.avintura.database.*
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.util.toInt
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
    val location: Location, // for category
    val distance: Float, // for category
    val price: String = "", // for category
    val coordinates: Coordinate, // for category map
)

data class YelpBusinessDetail(
    val id: String,
    val alias: String,
    val name: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "is_claimed") val isClaimed: Boolean,
    @Json(name = "is_closed") val isClosed: Boolean,
    val url: String,
    val phone: String,
    @Json(name = "display_phone") val displayPhone: String,
    @Json(name = "review_count") val reviewCount: Int,
    val rating: Float,
    val photos: List<String>,
    val price: String = "",
    val categories: List<Category>,
    val location: Location,
    val coordinates: Coordinate,
    val hours: List<Hours>?,
    val transactions: List<String>?,
    @Json(name = "special_hours") val specialHours: List<SpecialHour>?
)

data class Category(
    val alias: String,
    val title: String
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

data class Hours(
    val open: List<Open>,
    @Json(name = "hours_type") val hoursType: String,
    @Json(name = "is_open_now") val isOpenNow: Boolean
)

data class SpecialHour(
    val date: String?,
    @Json(name = "is_closed") val isClosed: Boolean?,
    val start: String?,
    val end: String?,
    @Json(name = "is_overnight") val isOvernight: Boolean?
)

data class Location(
    val address1: String?,
    val address2: String?,
    val address3: String?,
    val city: String,
    @Json(name = "zip_code") val zipCode: String,
    val country: String,
    val state: String,
    @Json(name = "display_address") val displayAddress: List<String>
)
data class Open(
    @Json(name = "is_overnight") val isOvernight: Boolean,
    val start: String,
    val end: String,
    val day: Int
)
data class YelpReviewContainer(
    val reviews: List<YelpReview>,
    val total: Int
)

data class YelpReview(
    val id: String,
    val rating: Float,
    val text: String,
    @Json(name = "time_created") val timeCreated: String,
    val url: String,
    val user: YelpUser
)

data class YelpUser(
    val id: String,
    @Json(name = "profile_url") val profileUrl: String,
    @Json(name = "image_url") val imageUrl: String?,
    val name: String
)

/**
 * Convert Network results to database objects
 */
fun YelpBusinessContainer.asDatabaseModel(featured: Int): List<Business> {
    return businesses.map {
        Business(
            it.id,
            it.name,
            it.rating,
            it.imageUrl,
            it.reviewCount,
            it.location.city,
            it.price,
            it.distance,
            Coordinates(
                it.coordinates.latitude,
                it.coordinates.longitude
            ),
            featured
        )
    }
}

fun YelpBusinessContainer.asCategoryTypeModel(categoryType: Int, offset: Int): List<CategoryType> {
    val start = offset + 1
    return businesses.mapIndexed { index, yelpBusiness ->
        CategoryType(
            yelpBusiness.id,
            categoryType,
            index + start
        )
    }
}

fun YelpBusinessDetail.asDetailDatabaseModel(): BusinessDetail {
    val dbLocation = Location(
        location.address1,
        location.address2,
        location.address3,
        location.zipCode,
        location.country,
        location.state
    )

    val coordinate = Coordinates(
        coordinates.latitude,
        coordinates.longitude
    )
    val displayAddress = location.displayAddress.joinToString("|")
    val categoriesWithDelimiter = categories.joinToString(", ") { category ->
        category.title
    }

    return BusinessDetail(
        id, alias, isClaimed.toInt(), isClosed.toInt(), url, phone, displayPhone,
        displayAddress, categoriesWithDelimiter, dbLocation, price, coordinate
    )
}

fun YelpReviewContainer.asReviewDatabaseModel(businessId: String): List<Review> {
    return reviews.map { r ->
        Review(
            r.id,
            businessId,
            r.rating,
            r.text,
            r.timeCreated,
            r.url,
            r.user.id,
            r.user.profileUrl,
            r.user.imageUrl,
            r.user.name
        )
    }
}

fun YelpBusinessDetail.asPhotoDatabaseModel(): List<Photo> {
    return photos.map { photoUrl ->
        Photo(id, photoUrl)
    }
}

fun YelpBusinessDetail.asHourDatabaseModel(): Hour? {
    val hour = hours?.get(0)
    return if (hour != null) {
        Hour(id, hour.hoursType, hour.isOpenNow.toInt())
    } else
        null
}

fun YelpBusinessDetail.asOpenDatabaseModel(): List<com.example.avintura.database.Open>? {
    val hour = hours?.get(0)
    return hour?.open?.map {
        Open(
            id,
            it.start,
            it.end,
            it.day,
            it.isOvernight.toInt()
        )
    }
}