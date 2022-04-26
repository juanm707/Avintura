package com.example.avintura.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Featured
import kotlinx.coroutines.flow.Flow

@Dao
interface FeaturedDao {
    @Query("SELECT b.*, fa.favorite FROM Featured f LEFT JOIN Business b ON f.businessId = b.id LEFT JOIN Favorite fa ON b.id = fa.id")
    fun getFeaturedBusinesses(): Flow<List<BusinessWithFavoriteStatus>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(businesses: List<Featured>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(business: Featured)

    @Query("DELETE FROM Featured")
    suspend fun deleteAll()
}