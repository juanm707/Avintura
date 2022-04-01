package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.avintura.database.Favorite
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpBusiness
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.ui.Category
import com.example.avintura.util.getString
import com.example.avintura.util.toInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryListViewModel(private val repository: AvinturaRepository, val category: Category) : ViewModel() {

    private val _connectionStatusError = MutableLiveData(false)
    val connectionStatus: LiveData<Boolean> = _connectionStatusError

    private val _businessesFavorite = MutableLiveData<List<AvinturaCategoryBusiness>>()
    val businessesFavorite: LiveData<List<AvinturaCategoryBusiness>> = _businessesFavorite

    private lateinit var _businessesPaging: LiveData<PagingData<YelpBusiness>>
    val businessesPaging: LiveData<PagingData<YelpBusiness>>
        get() = _businessesPaging

    init {
        getDataFromNetworkPaging()
    }

    private fun getDataFromNetworkPaging() {
        viewModelScope.launch {
            try {
                _businessesPaging = repository.getCategoryResultStream(category).cachedIn(viewModelScope).asLiveData()
            } catch (e: Exception) {
                Log.d("CategoryViewModel", "${e.message}")
            }
        }
    }

//    fun refreshDataFromNetwork() {
//        viewModelScope.launch {
//            try {
//                if (category != Category.Favorite) {
//                    repository.refreshBusinessesByCategory(
//                        category.getString(),
//                        "CA, CA 94574",
//                        0,
//                        50,
//                        24000,
//                        true,
//                        category
//                    )
//                    _connectionStatusError.value = false
//
//                }
//                _businesses.value = repository.getBusinessesByCategory(category)
//            }
//            catch (e: Exception) {
//                Log.d("NetworkError", e.message.toString()) // if request to update data failed, use whats in DB if any
//                _businesses.value = repository.getBusinessesByCategory(category)
//                if (businesses.value.isNullOrEmpty())
//                    _connectionStatusError.value = true
//            }
//        }
//    }

    fun refreshDBData() {
        viewModelScope.launch {
            _businessesFavorite.value = repository.getBusinessesByCategory(category)
        }
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