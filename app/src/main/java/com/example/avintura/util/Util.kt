package com.example.avintura.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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

fun Int.toBoolean() = this == 1

fun Boolean.toInt() = if (this) 1 else 0

fun ObjectAnimator.disableViewDuringAnimation(view: View) {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            view.isEnabled = false
        }

        override fun onAnimationEnd(animation: Animator?) {
            view.isEnabled = true
        }
    })
}

fun ImageView.scaleHeart() {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.3F)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.3F)

    val animator = ObjectAnimator.ofPropertyValuesHolder(
        this, scaleX, scaleY
    )
    animator.repeatCount = 1
    animator.repeatMode = ObjectAnimator.REVERSE
    animator.duration = 100
    animator.disableViewDuringAnimation(this)
    animator.start()
}

fun ImageView.setFavoriteDrawable(context: Context, favorite: Boolean, default: Boolean) {
    if (favorite) {
        if (default)
            setFilledFavorite(context)
        else
            setBorderFavorite(context)
    } else {
        if (default)
            setBorderFavorite(context)
        else
            setFilledFavorite(context)
    }
}

fun ImageView.setFavoriteDrawableColored(context: Context, unwrappedDrawable: Drawable?) {
    if (unwrappedDrawable != null) {
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(
            wrappedDrawable,
            ContextCompat.getColor(context, R.color.bright_maroon)
        )
        this.setImageDrawable(wrappedDrawable)
    }
}

private fun ImageView.setBorderFavorite(context: Context,) {
    val unwrappedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_border_24)
    this.setFavoriteDrawableColored(context, unwrappedDrawable)
}

private fun ImageView.setFilledFavorite(context: Context,) {
    val unwrappedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24)
    this.setFavoriteDrawableColored(context, unwrappedDrawable)
}