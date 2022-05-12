package com.example.avintura.domain

import com.example.avintura.database.Coordinates
import com.example.avintura.database.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class AvinturaBusiness(
    val id: String,
    val name: String,
    val rating: Float,
    val imageUrl: String,
    val reviewCount: Int,
    val city: String,
    var favorite: Boolean
)

data class AvinturaBusinessDetail(
    val businessBasic: AvinturaBusiness,
    val alias: String?,
    val isClaimed: Boolean?,
    val isClosed: Boolean?,
    val url: String,
    val phone: String?,
    val displayPhone: String?,
    val displayAddress: String?,
    val location: Location,
    val price: String,
    val coordinates: Coordinates,
    val categories: String?
)

data class AvinturaPhoto(
    val id: String,
    val url: String
)

data class AvinturaReview(
    val reviewId: String,
    val businessId: String,
    val rating: Float,
    val text: String,
    val timeCreated: String,
    val url: String,
    val user_id: String,
    val profileUrl: String,
    val imageUrl: String?,
    val name: String
)

data class AvinturaHour(
    val isOpenNow: Boolean,
    val businessId: String,
    val startHour: String,
    val endHour: String,
    val day: Int,
    val overnight: Int?
)

class AvinturaCategoryBusiness(
    val businessBasic: AvinturaBusiness,
    val city: String,
    val distance: Float?,
    val price: String?,
    val coordinates: Coordinates
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(coordinates.latitude, coordinates.longitude)
    }

    override fun getTitle(): String? {
        return businessBasic.name
    }

    override fun getSnippet(): String? {
        val favoriteExt = if (businessBasic.favorite) " • ♥" else ""
        if (price.isNullOrBlank())
            return "No Price$favoriteExt"
        return "$price$favoriteExt"
    }
}

abstract class SearchViewItem {
    abstract fun getString(): String
}

class Header(private val title: String) : SearchViewItem() {
    override fun getString(): String {
        return title
    }
}