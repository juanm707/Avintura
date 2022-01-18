package com.example.avintura.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentCategoryBinding
import com.example.avintura.util.setCategoryTileBackground
import com.example.avintura.viewmodels.CategoryViewModel
import com.example.avintura.viewmodels.CategoryViewModelFactory

enum class Category {
    Winery, Dining, HotelSpa, Activity, Favorite
}
class CategoryFragment : Fragment() {
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryViewModelFactory: CategoryViewModelFactory

    private var _binding: FragmentCategoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        categoryViewModelFactory = CategoryViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            CategoryFragmentArgs.fromBundle(requireArguments()).category
        )
        categoryViewModel = ViewModelProvider(this, categoryViewModelFactory)[CategoryViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setColorByCategory(categoryViewModel.category)
        binding.backArrow.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setColorByCategory(category: Category) {
        binding.repeatBackground.setCategoryTileBackground(requireContext(), category)
        when (category) {
            Category.Winery -> {
                binding.mainBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_pink))
                binding.categoryTopTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.ruby_red))
                DrawableCompat.setTint(
                    DrawableCompat.wrap(binding.backArrow.drawable),
                    ContextCompat.getColor(requireContext(), R.color.ruby_red)
                )
            }
            Category.Dining -> {
                binding.mainBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gamboge))
                binding.categoryTopTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.mahogany))
            }
            Category.HotelSpa -> {
                binding.mainBackground.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mauve))
                binding.categoryTopTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.persian_indigo))
            }
            Category.Activity -> {
                binding.mainBackground.setBackgroundColor(Color.parseColor("#89C2D9"))
                binding.categoryTopTitle.setTextColor(Color.parseColor("#013A63"))
            }
            Category.Favorite -> {
                binding.mainBackground.setBackgroundColor(Color.parseColor("#FFCCD5"))
                binding.categoryTopTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.bright_maroon))
            }
        }
    }
}