package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.avintura.database.Business
import com.example.avintura.database.Favorite

@Dao
interface FavoriteDao {

    @Query("SELECT COUNT(favorite) FROM Favorite WHERE favorite = 1")
    fun getFavoriteCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Query("DELETE FROM Favorite")
    suspend fun deleteAll()
}