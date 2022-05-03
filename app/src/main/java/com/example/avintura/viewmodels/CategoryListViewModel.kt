package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.network.YelpBusiness
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.ui.Category
import com.example.avintura.util.DEFAULT_SEARCH_LOCATION
import com.example.avintura.util.DEFAULT_SEARCH_RADIUS
import com.example.avintura.util.getString
import kotlinx.coroutines.launch

enum class CategorySort {
    BEST_MATCH, RATING, REVIEW_COUNT, DISTANCE
}

class CategoryListViewModel(private val repository: AvinturaRepository, val category: Category) : ViewModel() {

    var lastClickedRecyclerViewPosition = 0
    private val _connectionStatusError = MutableLiveData(false)
    val connectionStatus: LiveData<Boolean> = _connectionStatusError.distinctUntilChanged()

    private val _businesses = MutableLiveData<List<AvinturaCategoryBusiness>>()
    val businesses: LiveData<List<AvinturaCategoryBusiness>> = _businesses

    private val _sortBy = MutableLiveData(CategorySort.BEST_MATCH)
    val sortBy: LiveData<CategorySort> = _sortBy

    var businessesPaging: LiveData<PagingData<YelpBusiness>> = _sortBy.switchMap { categorySort ->
        repository.getCategoryResultStream(category, categorySort).cachedIn(viewModelScope).asLiveData()
    }
    //var businessesPaging: LiveData<PagingData<YelpBusiness>> = repository.getCategoryResultStream(category).cachedIn(viewModelScope).asLiveData()

    var offset = 0
    var limit = 30
    var delete = true

    init {
        // TODO need to cache from category too
        refreshDataFromNetwork() // copy paging initial lode, which is 3 * page size
    }

    fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                if (category != Category.Favorite) {
                    repository.refreshBusinessesByCategory(
                        category.getString(),
                        DEFAULT_SEARCH_LOCATION,
                        offset,
                        limit,
                        DEFAULT_SEARCH_RADIUS,
                        delete,
                        category,
                        _sortBy.value
                    )
                    _connectionStatusError.value = false
                    limit = 10
                    offset = if (offset == 0) 30 else offset + limit
                    delete = false
                }
                else {
                    _businesses.value = repository.getBusinessesByCategory(category)
                }
            }
            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString()) // if request to update data failed, use whats in DB if any
                _businesses.value = repository.getBusinessesByCategory(category)
                if (businesses.value.isNullOrEmpty())
                    _connectionStatusError.value = true
            }
        }
    }

    fun setSortType(categorySort: CategorySort) {
        _sortBy.value = categorySort
        if (categorySort == CategorySort.BEST_MATCH)
            delete = true
        resetParams()
        refreshDataFromNetwork()
    }

    fun getCachedData() {
        viewModelScope.launch {
            _businesses.value = repository.getBusinessesByCategory(category)
        }
    }

    private fun resetParams() {
        offset = 0
        limit = 30
    }

//    fun updateFavorite(position: Int) {
//        if (businesses.value != null) {
//            val business = businesses.value!![position]
//            viewModelScope.launch {
//                val favorite = Favorite(business.id, business.favorite.toInt())
//                repository.insert(favorite)
//            }
//        }
//    }
}

class CategoryListViewModelFactory(private val repository: AvinturaRepository, val category: Category) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(repository, category) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}