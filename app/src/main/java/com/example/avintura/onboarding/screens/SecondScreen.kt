package com.example.avintura.onboarding.screens

import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.avintura.R
import com.example.avintura.util.getFadeInObjectAnimator
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView


class SecondScreen : Fragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var nextText: MaterialButton
    private lateinit var cardView: MaterialCardView
    private lateinit var title: TextView
    private lateinit var description: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SecondScreen", "onCreateView")
        return inflater.inflate(R.layout.fragment_second_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = activity?.findViewById(R.id.viewPager)!!

        cardView = view.findViewById(R.id.image_card_view)
        cardView.alpha = 0f
        title = view.findViewById(R.id.share_favorite_title)
        title.alpha = 0f
        description = view.findViewById(R.id.share_favorite_description)
        description.alpha = 0f

        nextText = view.findViewById(R.id.share_favorite_next)
        nextText.setOnClickListener {
            viewPager2.currentItem = 2
        }
    }

    override fun onResume() {
        super.onResume()
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            getFadeInObjectAnimator(cardView),
            getFadeInObjectAnimator(title),
            getFadeInObjectAnimator(description)
        )
        animatorSet.startDelay = 200
        animatorSet.start()
    }
}