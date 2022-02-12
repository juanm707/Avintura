package com.example.avintura.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentMapsBinding
import com.example.avintura.domain.AvinturaCategoryBusiness
import com.example.avintura.ui.adapter.BusinessClusterRenderer
import com.example.avintura.ui.adapter.BusinessInfoWindowAdapter
import com.example.avintura.util.getProgressBarColor
import com.example.avintura.util.setUIColorByCategory
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

// https://stackoverflow.com/questions/37383749/how-to-properly-use-the-google-maps-fragment-in-a-navigation-drawer-activity-fra
// https://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
class MapsFragment : Fragment(), ClusterManager.OnClusterClickListener<AvinturaCategoryBusiness>, ClusterManager.OnClusterItemInfoWindowClickListener<AvinturaCategoryBusiness> {
    private lateinit var mapBusinessListViewModel: MapBusinessListViewModel
    private lateinit var mapBusinessListViewModelFactory: MapBusinessListViewModelFactory

    private var map: GoogleMap? = null

    private lateinit var clusterManager: ClusterManager<AvinturaCategoryBusiness>

    private var _binding: FragmentMapsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var contentView: View

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        if (map == null) {
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

            googleMap.setInfoWindowAdapter(clusterManager.markerManager) // don't need this...

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            setMapListeners(googleMap)

            observeBusinesses(googleMap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        mapBusinessListViewModelFactory = MapBusinessListViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            MapsFragmentArgs.fromBundle(requireArguments()).category
        )
        mapBusinessListViewModel = ViewModelProvider(this, mapBusinessListViewModelFactory)[MapBusinessListViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
        setUIColorByCategory(mapBusinessListViewModel.category, binding.mapToolbar, binding.mapToolbar, requireContext(), " Map")
        setToolbarItemsColor(mapBusinessListViewModel.category.getProgressBarColor(requireContext()))
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClusterClick(cluster: Cluster<AvinturaCategoryBusiness>?): Boolean {
        if (cluster != null) {
            return zoomInCluster(cluster)
        }
        return false
    }

    override fun onClusterItemInfoWindowClick(item: AvinturaCategoryBusiness?) {
        if (item != null) {
            val action = MapsFragmentDirections.actionMapsFragmentToBusinessDetailFragment(item.businessBasic.id, item.businessBasic.name)
            findNavController().navigate(action)
        }
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.mapToolbar.setupWithNavController(navController, appBarConfiguration)
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
        clusterManager.renderer = BusinessClusterRenderer(requireContext(), googleMap, clusterManager, mapBusinessListViewModel.category) // renders the cluster and individual marker icon
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
            if (map != null) {
                map!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (16 * (resources.displayMetrics.density)).toInt()))
                true
            }
            else {
                return false
            }
        } catch (e: Exception) {
            Log.d("MapFragment", "Error loading or zooming cluster. ${e.message}")
            false
        }
    }

    private fun setToolbarItemsColor(color: Int) {
        val arrowIcon = (binding.mapToolbar.navigationIcon as DrawerArrowDrawable)
        arrowIcon.color = color
        binding.mapToolbar.setTitleTextColor(color)
        requireActivity().window.statusBarColor = color
    }
}