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
interface PhotoDao {

    @Query("SELECT * FROM Photo WHERE id = :id")
    suspend fun getPhotos(id: String) : List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photo: List<Photo>)

    @Query("DELETE FROM Photo WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM Photo")
    suspend fun deleteAll()
}