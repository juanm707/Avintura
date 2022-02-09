package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpBusinessContainer
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.util.toInt
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AvinturaRepository) : ViewModel() {
    private val _connectionStatusError = MutableLiveData(false)
    val connectionStatus: LiveData<Boolean> = _connectionStatusError

    // TOP RESULTS
    private val _businesses = MutableLiveData<List<AvinturaBusiness>>()
    val businesses: LiveData<List<AvinturaBusiness>> = _businesses

    val favoriteCount = repository.getFavoriteCount().asLiveData()

    var position: Int = 0

//    init {
//        refreshDataFromNetwork()
//    }

    fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                repository.refreshBusinesses(0)
                _connectionStatusError.value = false
                _businesses.value = repository.getBusinesses()
            }
            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString()) // if request to update data failed, use whats in DB if any
                _businesses.value = repository.getBusinesses()
                if (businesses.value.isNullOrEmpty())
                    _connectionStatusError.value = true
            }
        }
    }

    fun updateFavorite(position: Int) {
        if (businesses.value != null) {
            val business = businesses.value!![position]
            viewModelScope.launch {
                val favorite = Favorite(business.id, business.favorite.toInt())
                repository.insert(favorite)
            }
        }
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