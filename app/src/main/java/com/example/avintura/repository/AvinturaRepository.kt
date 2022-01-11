package com.example.avintura.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.avintura.database.*
import com.example.avintura.database.dao.*
import com.example.avintura.domain.*
import com.example.avintura.network.*
import com.example.avintura.network.YelpAPINetwork.retrofitYelpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvinturaRepository(
    private val businessDao: BusinessDao,
    private val favoriteDao: FavoriteDao,
    private val businessDetailDao: BusinessDetailDao,
    private val photoDao: PhotoDao,
    private val reviewDao: ReviewDao,
    private val hourDao: HourDao,
    private val openDao: OpenDao
) {

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
    suspend fun refreshBusinessDetail(businessId: String) {
        val businessFromNetwork = retrofitYelpService.getBusiness(
            id = businessId
        )
        businessDetailDao.insert(businessFromNetwork.asDetailDatabaseModel())

        if (businessFromNetwork.photos.isNotEmpty())
            photoDao.delete(businessId)
        photoDao.insertAll(businessFromNetwork.asPhotoDatabaseModel())

        if (!(businessFromNetwork.hours.isNullOrEmpty())) {
            val hourDBModel = businessFromNetwork.asHourDatabaseModel()
            if (hourDBModel != null) {
                hourDao.insert(hourDBModel)
                val openDBModel = businessFromNetwork.asOpenDatabaseModel()
                if (openDBModel != null)
                    openDao.insertAll(openDBModel)
            }
        }
    }

    @WorkerThread
    suspend fun refreshReviews(businessId: String) {
        val reviewsFromNetwork = retrofitYelpService.getReviews(id = businessId)
        reviewDao.insertAll(reviewsFromNetwork.asReviewDatabaseModel(businessId))
    }

    @WorkerThread
    suspend fun getBusinesses(): List<AvinturaBusiness> {
        return businessDao.getBusinesses().asDomainModel()
    }

    @WorkerThread
    suspend fun getBusiness(businessId: String): AvinturaBusinessDetail {
        return businessDetailDao.getBusiness(businessId).asDomainModel()
    }

    @WorkerThread
    suspend fun getPhotos(businessId: String): List<AvinturaPhoto> {
        return photoDao.getPhotos(businessId).asPhotoDomainModel()
    }

    @WorkerThread
    suspend fun getReviews(businessId: String): List<AvinturaReview> {
        return reviewDao.getReviews(businessId).asReviewDomainModel()
    }

    @WorkerThread
    suspend fun getHours(businessId: String): List<AvinturaHour> {
        return hourDao.getHours(businessId).asHourDomainModel()
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