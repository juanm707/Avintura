package com.example.avintura.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.avintura.database.*
import com.example.avintura.database.dao.*
import com.example.avintura.domain.*
import com.example.avintura.network.*
import com.example.avintura.network.YelpAPINetwork.retrofitYelpService
import com.example.avintura.paging.YELP_NETWORK_PAGE_SIZE
import com.example.avintura.paging.YelpCategoryPagingDataSource
import com.example.avintura.ui.Category
import com.example.avintura.util.getString
import com.example.avintura.util.getThingsToDoCategories
import com.example.avintura.viewmodels.CategorySort
import kotlinx.coroutines.flow.Flow

class AvinturaRepository(
    private val database: AvinturaDatabase,
    private val businessDao: BusinessDao,
    private val favoriteDao: FavoriteDao,
    private val businessDetailDao: BusinessDetailDao,
    private val photoDao: PhotoDao,
    private val reviewDao: ReviewDao,
    private val hourDao: HourDao,
    private val openDao: OpenDao,
    private val featuredDao: FeaturedDao,
    private val categoryTypeDao: CategoryTypeDao
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    val featuredBusinesses: Flow<List<BusinessWithFavoriteStatus>> = featuredDao.getFeaturedBusinesses()

    suspend fun refreshBusinesses(offsetFromViewModel: Int) {
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(
            searchTerm = null,
            location = "CA, CA 94574",
            offset = offsetFromViewModel,
            radius = null,
            categories = null
        )
        if (businessesFromNetwork.businesses.isNotEmpty())
            featuredDao.deleteAll()
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
        featuredDao.insertAll(businessesFromNetwork.asFeaturedDatabaseModel())
    }

    suspend fun refreshBusinessesByCategory(
        searchTerm: String,
        location: String,
        offset: Int,
        limit: Int,
        radius: Int,
        deleteCategoryType: Boolean,
        categoryType: Category,
        categorySort: CategorySort?
    ) {
        val sort = categorySort?.getString() ?: ""
        val categoryString = if (categoryType == Category.Activity) getThingsToDoCategories() else null
        val businessesFromNetwork = retrofitYelpService.searchBusinesses(
            searchTerm = searchTerm,
            location = location,
            offset = offset,
            sortBy = sort,
            limit = limit,
            radius = radius,
            categories = categoryString
        )
        if (businessesFromNetwork.businesses.isNotEmpty() && deleteCategoryType)
            categoryTypeDao.delete(categoryType.ordinal)
        businessDao.insertAll(businessesFromNetwork.asDatabaseModel())
        if (categorySort == CategorySort.BEST_MATCH)
            categoryTypeDao.insertAll(businessesFromNetwork.asCategoryTypeModel(categoryType.ordinal, offset))
    }

    // @OptIn(ExperimentalPagingApi::class)
    fun getCategoryResultStream(category: Category, sortBy: CategorySort): Flow<PagingData<YelpBusiness>> {
        Log.d("AvinturaRepository", "AvinturaRepository::getCategoryResultsStream New category: ${category.getString()}")
        // val pagingSourceFactory = { database.categoryTypeDao().getBusinesses(category.ordinal)}
        return Pager(
            config = PagingConfig(
                pageSize = YELP_NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { YelpCategoryPagingDataSource(retrofitYelpService, category, sortBy)}
        ).flow
    }
    // TODO pass in database? so store in paging source?

    suspend fun refreshBusinessDetail(businessId: String): YelpBusinessDetail {
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
        return businessFromNetwork
    }

    suspend fun refreshReviews(businessId: String) {
        val reviewsFromNetwork = retrofitYelpService.getReviews(id = businessId)
        reviewDao.insertAll(reviewsFromNetwork.asReviewDatabaseModel(businessId))
    }

    suspend fun getBusinessesByCategory(categoryType: Category): List<AvinturaCategoryBusiness> {
        return if (categoryType == Category.Favorite)
            favoriteDao.getFavorites().asCategoryDomainModel()
        else
            categoryTypeDao.getCachedBusinesses(categoryType.ordinal).asCategoryDomainModel()
    }

    suspend fun getBusiness(businessId: String): AvinturaBusinessDetail? {
        val bd = businessDetailDao.getBusiness(businessId) ?: return null
        return bd.asDomainModel()
    }

    suspend fun getPhotos(businessId: String): List<AvinturaPhoto> {
        return photoDao.getPhotos(businessId).asPhotoDomainModel()
    }

    suspend fun getReviews(businessId: String): List<AvinturaReview> {
        return reviewDao.getReviews(businessId).asReviewDomainModel()
    }

    suspend fun getHours(businessId: String): List<AvinturaHour> {
        return hourDao.getHours(businessId).asHourDomainModel()
    }

    fun getFavoriteCount(): Flow<Int> {
        return favoriteDao.getFavoriteCount()
    }

    suspend fun insert(favorite: Favorite): Long {
        return favoriteDao.insert(favorite)
    }

    fun searchDatabaseForBusiness(searchQuery: String): Flow<List<String>> {
        return businessDao.searchDatabaseForBusinesses(searchQuery)
    }

    suspend fun searchAutocomplete(query: String, latitude: Double, longitude: Double): YelpAutocompleteContainer {
        return retrofitYelpService.getAutocompleteResults(
            text = query,
            latitude = latitude,
            longitude = longitude
        )
    }
}