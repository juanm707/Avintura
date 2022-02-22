package com.example.avintura.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.avintura.R
import com.example.avintura.util.getOnboardAnimatorSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView


class FirstScreen : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var nextText: MaterialButton
    private lateinit var cardView: MaterialCardView
    private lateinit var title: TextView
    private lateinit var description: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = activity?.findViewById(R.id.viewPager)!!

        cardView = view.findViewById(R.id.image_card_view)
        title = view.findViewById(R.id.category_title)
        description = view.findViewById(R.id.category_description)

        val animatorSet = getOnboardAnimatorSet(resources, cardView, title, description)
        animatorSet.start()

        nextText = view.findViewById(R.id.category_next)
        nextText.setOnClickListener {
            viewPager2.setCurrentItem(1, true)
        }
    }
}