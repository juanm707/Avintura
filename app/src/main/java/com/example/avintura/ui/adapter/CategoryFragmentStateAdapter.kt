package com.example.avintura.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.avintura.ui.Category
import com.example.avintura.ui.CategoryListFragment
import com.example.avintura.ui.MapsFragment

class CategoryFragmentStateAdapter(fragment: Fragment, private val category: Category) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int) : Fragment{
        return if (position == 0)
            CategoryListFragment.newInstance(category)
        else
            MapsFragment.newInstance(category)
    }
}