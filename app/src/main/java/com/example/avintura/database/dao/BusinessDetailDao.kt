package com.example.avintura.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.BusinessDetail
import com.example.avintura.database.BusinessDetailWithFavoriteStatus

// https://developer.android.com/training/data-storage/room/async-queries#one-shot
// One-shot queries must be suspended, while observable queries are either flow or live data
@Dao
interface BusinessDetailDao {

    @Query("SELECT bd.*, f.favorite FROM BusinessDetail bd LEFT JOIN Favorite f ON f.id = bd.id WHERE bd.id = :businessId")
    suspend fun getBusiness(businessId: String): BusinessDetailWithFavoriteStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(businessDetail: BusinessDetail)

    @Query("DELETE FROM BusinessDetail")
    suspend fun deleteAll()
}