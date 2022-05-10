package com.example.avintura.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentHomeBinding
import com.example.avintura.domain.AvinturaBusiness
import com.example.avintura.network.getSearchViewItems
import com.example.avintura.ui.adapter.BusinessSearchResultAdapter
import com.example.avintura.ui.adapter.ViewPagerTopRecyclerViewAdapter
import com.example.avintura.util.getScaleAnimatorSet
import com.example.avintura.viewmodels.HomeViewModel
import com.example.avintura.viewmodels.HomeViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class HomeFragment : Fragment(), ViewPagerTopRecyclerViewAdapter.OnBusinessClickListener {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity().application as AvinturaApplication).repository)
    }

    private val businessSearchResultAdapter: BusinessSearchResultAdapter by lazy { BusinessSearchResultAdapter(requireContext()) }

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("onCreate", "HomeFragment created")
        if (!onBoardingFinished()) {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToViewPagerFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("onCreateView", "HomeFragment created view")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue_sapphire)
        animateCrown()
        setUpNavigation()
        binding.searchResultRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = businessSearchResultAdapter
        }
        setUpToolbar()
        setBusinessesObserver()
        setConnectionStatusObserver()
        setFavoriteCountObserver()
        setCategoryCardClick()
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResumeHome", "ViewPager2 Position: ${homeViewModel.position}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("HomeFragment:OnDestroyView", "Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HomeFragment:OnDestroy", "Called")
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.homeToolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setUpToolbar() {
        // TODO profile fragment
        // TODO search view
        binding.homeToolbar.title = getString(R.string.app_name)
        binding.homeToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    Toast.makeText(requireContext(), "Profile selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        val searchItem = binding.homeToolbar.menu.findItem(R.id.action_search)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.queryHint = "Search business..."
            searchItem.setOnActionExpandListener(getOnActionExpandListener())
            searchView.setOnQueryTextListener(getOnQueryTextListener())
        }
    }

    private fun getOnActionExpandListener(): MenuItem.OnActionExpandListener {
        return object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                binding.searchResultRecyclerView.visibility = View.VISIBLE
                Log.d("SearchView", "opened")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                binding.searchResultRecyclerView.visibility = View.GONE
                Log.d("SearchView", "closed")
                return true
            }
        }
    }

    private fun getOnQueryTextListener(): SearchView.OnQueryTextListener? {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    Log.d("TextSubmit", query)
                }
                return true
            }
            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    Log.d("TextChange", query)
                    searchNetwork(query)
                    // searchDatabaseForBusinesses(query)
                }
                return true
            }
        }
    }

    private fun searchNetwork(query: String) {
        // %" "% because our custom sql query will require that
        if (query.isNotEmpty()) {
            homeViewModel.searchAutocompleteFromNetwork(query)
        } else
            businessSearchResultAdapter.setData(emptyList())

        homeViewModel.autoCompleteResults.observe(viewLifecycleOwner) { autoCompleteContainer ->
            autoCompleteContainer.let {
                businessSearchResultAdapter.setData(autoCompleteContainer.getSearchViewItems())
            }
        }
    }

//    private fun searchDatabaseForBusinesses(query: String) {
//        // %" "% because our custom sql query will require that
//        if (query.isNotEmpty()) {
//            val searchQuery = "%$query%"
//            homeViewModel.searchDatabaseForBusinesses(searchQuery)
//                .observe(viewLifecycleOwner) { businesses ->
//                    businesses.let {
//                        businessSearchResultAdapter.setData(businesses)
//                    }
//                }
//        } else
//            businessSearchResultAdapter.setData(emptyList())
//    }

    private fun setBusinessesObserver() {
        binding.homeViewPager.adapter = ViewPagerTopRecyclerViewAdapter(requireContext(), this, emptyList())
        binding.homeViewPager.registerOnPageChangeCallback(getOnPageChangeCallbackObject())
        homeViewModel.businesses.observe(viewLifecycleOwner) { businesses ->
            if (businesses.isNotEmpty()) {
                if (!homeViewModel.doneLoading) {
                    shrinkProgressCircle(businesses)
                } else {
                    hideProgressIndicator()
                    setViewPager(businesses)
                }
            }
        }
    }

    private fun shrinkProgressCircle(businesses: List<AvinturaBusiness>) {
        val shrinkAnimator = getScaleAnimatorSet(binding.progressCircularLottie, 0.0f)
        shrinkAnimator.duration = 500
        shrinkAnimator.doOnEnd {
            hideProgressIndicator()
            setViewPager(businesses)
        }
        shrinkAnimator.start()
    }

    private fun setViewPager(businesses: List<AvinturaBusiness>) {
        binding.homeViewPager.adapter = ViewPagerTopRecyclerViewAdapter(requireContext(), this, businesses)
        if (homeViewModel.doneLoading)
            binding.homeViewPager.setCurrentItem(homeViewModel.position, false)
    }

    private fun hideProgressIndicator() {
        binding.progressCircularLottie.apply {
            visibility = View.GONE
            pauseAnimation()
        }
    }

    private fun setFavoriteCountObserver() {
        homeViewModel.favoriteCount.observe(viewLifecycleOwner) { count ->
            binding.favoriteText.text =
                resources.getQuantityString(R.plurals.favorite_count, count, count)
        }
    }

    private fun setConnectionStatusObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            homeViewModel.connectionStatus.collectLatest {
                if (it.isNotBlank() || it.isNotEmpty())
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setCategoryCardClick() {
        binding.wineryCard.setOnClickListener {
            navigateToCategory(Category.Winery)
        }
        binding.diningCard.setOnClickListener {
            navigateToCategory(Category.Dining)
        }
        binding.hotelCard.setOnClickListener {
            navigateToCategory(Category.HotelSpa)
        }
        binding.thingsCard.setOnClickListener {
            navigateToCategory(Category.Activity)
        }
        binding.favoriteCard.setOnClickListener {
            navigateToCategory(Category.Favorite)
        }
    }

    private fun navigateToCategory(category: Category) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(category))
    }

    private fun getOnPageChangeCallbackObject(): ViewPager2.OnPageChangeCallback {
        return object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                homeViewModel.position = position
            }
        }
    }

    private fun onBoardingFinished(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }

    private fun animateCrown() {
        val animatorSet = AnimatorSet()
        val rotationAnimatorBackward = ObjectAnimator.ofFloat(binding.crownImage, View.ROTATION, 25f, -15f)
        rotationAnimatorBackward.duration = 500

        val rotationAnimatorForward = ObjectAnimator.ofFloat(binding.crownImage, View.ROTATION, 0f, 385f)
        rotationAnimatorForward.duration = 1000

        animatorSet.playSequentially(
            rotationAnimatorBackward,
            rotationAnimatorForward
        )
        animatorSet.interpolator = OvershootInterpolator()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                animatorSet.startDelay = 500
                animatorSet.start()
            }
        })
        animatorSet.start()
    }

    override fun onBusinessClick(position: Int) {
        if (homeViewModel.businesses.value != null) {
            val action = HomeFragmentDirections.actionHomeFragmentToBusinessDetailFragment(homeViewModel.businesses.value!![position].id, homeViewModel.businesses.value!![position].name)
            findNavController().navigate(action)
        }
    }

    override fun onFavoriteClick(position: Int) {
        homeViewModel.updateFavorite(position)
    }
}