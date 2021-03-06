package com.example.avintura.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.avintura.domain.*
import com.example.avintura.network.YelpUser
import com.example.avintura.util.toBoolean
import com.squareup.moshi.Json

/**
 * Database entities go in this file. These are responsible for reading and writing from the
 * database.
 */

/**
 * Business entity for the room database
 */
@Entity
data class Business(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Float,
    val imageUrl: String,
    val reviewCount: Int,
    val city: String,
    val price: String?,
    val distance: Float?,
    @Embedded val coordinates: Coordinates
)

@Entity
data class BusinessDetail(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Float,
    val imageUrl: String,
    val reviewCount: Int,
    val city: String,
    val alias: String?,
    val isClaimed: Int?,
    val isClosed: Int?,
    val url: String,
    val phone: String?,
    val displayPhone: String?,
    val displayAddress: String?,
    val categories: String?,
    @Embedded val location: Location,
    val price: String,
    @Embedded val coordinates: Coordinates
)

@Entity
data class Favorite(
    @PrimaryKey val id: String,
    val favorite: Int
)

@Entity(primaryKeys = ["id", "photoUrl"])
data class Photo(
    val id: String,
    val photoUrl: String
)

data class BusinessWithFavoriteStatus(
    val id: String,
    val name: String,
    val rating: Float,
    val imageUrl: String,
    val reviewCount: Int,
    val city: String,
    val favorite: Int
)

data class BusinessDetailWithFavoriteStatus(
    @Embedded val business: BusinessDetail,
    val favorite: Int
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class Location(
    val addressOne: String?,
    val addressTwo: String?,
    val addressThree: String?,
    val zipCode: String,
    val country: String,
    val state: String,
)

@Entity(primaryKeys = ["reviewId", "businessId"])
data class Review(
    val reviewId: String,
    val businessId: String,
    val rating: Float,
    val text: String,
    val timeCreated: String,
    val url: String,
    val user_id: String,
    val profileUrl: String,
    val imageUrl: String?,
    val name: String
)

@Entity
data class Hour(
    @PrimaryKey val businessId: String,
    val hourType: String,
    val isOpenNow: Int
)

@Entity(primaryKeys = ["businessId", "startHour", "endHour", "day"])
data class Open(
    val businessId: String,
    val startHour: String,
    val endHour: String,
    val day: Int,
    val overnight: Int?
)

data class OpenHours(
    val isOpenNow: Int,
    val businessId: String,
    val startHour: String,
    val endHour: String,
    val day: Int,
    val overnight: Int?
)

@Entity(primaryKeys = ["businessId", "type"])
data class CategoryType(
    val businessId: String,
    val type: Int,
    val rank: Int
)

data class CategoryBusinessWithFavoriteStatus(
    @Embedded val business: Business,
    val favorite: Int
)

@Entity
data class RemoteKeys(
    @PrimaryKey val businessId: String,
    val prevKey: Int?,
    val nextKey: Int?
)

@Entity
data class Featured(
    @PrimaryKey val businessId: String
)

/**
 * Map Business in db to domain entities
 */
fun List<BusinessWithFavoriteStatus>.asDomainModel(): List<AvinturaBusiness> {
    return map {
        AvinturaBusiness(
            id = it.id,
            name = it.name,
            rating = it.rating,
            imageUrl = it.imageUrl,
            reviewCount = it.reviewCount,
            city = it.city,
            favorite = it.favorite.toBoolean()
        )
    }
}

fun BusinessDetailWithFavoriteStatus.asDomainModel(): AvinturaBusinessDetail {
    val businessBasicInfo = AvinturaBusiness(business.id, business.name, business.rating, business.imageUrl, business.reviewCount, business.city, favorite.toBoolean())
    return AvinturaBusinessDetail(
        businessBasicInfo,
        business.alias,
        business.isClaimed?.toBoolean(),
        business.isClosed?.toBoolean(),
        business.url,
        business.phone,
        business.displayPhone,
        business.displayAddress,
        business.location,
        business.price,
        business.coordinates,
        business.categories
    )
}

//fun List<Business>.asDomainModel(): List<AvinturaBusiness> {
//    return map {
//        AvinturaBusiness(
//            it.name,
//            it.rating,
//            it.imageUrl,
//            it.reviewCount,
//            it.city
//        )
//    }
//}

fun List<Photo>.asPhotoDomainModel(): List<AvinturaPhoto> {
    return map {
        AvinturaPhoto(
            id = it.id,
            url = it.photoUrl
        )
    }
}

fun List<Review>.asReviewDomainModel(): List<AvinturaReview> {
    return map {
        AvinturaReview(
            it.reviewId,
            it.businessId,
            it.rating,
            it.text,
            it.timeCreated,
            it.url,
            it.user_id,
            it.profileUrl,
            it.imageUrl,
            it.name
        )
    }
}

fun List<OpenHours>.asHourDomainModel(): List<AvinturaHour> {
    return map {
        AvinturaHour(
            it.isOpenNow.toBoolean(),
            it.businessId,
            it.startHour,
            it.endHour,
            it.day,
            it.overnight
        )
    }
}

fun List<CategoryBusinessWithFavoriteStatus>.asCategoryDomainModel(): List<AvinturaCategoryBusiness> {
    return map { b ->
        AvinturaCategoryBusiness(
            AvinturaBusiness(
                b.business.id,
                b.business.name,
                b.business.rating,
                b.business.imageUrl,
                b.business.reviewCount,
                b.business.city,
                b.favorite.toBoolean()
            ),
            b.business.city,
            b.business.distance,
            b.business.price,
            Coordinates(b.business.coordinates.latitude, b.business.coordinates.longitude)
        )
    }
}