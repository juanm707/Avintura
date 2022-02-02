package com.example.avintura.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.avintura.AvinturaApplication
import com.example.avintura.databinding.FragmentCategoryListBinding
import com.example.avintura.ui.adapter.CategoryResultListRecyclerViewAdapter
import com.example.avintura.ui.adapter.ViewPagerTopRecyclerViewAdapter
import com.example.avintura.util.getProgressBarColor
import com.example.avintura.util.setCategoryTileBackground
import com.example.avintura.viewmodels.CategoryListViewModel
import com.example.avintura.viewmodels.CategoryListViewModelFactory
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter

const val CATEGORY_PARAM = "category"

class CategoryListFragment : Fragment(), ViewPagerTopRecyclerViewAdapter.OnBusinessClickListener {
    private lateinit var categoryListViewModel: CategoryListViewModel
    private lateinit var categoryListViewModelFactory: CategoryListViewModelFactory

    private var _binding: FragmentCategoryListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = (it.getSerializable(CATEGORY_PARAM) as Category)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        categoryListViewModelFactory = CategoryListViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            category!!
        )
        categoryListViewModel = ViewModelProvider(this, categoryListViewModelFactory)[CategoryListViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBusinessesObserver()
        setCategoryColor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setUpBusinessesObserver() {
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        categoryListViewModel.businesses.observe(viewLifecycleOwner, {
            binding.categoryRecyclerView.apply {
                binding.progressCircular.visibility = View.GONE
                adapter = AlphaInAnimationAdapter(CategoryResultListRecyclerViewAdapter(it, requireContext(), this@CategoryListFragment))
            }
        })
    }

    private fun setCategoryColor() {
        binding.categoryRecyclerView.setCategoryTileBackground(requireContext(), categoryListViewModel.category)
        binding.progressCircular.setIndicatorColor(categoryListViewModel.category.getProgressBarColor(requireContext()))
    }

    override fun onBusinessClick(position: Int) {
        if (categoryListViewModel.businesses.value != null) {
            val action = CategoryFragmentDirections.actionCategoryFragmentToBusinessDetailFragment(
                categoryListViewModel.businesses.value!![position].businessBasic.id,
                categoryListViewModel.businesses.value!![position].businessBasic.name)
            findNavController().navigate(action)
        }
    }

    override fun onFavoriteClick(position: Int) {
        Toast.makeText(requireContext(), "Favorited $position", Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(category: Category) =
            CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CATEGORY_PARAM, category)
                }
            }
    }
}