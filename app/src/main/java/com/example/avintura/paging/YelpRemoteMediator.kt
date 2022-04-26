package com.example.avintura.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.avintura.database.AvinturaDatabase
import com.example.avintura.database.CategoryBusinessWithFavoriteStatus
import com.example.avintura.database.RemoteKeys
import com.example.avintura.network.YelpApiService
import com.example.avintura.network.YelpBusiness
import com.example.avintura.network.asCategoryTypeModel
import com.example.avintura.network.asDatabaseModel
import com.example.avintura.ui.Category
import com.example.avintura.util.DEFAULT_SEARCH_LOCATION
import com.example.avintura.util.DEFAULT_SEARCH_RADIUS
import com.example.avintura.util.getString
import com.example.avintura.util.getThingsToDoCategories
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class YelpRemoteMediator(private val category: Category, private val yelpApiService: YelpApiService, private val database: AvinturaDatabase) : RemoteMediator<Int, CategoryBusinessWithFavoriteStatus>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, CategoryBusinessWithFavoriteStatus>): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }
        try {
            val categoryString = if (category == Category.Activity) getThingsToDoCategories() else null
            val response = yelpApiService.searchBusinesses(
                searchTerm = category.getString(),
                location = DEFAULT_SEARCH_LOCATION,
                offset = offset,
                limit = state.config.pageSize,
                radius = DEFAULT_SEARCH_RADIUS,
                categories = categoryString
            )
            val businesses = response.businesses
            val endOfPaginationReached = businesses.isEmpty()
            database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                    database.categoryTypeDao().delete(category.ordinal)
                }
                val nextKey = if (endOfPaginationReached) null else offset + state.config.pageSize
                val keys = businesses.map { yelpBusiness ->
                    RemoteKeys(businessId = yelpBusiness.id, prevKey = null, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAll(keys)
                database.businessDao().insertAll(response.asDatabaseModel())
                database.categoryTypeDao().insertAll(response.asCategoryTypeModel(category.ordinal, offset))
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    // When we need to load data at the end of the currently loaded data set; load parameter is LoadType.APPEND
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CategoryBusinessWithFavoriteStatus>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { categoryBusiness ->
                // Get the remote keys of the last item retrieved
                database.remoteKeysDao().remoteKeysForBusinessId(categoryBusiness.business.id)
            }
    }
}