package com.example.avintura.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentHomeBinding
import com.example.avintura.ui.adapter.ViewPagerTopRecyclerViewAdapter
import com.example.avintura.viewmodels.HomeViewModel
import com.example.avintura.viewmodels.HomeViewModelFactory

class HomeFragment : Fragment(), ViewPagerTopRecyclerViewAdapter.OnBusinessClickListener {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity().application as AvinturaApplication).repository)
    }

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue_sapphire)
        setUpNavigation()
        setUpToolbar()
        setBusinessesObserver()
        setConnectionStatusObserver()
        setFavoriteCountObserver()
        setCategoryCardClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.homeToolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setUpToolbar() {
        binding.homeToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    Toast.makeText(requireContext(), "Profile selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setBusinessesObserver() {
        binding.homeViewPager.adapter = ViewPagerTopRecyclerViewAdapter(requireContext(), this)
        homeViewModel.businesses.observe(viewLifecycleOwner, { businesses ->
            binding.progressCircular.visibility = View.GONE
            (binding.homeViewPager.adapter as ViewPagerTopRecyclerViewAdapter).submitList(businesses)
            binding.homeViewPager.setCurrentItem(homeViewModel.position, false)
        })
    }

    private fun setFavoriteCountObserver() {
        homeViewModel.favoriteCount.observe(viewLifecycleOwner, { count ->
            binding.favoriteText.text = resources.getQuantityString(R.plurals.favorite_count, count, count)
        })
    }

    private fun setConnectionStatusObserver() {
        homeViewModel.connectionStatus.observe(viewLifecycleOwner, {
            if (it)
                Toast.makeText(requireContext(), "BAD CONNECTION", Toast.LENGTH_SHORT).show()
        })
    }

    private fun setCategoryCardClick() {
        binding.wineryCard.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(Category.Winery))
        }
        binding.diningCard.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(Category.Dining))
        }
        binding.hotelCard.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(Category.HotelSpa))
        }
        binding.thingsCard.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(Category.Activity))
        }
        binding.favoriteCard.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCategoryFragment(Category.Favorite))
        }
    }

    override fun onBusinessClick(position: Int) {
        if (homeViewModel.businesses.value != null) {
            homeViewModel.position = position
            val action = HomeFragmentDirections.actionHomeFragmentToBusinessDetailFragment(homeViewModel.businesses.value!![position].id, homeViewModel.businesses.value!![position].name)
            findNavController().navigate(action)
        }
    }

    override fun onFavoriteClick(position: Int) {
        homeViewModel.updateFavorite(position)
    }
}