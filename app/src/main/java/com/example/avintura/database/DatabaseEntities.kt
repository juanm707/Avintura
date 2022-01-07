package com.example.avintura.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.util.toBoolean

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
    val city: String
)

@Entity
data class Favorite(
    @PrimaryKey val id: String,
    val favorite: Int
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