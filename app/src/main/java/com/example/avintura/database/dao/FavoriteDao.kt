package com.example.avintura.database.dao

import androidx.room.*
import com.example.avintura.database.CategoryBusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT  b.*, f.favorite FROM Favorite f LEFT JOIN Business b ON f.id = b.id WHERE f.favorite = 1")
    suspend fun getFavorites() : List<CategoryBusinessWithFavoriteStatus>

    @Query("SELECT COUNT(favorite) FROM Favorite WHERE favorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite) : Long

    @Query("DELETE FROM Favorite")
    suspend fun deleteAll()
}