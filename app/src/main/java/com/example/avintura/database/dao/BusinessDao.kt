package com.example.avintura.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.domain.Header
import com.example.avintura.domain.SearchViewItem
import com.example.avintura.network.YelpAutocompleteBusiness
import kotlinx.coroutines.flow.Flow

// https://developer.android.com/training/data-storage/room/async-queries#one-shot
// One-shot queries must be suspended, while observable queries are either flow or live data
@Dao
interface BusinessDao {

    // For search view
    @Query(
        "SELECT b.name, b.id FROM Business b WHERE b.name LIKE :searchQuery " +
                "UNION " +
                "SELECT bd.name, bd.id FROM BusinessDetail bd WHERE bd.name LIKE :searchQuery")
    suspend fun searchDatabaseForBusinesses(searchQuery: String): List<NameIdTuple>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(businesses: List<Business>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(businesses: Business)

    @Query("DELETE FROM Business")
    suspend fun deleteAll()
}

data class NameIdTuple(
    val name: String?,
    val id: String?
)

fun List<NameIdTuple>.getSearchViewItems(): List<SearchViewItem> {
    val results = mutableListOf<SearchViewItem>()
    if (isNotEmpty()) {
        results.add(Header("Stored Businesses"))
        forEach { businessTuple ->
            if (businessTuple.id != null && businessTuple.name != null)
                results.add(YelpAutocompleteBusiness(businessTuple.name, businessTuple.id))
        }
    }
    return results
}
