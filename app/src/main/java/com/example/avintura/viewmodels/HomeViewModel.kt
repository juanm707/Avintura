package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpBusinessContainer
import com.example.avintura.repository.AvinturaRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AvinturaRepository) : ViewModel() {
//    private val _businesses = MutableLiveData<YelpBusinessContainer>()
//    val businesses: LiveData<YelpBusinessContainer> = _businesses
    val businesses = repository.businesses

    init {
        refreshDataFromNetwork()
    }

    private fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                repository.refreshBusinesses()
            }
            catch (e: Exception) {
                Log.d("NetworkError", "$e")
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