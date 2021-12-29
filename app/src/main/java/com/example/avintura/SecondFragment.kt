package com.example.avintura

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.avintura.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbarSecond.setupWithNavController(navController, appBarConfiguration)

        // Adding toolbar to fragment, this way! vvvv
        // if using an activity action bar, app bar, tool bar etc then need to do setHasOptionsMenu(True)
        // set support action bar in activity and then override menthods create options and on option item selected etc...
        // https://stackoverflow.com/questions/20226897/oncreateoptionsmenu-not-called-in-fragment
        // fragment owned app bar
        // https://developer.android.com/guide/navigation/navigation-ui#support_app_bar_variations
        // https://developer.android.com/guide/fragments/appbar#fragment

        binding.toolbarSecond.inflateMenu(R.menu.menu_main)
        binding.toolbarSecond.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    Toast.makeText(requireContext(), "Settings selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}