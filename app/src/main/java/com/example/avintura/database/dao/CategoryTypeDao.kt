package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.avintura.database.Business
import com.example.avintura.database.CategoryBusinessWithFavoriteStatus
import com.example.avintura.database.CategoryType
import com.example.avintura.database.Favorite
import com.example.avintura.ui.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryTypeDao {
    @Query("SELECT b.*, f.favorite FROM CategoryType ct LEFT JOIN Business b ON ct.businessId = b.id LEFT JOIN Favorite f ON f.id = b.id WHERE ct.type = :category ORDER BY ct.rank")
    suspend fun getBusinesses(category: Int) : List<CategoryBusinessWithFavoriteStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categoryType: List<CategoryType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryType: CategoryType)

    @Query("DELETE FROM CategoryType")
    suspend fun deleteAll()

    @Query("DELETE FROM CategoryType WHERE type = :categoryType")
    suspend fun delete(categoryType: Int)
}