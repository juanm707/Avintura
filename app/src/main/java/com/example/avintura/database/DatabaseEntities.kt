package com.example.avintura.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.avintura.domain.AvinturaBusiness

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
    val reviewCount: Int
)

/**
 * Map Business in db to domain entities
 */
fun List<Business>.asDomainModel(): List<AvinturaBusiness> {
    return map {
        AvinturaBusiness(
            id = it.id,
            name = it.name,
            rating = it.rating,
            imageUrl = it.imageUrl,
            reviewCount = it.reviewCount
        )
    }
}