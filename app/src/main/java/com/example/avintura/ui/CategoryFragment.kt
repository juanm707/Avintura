package com.example.avintura.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.avintura.R
import com.example.avintura.databinding.FragmentCategoryBinding
import com.example.avintura.ui.adapter.CategoryFragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator


enum class Category {
    Winery, Dining, HotelSpa, Activity, Favorite // 0, 1, 2, 3, 4
}
class CategoryFragment : Fragment() {
    private lateinit var categoryFragmentStateAdapter: CategoryFragmentStateAdapter
    private lateinit var category: Category

    private var _binding: FragmentCategoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        category = CategoryFragmentArgs.fromBundle(requireArguments()).category
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNavigation()
        setUpViewPager()
        setColorByCategory(category)
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

    private fun setUpViewPager() {
        binding.categoryViewPager.isUserInputEnabled = false // no scroll
        categoryFragmentStateAdapter = CategoryFragmentStateAdapter(this, category)
        binding.categoryViewPager.adapter = categoryFragmentStateAdapter

        val tabLayout = binding.categoryTablayout

        // TODO prevent slide when still loading business in list fragment
        // tab icons from xml layout file disappear so have to set in mediator
        TabLayoutMediator(tabLayout, binding.categoryViewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_list_32)
                }
                1 -> {
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_map_24)
                }
            }
        }.attach()
    }

    private fun setColorByCategory(category: Category) {
        when (category) {
            Category.Winery -> {
                binding.coordinatorLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_pink))
                binding.categoryToolbar.title = "Wineries"
                setToolbarItemsColor(ContextCompat.getColor(requireContext(), R.color.ruby_red))
                setTabLayoutColor(ContextCompat.getColor(requireContext(), R.color.ruby_red), ContextCompat.getColor(requireContext(), R.color.pink))
            }
            Category.Dining -> {
                binding.coordinatorLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gamboge))
                binding.categoryToolbar.title = "Dining"
                setToolbarItemsColor(ContextCompat.getColor(requireContext(), R.color.mahogany))
                setTabLayoutColor(ContextCompat.getColor(requireContext(), R.color.mahogany), ContextCompat.getColor(requireContext(), R.color.orange_yellow_crayola))
            }
            Category.HotelSpa -> {
                binding.coordinatorLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mauve))
                binding.categoryToolbar.title = "Hotel & Spa"
                setToolbarItemsColor(ContextCompat.getColor(requireContext(), R.color.persian_indigo))
                setTabLayoutColor(ContextCompat.getColor(requireContext(), R.color.persian_indigo), ContextCompat.getColor(requireContext(), R.color.mauve_unselected))
            }
            Category.Activity -> {
                binding.coordinatorLayout.setBackgroundColor(Color.parseColor("#89C2D9"))
                binding.categoryToolbar.title = "Things To Do"
                setToolbarItemsColor(Color.parseColor("#013A63"))
                setTabLayoutColor(Color.parseColor("#013A63"), ContextCompat.getColor(requireContext(), R.color.celeste))
            }
            Category.Favorite -> {
                binding.coordinatorLayout.setBackgroundColor(Color.parseColor("#FFCCD5"))
                binding.categoryToolbar.title = "Favorites"
                setToolbarItemsColor(ContextCompat.getColor(requireContext(), R.color.bright_maroon))
                setTabLayoutColor(
                    ContextCompat.getColor(requireContext(), R.color.bright_maroon), ContextCompat.getColor(requireContext(),
                        R.color.pastel_pink
                    ))
            }
        }
    }

    private fun setToolbarItemsColor(color: Int) {
        val arrowIcon = (binding.categoryToolbar.navigationIcon as DrawerArrowDrawable)
        arrowIcon.color = color
        binding.categoryToolbar.setTitleTextColor(color)
        requireActivity().window.statusBarColor = color
    }

    private fun setTabLayoutColor(selected: Int, unselected: Int) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(
                selected,
                unselected
            )
        )
        binding.categoryTablayout.tabIconTint = colorStateList
        binding.categoryTablayout.setSelectedTabIndicatorColor(selected)
        val colorStateListRipple = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(
                unselected,
                selected
            )
        )
        binding.categoryTablayout.tabRippleColor = colorStateListRipple
    }
}