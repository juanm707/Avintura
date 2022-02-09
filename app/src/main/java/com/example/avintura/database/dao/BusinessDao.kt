package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessWithFavoriteStatus
import kotlinx.coroutines.flow.Flow

// https://developer.android.com/training/data-storage/room/async-queries#one-shot
// One-shot queries must be suspended, while observable queries are either flow or live data
@Dao
interface BusinessDao {

    @Query("SELECT b.*, f.favorite FROM Business b LEFT JOIN Favorite f ON f.id = b.id WHERE featured = 1")
    suspend fun getBusinesses(): List<BusinessWithFavoriteStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(businesses: List<Business>)

    @Query("DELETE FROM Business")
    suspend fun deleteAll()

    @Query("DELETE FROM Business WHERE featured = 1")
    suspend fun deleteFeatured()
}