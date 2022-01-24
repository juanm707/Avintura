package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.avintura.database.Business
import com.example.avintura.database.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    // TODO get favorites category
    @Query("SELECT COUNT(favorite) FROM Favorite WHERE favorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Query("DELETE FROM Favorite")
    suspend fun deleteAll()
}