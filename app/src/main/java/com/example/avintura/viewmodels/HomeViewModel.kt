package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.avintura.database.Business
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import com.example.avintura.database.asDomainModel
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpAutocompleteContainer
import com.example.avintura.network.YelpBusinessContainer
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.util.toInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AvinturaRepository) : ViewModel() {
    private val _connectionStatusError = MutableSharedFlow<String>()
    val connectionStatus = _connectionStatusError.asSharedFlow()

    private val _autoCompleteResults = MutableLiveData<YelpAutocompleteContainer>()
    val autoCompleteResults: LiveData<YelpAutocompleteContainer> = _autoCompleteResults

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
                _autoCompleteResults.value = repository.searchAutocomplete(searchQuery, 38.464673, -122.425152)
                _connectionStatusError.emit("")
            }
            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString())
                _connectionStatusError.emit(e.message.toString())
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

    fun searchDatabaseForBusinesses(searchQuery: String): LiveData<List<String>> {
        return repository.searchDatabaseForBusiness(searchQuery).asLiveData()
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