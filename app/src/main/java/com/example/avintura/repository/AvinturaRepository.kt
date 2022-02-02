package com.example.avintura.repository

import androidx.annotation.WorkerThread
import com.example.avintura.database.*
import com.example.avintura.database.dao.*
import com.example.avintura.domain.*
import com.example.avintura.network.*
import com.example.avintura.network.YelpAPINetwork.retrofitYelpService
import com.example.avintura.ui.Category
import com.example.avintura.util.getThingsToDoCategories
import kotlinx.coroutines.flow.Flow

class AvinturaRepository(
    private val businessDao: BusinessDao,
    private val favoriteDao: FavoriteDao,
    private val businessDetailDao: BusinessDetailDao,
    private val photoDao: PhotoDao,
    private val reviewDao: ReviewDao,
    private val hourDao: HourDao,
    private val openDao: OpenDao,
    private val categoryTypeDao: CategoryTypeDao
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun refreshBusinesses(offsetFromViewModel: Int) {
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(
            searchTerm = null,
            location = "Napa County, CA",
            offset = offsetFromViewModel,
            radius = null,
            categories = null
        )
        if (businessesFromNetwork.businesses.isNotEmpty())
            businessDao.deleteAll()
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
    }

    @WorkerThread
    suspend fun refreshBusinessesByCategory(
        searchTerm: String,
        location: String,
        offset: Int,
        limit: Int,
        radius: Int,
        deleteCategoryType: Boolean,
        categoryType: Category
    ) {
        val categoryString = if (categoryType == Category.Activity) getThingsToDoCategories() else null
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(
            searchTerm = searchTerm,
            location = location,
            offset = offset,
            limit = limit,
            radius = radius,
            categories = categoryString
        )
        if (businessesFromNetwork.businesses.isNotEmpty() && deleteCategoryType)
            categoryTypeDao.delete(categoryType.ordinal)
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
        categoryTypeDao.insertAll(businessesFromNetwork.asCategoryTypeModel(categoryType.ordinal, offset))
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
    suspend fun getBusinessesByCategory(categoryType: Category): List<AvinturaCategoryBusiness> {
        return if (categoryType == Category.Favorite)
            favoriteDao.getFavorites().asCategoryDomainModel()
        else
            categoryTypeDao.getBusinesses(categoryType.ordinal).asCategoryDomainModel()
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
    fun getFavoriteCount(): Flow<Int> {
        return favoriteDao.getFavoriteCount()
    }

    @WorkerThread
    suspend fun insert(favorite: Favorite) {
        return favoriteDao.insert(favorite)
    }
}