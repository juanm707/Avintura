package com.example.avintura.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentCategoryBinding
import com.example.avintura.network.YelpBusiness
import com.example.avintura.ui.adapter.CategoryFavoriteListRecyclerViewAdapter
import com.example.avintura.ui.adapter.CategoryResultListRecyclerViewAdapter
import com.example.avintura.ui.adapter.ViewPagerTopRecyclerViewAdapter
import com.example.avintura.util.getProgressBarColor
import com.example.avintura.util.setCategoryTileBackground
import com.example.avintura.util.setUIColorByCategory
import com.example.avintura.viewmodels.CategoryListViewModel
import com.example.avintura.viewmodels.CategoryListViewModelFactory
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


enum class Category {
    Winery, Dining, HotelSpa, Activity, Favorite // 0, 1, 2, 3, 4
}

class CategoryFragment : Fragment(),
    CategoryFavoriteListRecyclerViewAdapter.OnBusinessClickListener {
    private lateinit var categoryListViewModel: CategoryListViewModel
    private lateinit var categoryListViewModelFactory: CategoryListViewModelFactory

    private var showMap = false

    private var _binding: FragmentCategoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        categoryListViewModelFactory = CategoryListViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            CategoryFragmentArgs.fromBundle(requireArguments()).category
        )
        categoryListViewModel = ViewModelProvider(this, categoryListViewModelFactory)[CategoryListViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
        // if picked favorites, just get from DB, else page network TODO: remote mediator
        if (categoryListViewModel.category == Category.Favorite)
            setUpFavoritesObserver()
        else
            setUpBusinessesObserver()
        setUpToolbar()
        setCategoryColor()
        setUIColorByCategory(categoryListViewModel.category, binding.coordinatorLayout, binding.categoryToolbar, requireContext(), "")
        setToolbarItemsColor(categoryListViewModel.category.getProgressBarColor(requireContext()))
    }

    override fun onResume() {
        super.onResume()
        // TODO change to flow
        if (categoryListViewModel.category == Category.Favorite)
            categoryListViewModel.refreshDBData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.categoryToolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setUpBusinessesObserver() {
        val categoryAdapter = CategoryResultListRecyclerViewAdapter(requireContext(), this)
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = categoryAdapter
        }
//        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
//            categoryListViewModel.businessesFlow.collectLatest { pagingData ->
//                categoryAdapter.submitData(pagingData)
//            }
//        }

        // repo uses flow, ui + view model uses live data
        viewLifecycleOwner.lifecycleScope.launch {
            categoryListViewModel.businessesPaging.observe(viewLifecycleOwner) { pagingData ->
                categoryAdapter.submitData(lifecycle, pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            categoryAdapter.loadStateFlow.collect {
                if (it.refresh is LoadState.NotLoading)
                    binding.progressCircular.visibility = View.GONE
            }
        }
    }

    private fun setUpFavoritesObserver() {
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        categoryListViewModel.businessesFavorite.observe(viewLifecycleOwner) { resultList ->
            if (resultList.isEmpty()) {
                showMap = false
                Log.d("Category", "Empty")
                binding.emptyListText.apply {
                    visibility = View.VISIBLE
                    setTextColor(categoryListViewModel.category.getProgressBarColor(requireContext()))
                }
            } else {
                showMap = true
            }
            binding.categoryRecyclerView.apply {
                binding.progressCircular.visibility = View.GONE
                adapter = AlphaInAnimationAdapter(
                    CategoryFavoriteListRecyclerViewAdapter(
                        resultList,
                        requireContext(),
                        this@CategoryFragment
                    )
                )
            }
        }
    }

    private fun setUpToolbar() {
        binding.categoryToolbar.inflateMenu(R.menu.menu_category)
        binding.categoryToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    if (!showMap) {
                        Toast.makeText(requireContext(), "No locations available to display.", Toast.LENGTH_SHORT).show()
                        true
                    } else {
                        val action = CategoryFragmentDirections.actionCategoryFragmentToMapsFragment(
                            categoryListViewModel.category
                        )
                        findNavController().navigate(action)
                        true
                    }
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setCategoryColor() {
        binding.categoryRecyclerView.setCategoryTileBackground(requireContext(), categoryListViewModel.category)
        binding.progressCircular.setIndicatorColor(categoryListViewModel.category.getProgressBarColor(requireContext()))
    }

    private fun setToolbarItemsColor(color: Int) {
        val arrowIcon = (binding.categoryToolbar.navigationIcon as DrawerArrowDrawable)
        arrowIcon.color = color
        binding.categoryToolbar.setTitleTextColor(color)
        requireActivity().window.statusBarColor = color

        val mapIcon = binding.categoryToolbar.menu.findItem(R.id.action_map)
        DrawableCompat.setTint(
            DrawableCompat.wrap(mapIcon.icon),
            color
        )
    }

    // TODO fix on business click for paging items
    override fun onBusinessClick(id: String, name: String) {
        val action = CategoryFragmentDirections.actionCategoryFragmentToBusinessDetailFragment(
            id,
            name)
        findNavController().navigate(action)
    }

    // Zqx4mHjH6bg0L1yDrSbzkQ

//    override fun onBusinessClick(position: String) {
//        if (categoryListViewModel.category == Category.Favorite) {
//            if (categoryListViewModel.businessesFavorite.value != null) {
//                val action = CategoryFragmentDirections.actionCategoryFragmentToBusinessDetailFragment(
//                    categoryListViewModel.businessesFavorite.value!![position].businessBasic.id,
//                    categoryListViewModel.businessesFavorite.value!![position].businessBasic.name)
//                findNavController().navigate(action)
//            }
//        } else {
//
//        }
//    }

//    override fun onFavoriteClick(position: Int) {
//        Toast.makeText(requireContext(), "Favorited $position", Toast.LENGTH_SHORT).show()
//    }
}