package com.example.avintura.ui.adapter

import android.content.Context
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.example.avintura.R
import com.example.avintura.domain.AvinturaCategoryBusiness
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

        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet
    }
}

class BusinessRenderer(
    val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AvinturaCategoryBusiness>
) : DefaultClusterRenderer<AvinturaCategoryBusiness>(context, map, clusterManager) {

    private val clusterIconGenerator: IconGenerator = IconGenerator(context.applicationContext)

    override fun onBeforeClusterItemRendered(
        item: AvinturaCategoryBusiness,
        markerOptions: MarkerOptions
    ) {
        val markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        markerOptions.icon(markerDescriptor).snippet(item.title);
    }

    override fun onBeforeClusterRendered(cluster: Cluster<AvinturaCategoryBusiness>, markerOptions: MarkerOptions) {
        markerOptions.icon(getMarkerIcon(cluster.size))
    }

    override fun onClusterUpdated(cluster: Cluster<AvinturaCategoryBusiness>, marker: Marker) {
        marker.setIcon(getMarkerIcon(cluster.size))
    }

    private fun getMarkerIcon(size: Int): BitmapDescriptor {
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

}