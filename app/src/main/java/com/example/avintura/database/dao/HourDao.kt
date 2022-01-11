package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.*
import kotlinx.coroutines.flow.Flow

// https://developer.android.com/training/data-storage/room/async-queries#one-shot
// One-shot queries must be suspended, while observable queries are either flow or live data
@Dao
interface HourDao {

    @Query("SELECT h.isOpenNow, o.* FROM Hour h LEFT JOIN Open o on o.businessId = h.businessId WHERE h.businessId = :businessId ORDER BY o.day")
    suspend fun getHours(businessId: String): List<OpenHours>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hour: Hour)

    @Query("DELETE FROM Hour")
    suspend fun deleteAll()
}