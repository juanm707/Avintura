package com.example.avintura.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.avintura.database.asDomainModel
import com.example.avintura.database.dao.BusinessDao
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpAPINetwork.retrofitYelpService
import com.example.avintura.network.YelpApiService
import com.example.avintura.network.asDatabaseModel

class AvinturaRepository(private val businessDao: BusinessDao, retrofitYelpService: YelpApiService) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val businesses: LiveData<List<AvinturaBusiness>> = Transformations.map(businessDao.getBusinesses()) {
        it.asDomainModel()
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun refreshBusinesses() {
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(searchTerm = null, location = "Napa County, CA", offset = 0)
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
    }
}