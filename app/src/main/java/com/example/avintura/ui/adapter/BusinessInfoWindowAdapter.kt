package com.example.avintura.ui.adapter

import android.content.Context
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import com.example.avintura.R
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.ui.Category
import com.example.avintura.util.getStarRatingSmallDrawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class BusinessInfoWindowAdapter(val context: Context)  : GoogleMap.InfoWindowAdapter{
    private var window = LayoutInflater.from(context).inflate(R.layout.map_marker_info_window, null)

    override fun getInfoContents(marker: Marker): View? {
        renderWindow(marker, window)
        return window
    }

    override fun getInfoWindow(marker: Marker): View? {
        renderWindow(marker, window)
        return window
    }

    private fun renderWindow(marker: Marker, view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.title)
        val tvSnippet = view.findViewById<TextView>(R.id.snippet)
        val ratingImage = view.findViewById<ImageView>(R.id.rating_image)
        val businessImage = view.findViewById<ImageView>(R.id.business_image)

        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet

        val businessItem = marker.tag as AvinturaCategoryBusiness
        ratingImage.setImageDrawable(businessItem.businessBasic.rating.getStarRatingSmallDrawable(context))

        // doesn't work imageView.load(url) so have to use image request builder
        val imageRequest = ImageRequest.Builder(context)
            .error(R.drawable.ic_baseline_broken_image_24)
            .data(businessItem.businessBasic.imageUrl)
            .target { businessImage.setImageDrawable(it)}
            .allowHardware(false)
            .listener(MarkerCallback(marker))
            .build()
        context.imageLoader.enqueue(imageRequest)
    }
}

class MarkerCallback(val marker: Marker) : ImageRequest.Listener {
    override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
        if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
            marker.showInfoWindow()
        }
    }
}

class BusinessClusterRenderer(
    val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AvinturaCategoryBusiness>,
    val category: Category?
) : DefaultClusterRenderer<AvinturaCategoryBusiness>(context, map, clusterManager) {

    private val clusterIconGenerator: IconGenerator = IconGenerator(context.applicationContext)

    override fun onBeforeClusterItemRendered(item: AvinturaCategoryBusiness, markerOptions: MarkerOptions) {
        val iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_dining_24)
        if (iconDrawable != null) {
            DrawableCompat.setTint(DrawableCompat.wrap(iconDrawable), ContextCompat.getColor(context,
                R.color.alloy_orange
            ))
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconDrawable.toBitmap())).title(item.title).snippet(item.snippet)
        } else {
            val markerHue = getMarkerHue(category)
            val markerDescriptor = BitmapDescriptorFactory.defaultMarker(markerHue)
            markerOptions.icon(markerDescriptor).title(item.title).snippet(item.snippet)
        }
    }

    override fun onClusterItemRendered(clusterItem: AvinturaCategoryBusiness, marker: Marker) {
        marker.tag = clusterItem
    }

    override fun onBeforeClusterRendered(cluster: Cluster<AvinturaCategoryBusiness>, markerOptions: MarkerOptions) {
        markerOptions.icon(getClusterMarkerIcon(cluster.size))
    }

    override fun onClusterUpdated(cluster: Cluster<AvinturaCategoryBusiness>, marker: Marker) {
        marker.setIcon(getClusterMarkerIcon(cluster.size))
    }

    private fun getClusterMarkerIcon(size: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, R.drawable.cluster_background)
        background?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getSizeColor(size), BlendModeCompat.SRC_ATOP)
        clusterIconGenerator.setBackground(background)
        clusterIconGenerator.setTextAppearance(R.style.Theme_Avintura_WhiteTextAppearance)

        val icon = clusterIconGenerator.makeIcon(size.toString())
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    private fun getSizeColor(size: Int): Int {
        return when (size) {
            in 1..15 -> ContextCompat.getColor(context, R.color.teal_claimed)
            in 16..35 -> ContextCompat.getColor(context, R.color.viridian_green)
            else -> ContextCompat.getColor(context, R.color.blue_sapphire)
        }
    }

    private fun getMarkerHue(category: Category?): Float {
        return when (category) {
            Category.Activity -> BitmapDescriptorFactory.HUE_AZURE
            Category.HotelSpa -> BitmapDescriptorFactory.HUE_VIOLET
            Category.Favorite -> BitmapDescriptorFactory.HUE_ROSE
            Category.Dining -> BitmapDescriptorFactory.HUE_ORANGE
            else -> BitmapDescriptorFactory.HUE_RED
        }
    }
}