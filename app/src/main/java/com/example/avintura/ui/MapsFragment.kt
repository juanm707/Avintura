package com.example.avintura.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var locationPermissionGranted: Boolean = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
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

            when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    googleMap.isMyLocationEnabled = true
                    Log.d("PermissionCheck", "Already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    //showInContextUI(...)
                    Log.d("PermissionCheck", "Show request permission rationale")
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO when user is prompted first time to enable location by permission, it doesnt update the map to show current location if granted
        // however it works when user opens map fragment after the first time.
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                Log.d("PermissionCheck", "isGranted")
                val gMap = map
                if (gMap != null) {
                    Log.d("PermissionCheck", "Map not null, enabling location")
                    gMap.isMyLocationEnabled = true
                }
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d("PermissionCheck", "Explaining")
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Avintura uses your location to display better results.")
                    .setPositiveButton("Close") { dialog, id ->

                    }
                builder.show()
            }
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
        setUIColorByCategory(
            mapBusinessListViewModel.category,
            binding.mapToolbar,
            binding.mapToolbar,
            requireContext(),
            " Map"
        )
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
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }
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
        mapBusinessListViewModel.businesses.observe(viewLifecycleOwner) { businesses ->
            // Initialize bounds with current user location
            if (businesses.isEmpty())
                showErrorDialog()
            else {
                val builder: LatLngBounds.Builder = LatLngBounds.Builder()
                for (business in businesses) {
                    builder.include(
                        LatLng(
                            business.coordinates.latitude,
                            business.coordinates.longitude
                        )
                    )
                    clusterManager.addItem(business)
                }
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        (16 * (resources.displayMetrics.density)).toInt()
                    )
                )
            }
        }
    }

    private fun showErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("No businesses to display.")
            .setPositiveButton("Ok") { dialog, which ->
                // Respond to positive button press
                findNavController().navigateUp()
            }
            .setOnDismissListener {
                findNavController().navigateUp()
            }
            .show()
    }

    private fun zoomInCluster(cluster: Cluster<AvinturaCategoryBusiness>): Boolean {
        val builder = LatLngBounds.builder()
        for (b in cluster.items) {
            builder.include(b.position)
        }

        return try {
            val bounds = builder.build()
            val gMap = map
            if (gMap != null) {
                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (16 * (resources.displayMetrics.density)).toInt()))
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