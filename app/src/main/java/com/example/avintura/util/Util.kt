package com.example.avintura.util

import android.animation.*
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.avintura.R
import com.example.avintura.domain.AvinturaHour
import com.example.avintura.ui.Category
import com.example.avintura.viewmodels.CategorySort
import java.text.SimpleDateFormat
import java.util.*

const val DEFAULT_SEARCH_LOCATION = "CA, CA 94574"
const val DEFAULT_SEARCH_RADIUS = 24000
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

private fun ImageView.setBorderFavorite(context: Context) {
    val unwrappedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_border_24)
    this.setFavoriteDrawableColored(context, unwrappedDrawable)
}

private fun ImageView.setFilledFavorite(context: Context) {
    val unwrappedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24)
    this.setFavoriteDrawableColored(context, unwrappedDrawable)
}

fun getHoursOfOperationToday(hours: List<AvinturaHour>): String {
    // Yelp     0 Monday, 1 Tuesday, 2 Wednesday, 3 Thursday,  4 Friday,   5 Saturday, 6 Sunday
    // Calendar 1 Sunday, 2 Monday,  3 Tuesday,   4 Wednesday, 5 Thursday, 6 Friday,   7 Saturday
    val today = Calendar.getInstance(Locale.getDefault())
    val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

    val todayYelpDay = if (dayOfWeek == 1) 6 else dayOfWeek - 2
    return try {
        val todayHours = hours.first {
            it.day == todayYelpDay
        }
        val hoursOpenToday = getHoursOfOperation(todayHours)

        val sdf = SimpleDateFormat("E", Locale.US)
        val day = sdf.format(today.time)

        "$day, $hoursOpenToday"

    } catch (e: NoSuchElementException) {
        "No hours today"
    }
}

fun getHoursOfOperation(hour: AvinturaHour): String {
    val start = getTimeAndPeriod(hour.startHour)
    val end = getTimeAndPeriod(hour.endHour)
    return "$start - $end"
}

private fun getTimeAndPeriod(time: String): String {
    val period: String
    var hour = time.substring(0, 2).toInt()

    if (hour >= 12) {
        period = "PM"
        if (hour == 12) {
            return "12:${time.substring(2, 4)} $period"
        } else {
            return "${hour - 12}:${time.substring(2, 4)} $period"
        }
    } else {
        period = "AM"
        if (hour == 0) hour = 12
        return "$hour:${time.substring(2, 4)} $period"
    }
}

fun getDay(day: Int): String {
    return when (day) {
        0 -> "Mon"
        1 -> "Tue"
        2 -> "Wed"
        3 -> "Thu"
        4 -> "Fri"
        5 -> "Sat"
        6 -> "Sun"
        else -> ""
    }
}

fun RecyclerView.setCategoryTileBackground(context: Context, category: Category) {
    when (category) {
        Category.Winery -> setTileBackgroundDrawable(context, ContextCompat.getDrawable(context, R.drawable.ic_wine_svgrepo_com_tile))
        Category.Dining -> setTileBackgroundDrawable(context, ContextCompat.getDrawable(context, R.drawable.food_svgrepo_com_tile))
        Category.HotelSpa -> setTileBackgroundDrawable(context, ContextCompat.getDrawable(context, R.drawable.ic_hotel_svgrepo_com_tile))
        Category.Activity -> setTileBackgroundDrawable(context, ContextCompat.getDrawable(context, R.drawable.ic_hot_air_balloon_svgrepo_com_tile))
        Category.Favorite -> setTileBackgroundDrawable(context, ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24_tile))
    }
}

private fun RecyclerView.setTileBackgroundDrawable(context: Context, d: Drawable?) {
    if (d != null) {
        val bitmap = d.drawableToBitmap()
        if (bitmap != null) {
            val matrix = Matrix().apply {
                postRotate(15F)
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val bitmapDrawable = BitmapDrawable(context.resources, rotated)
            bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            background = bitmapDrawable
        }
    }
}

private fun Drawable.drawableToBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}

fun Category.getString(): String {
    return when (this) {
        Category.Winery -> "Winery"
        Category.Dining -> "Restaurant"
        Category.Activity -> "Things To Do"
        Category.HotelSpa -> "Hotel and Spa"
        Category.Favorite -> "Favorite"
    }
}

fun Category.getProgressBarColor(context: Context): Int {
    return when (this) {
        Category.Winery -> ContextCompat.getColor(context, R.color.ruby_red)
        Category.Dining -> ContextCompat.getColor(context, R.color.mahogany)
        Category.Activity -> Color.parseColor("#013A63")
        Category.HotelSpa -> ContextCompat.getColor(context, R.color.persian_indigo)
        Category.Favorite -> ContextCompat.getColor(context, R.color.bright_maroon)
    }
}

fun getThingsToDoCategories(): String {
    return "tours,transport,limos,parks,gyms,galleries,yoga,theater,martialarts,bikerentals,partybusrentals,museums,landmarks,playgrounds,hiking,kids_activities,dancestudio,golf,meditationcenters,farms,festivals,farmersmarket,movietheaters,rafting,swimmingpools,boating,foodtours,social_clubs,bikes,bustours,hot_air_balloons,artclasses,walkingtours,paddleboarding,djs,active,arts,dog_parks,recreation,horsebackriding,horse_boarding"
}

fun setUIColorByCategory(
    category: Category,
    viewGroup: ViewGroup,
    toolbar: Toolbar,
    context: Context,
    mapTitleExtension: String
) {
    var bgColor: Int = ContextCompat.getColor(context, R.color.middle_blue_green)
    var title = "Category"

    when (category) {
        Category.Winery -> {
            bgColor = ContextCompat.getColor(context, R.color.pastel_pink)
            title = "Wineries"
        }
        Category.Dining -> {
            bgColor = ContextCompat.getColor(context, R.color.gamboge)
            title = "Dining"
        }
        Category.HotelSpa -> {
            bgColor = ContextCompat.getColor(context, R.color.mauve)
            title = "Hotel & Spa"
        }
        Category.Activity -> {
            bgColor = Color.parseColor("#89C2D9")
            title = "Things To Do"
        }
        Category.Favorite -> {
            bgColor = Color.parseColor("#FFCCD5")
            title = "Favorites"
        }
    }

    viewGroup.setBackgroundColor(bgColor)
    toolbar.title = title + mapTitleExtension
}

fun metersToMiles(distance: Float): String {
    return String.format("%.1f", distance/1609) + " mi"
}

fun getOnboardAnimatorSet(resources: Resources, cardView: View, title: View, description: View): AnimatorSet {
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(
        getMoveObjectAnimator(resources, cardView),
        getMoveObjectAnimator(resources, title),
        getMoveObjectAnimator(resources, description),
        getFadeInObjectAnimator(cardView),
        getFadeInObjectAnimator(title),
        getFadeInObjectAnimator(description),
    )
    return animatorSet
}

private fun getMoveObjectAnimator(resources: Resources, view: View): ObjectAnimator {
    return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -resources.displayMetrics.widthPixels.toFloat(), 0f)
}

fun getFadeInObjectAnimator(view: View): ObjectAnimator {
    return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
}

fun getScaleAnimatorSet(view: View, vararg values: Float): ValueAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, *values)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, *values)
    return ObjectAnimator.ofPropertyValuesHolder(
        view,
        scaleX,
        scaleY
    )
}

fun CategorySort.getString(): String {
    return when (this) {
        CategorySort.BEST_MATCH -> "best_match"
        CategorySort.DISTANCE -> "distance"
        CategorySort.RATING -> "rating"
        CategorySort.REVIEW_COUNT -> "review_count"
    }
}
