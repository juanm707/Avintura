package com.example.avintura.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import com.example.avintura.database.asDomainModel
import com.example.avintura.database.dao.BusinessDao
import com.example.avintura.database.dao.FavoriteDao
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpAPINetwork.retrofitYelpService
import com.example.avintura.network.YelpApiService
import com.example.avintura.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvinturaRepository(private val businessDao: BusinessDao,
                         private val favoriteDao: FavoriteDao,
                         retrofitYelpService: YelpApiService) {

    // TODO: FIX THIS vvv https://developer.android.com/topic/libraries/architecture/livedata#livedata-in-architecture
    // https://developer.android.com/codelabs/advanced-kotlin-coroutines#7

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
//    val businesses: LiveData<List<AvinturaBusiness>> = Transformations.map(businessDao.getBusinesses()) {
//        it.asDomainModel()
//    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun refreshBusinesses(offsetFromViewModel: Int) {
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(
            searchTerm = null,
            location = "Napa County, CA",
            offset = offsetFromViewModel
        )
        if (businessesFromNetwork.businesses.isNotEmpty())
            businessDao.deleteAll()
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
    }

    @WorkerThread
    suspend fun getBusinesses(): List<AvinturaBusiness> {
        return businessDao.getBusinesses().asDomainModel()
    }

    @WorkerThread
    fun getFavoriteCount(): LiveData<Int> {
        return favoriteDao.getFavoriteCount()
    }

    @WorkerThread
    suspend fun insert(favorite: Favorite) {
        return favoriteDao.insert(favorite)
    }
}