package com.example.avintura.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentHomeBinding
import com.example.avintura.ui.adapter.ViewPagerTopRecyclerViewAdapter
import com.example.avintura.viewmodels.HomeViewModel
import com.example.avintura.viewmodels.HomeViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

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

        setUpNavigation()
        setUpToolbar()
        setBusinessesObserver()
        setConnectionStatusObserver()
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
        })
    }

    private fun setConnectionStatusObserver() {
        homeViewModel.connectionStatus.observe(viewLifecycleOwner, {
            if (it)
                Toast.makeText(requireContext(), "BAD CONNECTION", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onBusinessClick(position: Int) {
       Toast.makeText(requireContext(), "Clicked on position: $position", Toast.LENGTH_SHORT).show()
    }

    override fun onFavoriteClick(position: Int) {
        homeViewModel.updateFavorite(position)
    }
}