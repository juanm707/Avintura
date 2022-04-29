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
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.database.asCategoryDomainModel
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.FieldPosition


enum class Category {
    Winery, Dining, HotelSpa, Activity, Favorite // 0, 1, 2, 3, 4
}

// TODO sort by rating distance etc...
class CategoryFragment : Fragment(),
    CategoryFavoriteListRecyclerViewAdapter.OnBusinessClickListener {
    private lateinit var categoryListViewModel: CategoryListViewModel
    private lateinit var categoryListViewModelFactory: CategoryListViewModelFactory

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
        // if picked favorites, just get from DB, else page network
        if (categoryListViewModel.category == Category.Favorite)
            setUpCacheObserver()
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
            categoryListViewModel.getCachedData()
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

        // repo uses flow, ui + view model uses live data
        viewLifecycleOwner.lifecycleScope.launch {
            categoryListViewModel.businessesPaging.observe(viewLifecycleOwner) { pagingData ->
                categoryAdapter.submitData(lifecycle, pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            categoryAdapter.loadStateFlow.collect { loadState ->
                if (loadState.refresh is LoadState.NotLoading)
                    binding.progressCircular.visibility = View.GONE

                if (loadState.append is LoadState.Loading || loadState.source.append is LoadState.Loading) {
                    // add to cache/room db
                    categoryListViewModel.refreshDataFromNetwork()
                }

                // Toast any error, regardless of whether it came from RemoteMediator or Paging Source
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                    ?: loadState.refresh as? LoadState.Error
                errorState?.let {
                    Toast.makeText(requireContext(), "Wooops, using cached results ${it.error}", Toast.LENGTH_LONG).show()
                    binding.progressCircular.visibility = View.GONE
                    setUpCacheObserver()
                }
            }
        }
    }

    private fun setUpCacheObserver() {
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        categoryListViewModel.businesses.observe(viewLifecycleOwner) { resultList ->
            if (resultList.isEmpty()) {
                Log.d("Category", "Empty")
                binding.emptyListText.apply {
                    visibility = View.VISIBLE
                    setTextColor(categoryListViewModel.category.getProgressBarColor(requireContext()))
                }
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
                scrollToPosition(categoryListViewModel.lastClickedRecyclerViewPosition)
            }
        }
        categoryListViewModel.getCachedData()
    }

    private fun setUpToolbar() {
        categoryListViewModel.connectionStatus.observe(viewLifecycleOwner) { error ->
            binding.categoryToolbar.menu.findItem(R.id.action_map).isEnabled = !error
        }
        binding.categoryToolbar.inflateMenu(R.menu.menu_category)
        binding.categoryToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_map -> {
                    val action = CategoryFragmentDirections.actionCategoryFragmentToMapsFragment(
                        categoryListViewModel.category
                    )
                    findNavController().navigate(action)
                    true
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

    override fun onBusinessClick(id: String, name: String, position: Int) {
        categoryListViewModel.lastClickedRecyclerViewPosition = position
        val action = CategoryFragmentDirections.actionCategoryFragmentToBusinessDetailFragment(
            id,
            name)
        findNavController().navigate(action)
    }

//    override fun onFavoriteClick(position: Int) {
//        Toast.makeText(requireContext(), "Favorited $position", Toast.LENGTH_SHORT).show()
//    }
}