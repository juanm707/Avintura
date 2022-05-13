package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import com.example.avintura.database.asDomainModel
import com.example.avintura.database.dao.getSearchViewItems
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.domain.SearchViewItem
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpAutocompleteContainer
import com.example.avintura.network.YelpBusinessContainer
import com.example.avintura.network.getSearchViewItems
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.util.toInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

const val LATITUDE_NAPA_VALLEY = 38.464673
const val LONGITUDE_NAPA_VALLEY = -122.425152

class HomeViewModel(private val repository: AvinturaRepository) : ViewModel() {
    private val _connectionStatusError = MutableSharedFlow<String>()
    val connectionStatus = _connectionStatusError.asSharedFlow().distinctUntilChanged()

    private val _searchResults = MutableLiveData<List<SearchViewItem>>()
    val searchResults: LiveData<List<SearchViewItem>> = _searchResults

    // TOP RESULTS
    val businesses: LiveData<List<AvinturaBusiness>> = repository.featuredBusinesses.asLiveData().map { list ->
        list.asDomainModel()
    }

    val favoriteCount = repository.getFavoriteCount().asLiveData()

    var position: Int = 0
    var doneLoading = false

    init {
        refreshDataFromNetwork()
    }

    private fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                repository.refreshBusinesses(0)
                _connectionStatusError.emit("")
            }
            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString())
                _connectionStatusError.emit(e.message.toString())
            }
            doneLoading = true
        }
    }

    fun searchAutocompleteFromNetwork(searchQuery: String) {
        viewModelScope.launch {
            try {
                val autocompleteSearchViewItems = repository.searchAutocomplete(searchQuery, LATITUDE_NAPA_VALLEY, LONGITUDE_NAPA_VALLEY).getSearchViewItems().toMutableList()
                autocompleteSearchViewItems.addAll(searchDatabaseForBusinesses(searchQuery))
                _searchResults.value = autocompleteSearchViewItems
                _connectionStatusError.emit("")
            }
            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString())
                _connectionStatusError.emit(e.message.toString())
                _searchResults.value = searchDatabaseForBusinesses(searchQuery)
            }
        }
    }

    fun updateFavorite(position: Int) {
        if (businesses.value != null) {
            val business = businesses.value!![position]
            viewModelScope.launch {
                val favorite = Favorite(business.id, (!business.favorite).toInt())
                repository.insert(favorite)
            }
        }
    }

    private suspend fun searchDatabaseForBusinesses(searchQuery: String): List<SearchViewItem> {
        return repository.searchDatabaseForBusiness("%$searchQuery%").getSearchViewItems()
    }
}

class HomeViewModelFactory(private val repository: AvinturaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}