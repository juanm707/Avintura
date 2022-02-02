package com.example.avintura.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.ui.adapter.BusinessInfoWindowAdapter
import com.example.avintura.ui.adapter.BusinessRenderer
import com.example.avintura.viewmodels.MapBusinessListViewModel
import com.example.avintura.viewmodels.MapBusinessListViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager


class MapsFragment : Fragment(), ClusterManager.OnClusterClickListener<AvinturaCategoryBusiness>, ClusterManager.OnClusterItemInfoWindowClickListener<AvinturaCategoryBusiness> {
    private lateinit var mapBusinessListViewModel: MapBusinessListViewModel
    private lateinit var mapBusinessListViewModelFactory: MapBusinessListViewModelFactory
    private var category: Category? = null
    private lateinit var map: GoogleMap

    private lateinit var clusterManager: ClusterManager<AvinturaCategoryBusiness>

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        setMapSettings(googleMap)
        setUpClusterManager(googleMap)

        googleMap.setInfoWindowAdapter(clusterManager.markerManager) // dont need this...

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        setMapListeners(googleMap)

        observeBusinesses(googleMap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = (it.getSerializable(CATEGORY_PARAM) as Category)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapBusinessListViewModelFactory = MapBusinessListViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            category!!
        )
        mapBusinessListViewModel = ViewModelProvider(this, mapBusinessListViewModelFactory)[MapBusinessListViewModel::class.java]
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    companion object {
        @JvmStatic
        fun newInstance(category: Category) =
            MapsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CATEGORY_PARAM, category)
                }
            }
    }

    override fun onClusterClick(cluster: Cluster<AvinturaCategoryBusiness>?): Boolean {
        if (cluster != null) {
            return zoomInCluster(cluster)
        }
        return false
    }

    override fun onClusterItemInfoWindowClick(item: AvinturaCategoryBusiness?) {
        if (item != null) {
            Toast.makeText(requireContext(), "Clicked info window: " + item.businessBasic.name, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMapSettings(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun  setMapListeners(googleMap: GoogleMap) {
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)
    }

    private fun setUpClusterManager(googleMap: GoogleMap) {
        clusterManager = ClusterManager(requireContext(), googleMap)
        clusterManager.setOnClusterClickListener(this)
        clusterManager.setOnClusterItemInfoWindowClickListener(this)
        clusterManager.renderer = BusinessRenderer(requireContext(), googleMap, clusterManager) // renders the cluster and individual marker icon
        clusterManager.markerCollection.setInfoWindowAdapter(BusinessInfoWindowAdapter(requireContext())) // the new info window
    }

    private fun observeBusinesses(googleMap: GoogleMap) {
        mapBusinessListViewModel.businesses.observe(viewLifecycleOwner, { businesses ->
            // Initialize bounds with current user location
            val builder: LatLngBounds.Builder = LatLngBounds.Builder()
            for (business in businesses) {
                builder.include(LatLng(business.coordinates.latitude, business.coordinates.longitude))
                clusterManager.addItem(business)
            }
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    (16 * (resources.displayMetrics.density)).toInt()
                )
            )
        })
    }

    private fun zoomInCluster(cluster: Cluster<AvinturaCategoryBusiness>): Boolean {
        val builder = LatLngBounds.builder()
        for (b in cluster.items) {
            builder.include(b.position)
        }
        val bounds = builder.build()

        return try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (16 * (resources.displayMetrics.density)).toInt()))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}