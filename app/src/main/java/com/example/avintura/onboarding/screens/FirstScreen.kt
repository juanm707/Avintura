package com.example.avintura.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.avintura.R
import com.google.android.material.button.MaterialButton


class FirstScreen : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var nextText: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_screen, container, false)

        viewPager2 = activity?.findViewById(R.id.viewPager)!!
        nextText = view.findViewById(R.id.category_next)
        nextText.setOnClickListener {
            viewPager2.currentItem = 1
        }

        return view
    }
}