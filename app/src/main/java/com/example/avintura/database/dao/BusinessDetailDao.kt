package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessDetail
import com.example.avintura.database.BusinessDetailWithFavoriteStatus
import com.example.avintura.database.BusinessWithFavoriteStatus
import kotlinx.coroutines.flow.Flow

// https://developer.android.com/training/data-storage/room/async-queries#one-shot
// One-shot queries must be suspended, while observable queries are either flow or live data
@Dao
interface BusinessDetailDao {

    @Query("SELECT bd.*, f.favorite, b.name, b.rating, b.imageUrl, b.reviewCount, b.city FROM BusinessDetail bd LEFT JOIN Business b ON b.id = bd.id LEFT JOIN Favorite f ON f.id = b.id WHERE bd.id = :businessId")
    suspend fun getBusiness(businessId: String): BusinessDetailWithFavoriteStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(businessDetail: BusinessDetail)

    @Query("DELETE FROM BusinessDetail")
    suspend fun deleteAll()
}