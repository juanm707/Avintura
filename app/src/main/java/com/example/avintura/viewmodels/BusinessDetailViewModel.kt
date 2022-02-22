package com.example.avintura.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.avintura.database.BusinessWithFavoriteStatus
import com.example.avintura.database.Favorite
import com.example.avintura.domain.*
import com.example.avintura.network.YelpAPINetwork
import com.example.avintura.network.YelpBusinessContainer
import com.example.avintura.repository.AvinturaRepository
import com.example.avintura.util.toInt
import kotlinx.coroutines.launch

class BusinessDetailViewModel(private val repository: AvinturaRepository, private val id: String) : ViewModel() {
    private val _connectionStatusError = MutableLiveData(false)
    val connectionStatus: LiveData<Boolean> = _connectionStatusError

    private val _photos = MutableLiveData<List<AvinturaPhoto>>()
    val photos: LiveData<List<AvinturaPhoto>> = _photos

    private val _hours = MutableLiveData<List<AvinturaHour>>()
    val hours: LiveData<List<AvinturaHour>> = _hours

    private val _reviews = MutableLiveData<List<AvinturaReview>>()
    val reviews: LiveData<List<AvinturaReview>> = _reviews

    private val _business = MutableLiveData<AvinturaBusinessDetail>()
    val business: LiveData<AvinturaBusinessDetail> = _business

    private val _favoriteInsertion = MutableLiveData<Boolean>()
    val favoriteInsertion: LiveData<Boolean> = _favoriteInsertion

    init {
        refreshDataFromNetwork()
    }

    private fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                repository.refreshBusinessDetail(id) // PUC Cafe "cjtJnwvXzixd4C0PlU6BhQ"
                _connectionStatusError.value = false
                _business.value = repository.getBusiness(id)
                _photos.value = repository.getPhotos(id)
                _hours.value = repository.getHours(id)

                repository.refreshReviews(id)
                _connectionStatusError.value = false
                _reviews.value = repository.getReviews(id)
            }

            catch (e: Exception) {
                Log.d("NetworkError", e.message.toString()) // if request to update data failed, use whats in DB if any

                _business.value = repository.getBusiness(id)
                if (business.value == null)
                    _connectionStatusError.value = true
                else {
                    _photos.value = repository.getPhotos(id)
                    _hours.value = repository.getHours(id)
                    _reviews.value = repository.getReviews(id)
                }
            }
        }
    }

    fun updateFavorite() {
        viewModelScope.launch {
            val business = business.value
            if (business != null) {
                val favorite = Favorite(business.businessBasic.id, (!business.businessBasic.favorite).toInt())
                val insertion = repository.insert(favorite)
                _favoriteInsertion.value = insertion > 0
            } else
                _favoriteInsertion.value = false
        }
    }
}

class BusinessDetailViewModelFactory(private val repository: AvinturaRepository, private val id: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusinessDetailViewModel(repository, id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}