package com.example.avintura.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.example.avintura.R

fun Float.getStarRatingRegularDrawable(context: Context): Drawable? {
    return when (this) {
        1f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_1)
        1.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_1_half)
        2f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_2)
        2.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_2_half)
        3f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_3)
        3.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_3_half)
        4f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_4)
        4.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_4_half)
        5f -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_5)
        else -> AppCompatResources.getDrawable(context, R.drawable.stars_regular_0)
    }
}

fun Float.getStarRatingSmallDrawable(context: Context): Drawable? {
    return when(this)
    {
        1f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_1)
        1.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_1_half)
        2f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_2)
        2.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_2_half)
        3f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_3)
        3.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_3_half)
        4f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_4)
        4.5f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_4_half)
        5f -> AppCompatResources.getDrawable(context, R.drawable.stars_small_5)
        else -> AppCompatResources.getDrawable(context, R.drawable.stars_small_0)
    }
}