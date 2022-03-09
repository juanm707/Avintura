package com.example.avintura.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpApiService
import com.example.avintura.network.YelpBusiness
import com.example.avintura.ui.Category
import com.example.avintura.util.DEFAULT_SEARCH_LOCATION
import com.example.avintura.util.DEFAULT_SEARCH_RADIUS
import com.example.avintura.util.getString
import com.example.avintura.util.getThingsToDoCategories
import retrofit2.HttpException
import java.io.IOException

const val NETWORK_PAGE_SIZE = 10

class YelpCategoryPagingDataSource(private val yelpApiService: YelpApiService, private val category: Category) : PagingSource<Int, YelpBusiness>() {
    override fun getRefreshKey(state: PagingState<Int, YelpBusiness>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(10)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(10)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, YelpBusiness> {
        val offset = params.key ?: 0
        return try {
            val categoryString = if (category == Category.Activity) getThingsToDoCategories() else null
            val response = yelpApiService.searchBusinesses(
                searchTerm = category.getString(),
                location = DEFAULT_SEARCH_LOCATION,
                offset = offset,
                limit = params.loadSize,
                radius = DEFAULT_SEARCH_RADIUS,
                categories = categoryString
            )
            val businesses = response.businesses
            val nextKey = if (businesses.isEmpty()) {
                null
            } else {
                offset + params.loadSize
            }
            LoadResult.Page(
                data = businesses,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}