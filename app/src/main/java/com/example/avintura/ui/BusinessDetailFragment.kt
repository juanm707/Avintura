package com.example.avintura.ui

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.request.ImageRequest
import com.example.avintura.AvinturaApplication
import com.example.avintura.R
import com.example.avintura.databinding.FragmentBusinessDetailBinding
import com.example.avintura.domain.AvinturaBusinessDetail
import com.example.avintura.ui.adapter.HoursRecyclerViewAdapter
import com.example.avintura.ui.adapter.ReviewRecyclerViewAdapter
import com.example.avintura.util.getHoursOfOperationToday
import com.example.avintura.util.getStarRatingRegularDrawable
import com.example.avintura.viewmodels.BusinessDetailViewModel
import com.example.avintura.viewmodels.BusinessDetailViewModelFactory

private const val STREET_ADDRESS_INDEX = 0
private const val CITY_ADDRESS_INDEX = 1

/**
 * Fragment that shows an individual business detail based on the passed id
 */
class BusinessDetailFragment : Fragment() {

    private lateinit var businessDetailViewModel: BusinessDetailViewModel
    private lateinit var businessDetailViewModelFactory: BusinessDetailViewModelFactory

    private lateinit var businessName: String
    private var titleTextColor: Int = 0

    private var _binding: FragmentBusinessDetailBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBusinessDetailBinding.inflate(inflater, container, false)
        businessDetailViewModelFactory = BusinessDetailViewModelFactory(
            (requireActivity().application as AvinturaApplication).repository,
            BusinessDetailFragmentArgs.fromBundle(requireArguments()).id
        )
        businessName = BusinessDetailFragmentArgs.fromBundle(requireArguments()).name
        businessDetailViewModel = ViewModelProvider(this, businessDetailViewModelFactory)[BusinessDetailViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.collapsingToolbarLayout.title = businessName
        setUpToolbar()
        setUpNavigation()
        setUpBusinessObserver()
        setUpPhotoObserver()
        setUpReviewsObserver()
        setUpHoursObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.detailToolbar.setupWithNavController(navController, appBarConfiguration)
    }

    /*
    Adding toolbar to fragment, this way! vvvv
         if using an ACTIVITY OWNED action bar, app bar, tool bar etc then need to do setHasOptionsMenu(True) in oncreate
         set support action bar in activity and then override methods create options and on option item selected etc...
         https://stackoverflow.com/questions/20226897/oncreateoptionsmenu-not-called-in-fragment
         fragment owned app bar
         https://developer.android.com/guide/navigation/navigation-ui#support_app_bar_variations
         https://developer.android.com/guide/fragments/appbar#fragment
     */
    private fun setUpToolbar() {
        // Set default color of toolbar and font
        val tf = ResourcesCompat.getFont(requireContext(), R.font.montserrat)
        binding.collapsingToolbarLayout.apply {
            setExpandedTitleColor(ContextCompat.getColor(requireContext(), R.color.white))
            setCollapsedTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setCollapsedTitleTypeface(tf)
            setExpandedTitleTypeface(tf)
        }
        binding.detailToolbar.inflateMenu(R.menu.menu_detail)
    }

    private fun setUpPhotoObserver() {
        val photoImageViewList = listOf(binding.image1, binding.image2, binding.image3)
        businessDetailViewModel.photos.observe(viewLifecycleOwner, { photos ->
            for (i in photos.indices) {
                photoImageViewList[i].load(photos[i].url) {
                    placeholder(R.drawable.ic_baseline_image_24)
                    crossfade(true)
                    crossfade(500)
                    error(R.drawable.ic_baseline_broken_image_24)
                }
            }
        })
    }

    private fun setUpBusinessObserver() {
        businessDetailViewModel.business.observe(viewLifecycleOwner, { business ->
            setRatingImage(business)
            setScrollToSectionClickListeners()
            setPriceAndCategory(business)
            setCollapsingToolbarData(business)
            setClaimedStatus(business)
            binding.reviewCount.text = resources.getQuantityString(R.plurals.review_count, business.businessBasic.reviewCount, business.businessBasic.reviewCount)
            binding.yelpLogo.setOnClickListener {
                val webpage: Uri = Uri.parse(business.url)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                startActivity(Intent.createChooser(intent, "Open with"))
            }
            setPhoneData(business)
            setAddressAndNavigation(business)
            businessDetailViewModel.favoriteInsertion.observe(viewLifecycleOwner, {
                val result = if (it) "successful" else "unsuccessful"
                updateFavoriteStatus(it, business)
                Toast.makeText(requireContext(), "Favorite update was $result!", Toast.LENGTH_SHORT).show()
            })
            binding.detailToolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_settings -> {
                        Toast.makeText(requireContext(), "Settings selected", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_call -> {
                        startCallIntent(business)
                        true
                    }
                    R.id.action_share -> {
                        startShareIntent(business)
                        true
                    }
                    R.id.action_add -> {
                        businessDetailViewModel.updateFavorite()
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
        })
    }

    private fun setUpReviewsObserver() {
        // Todo sort reviews by date
        binding.reviewCardView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        businessDetailViewModel.reviews.observe(viewLifecycleOwner, { reviews ->
            binding.reviewRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ReviewRecyclerViewAdapter(reviews, requireContext())
            }
        })
    }

    private fun setUpHoursObserver() {
        businessDetailViewModel.hours.observe(viewLifecycleOwner, { hours ->
            if (hours.isNotEmpty()) {
                if (hours[0].isOpenNow) {
                    binding.openOrClosed.apply {
                        text = "Open"
                        setTextColor(Color.parseColor("#43A047"))
                    }
                } else {
                    binding.openOrClosed.apply {
                        text = "Closed"
                        setTextColor(Color.parseColor("#E53935"))
                    }
                }

                binding.hoursOfOperationToday.text = getHoursOfOperationToday(hours)

                binding.hoursRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                    adapter = HoursRecyclerViewAdapter(hours)
                }
            }
        })
    }

    private fun setCollapsingToolbarData(business: AvinturaBusinessDetail) {
        binding.imageViewCollapsing.load(business.businessBasic.imageUrl) {
            crossfade(true)
            crossfade(500)
            listener(
                // pass two arguments
                onSuccess = { _, m ->
                    setPaletteColors()
                },
                onError = { request: ImageRequest, throwable: Throwable ->

                })
            allowHardware(false)
            //transformations(BlurTransformation(requireContext()))
        }
    }

    private fun setPriceAndCategory(business: AvinturaBusinessDetail) {
        // TODO use location if possible
        // "$$ • Wineries, Venues &amp; Event Spaces • 1.5 mi"
        val price = if (business.price == "") {
            "No Price"
        } else
            business.price

        binding.priceAndCategory.text = "$price • ${business.categories ?: "No Categories"} • 1.5 mi"
    }

    private fun updateFavoriteStatus(update: Boolean, business: AvinturaBusinessDetail) {
        if (update) {
            business.businessBasic.favorite = !(business.businessBasic.favorite)
            val heartItem = binding.detailToolbar.menu.findItem(R.id.action_add)
            if (business.businessBasic.favorite) {
                heartItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
            } else {
                heartItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_border_24)
            }
            if (titleTextColor != 0) {
                DrawableCompat.setTint(
                    DrawableCompat.wrap(heartItem.icon),
                    titleTextColor
                )
            }
        }
    }

    private fun setPhoneData(business: AvinturaBusinessDetail) {
        if (business.displayPhone == "") {
            binding.phoneNumber.text = "No Phone Number"
            binding.callButton.isEnabled = false
        } else
            binding.phoneNumber.text = business.displayPhone

        binding.callButton.setOnClickListener {
            startCallIntent(business)
        }
    }

    private fun startCallIntent(business: AvinturaBusinessDetail) {
        val callIntent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:${business.phone}")
        )
        startActivity(callIntent)
    }

    private fun startShareIntent(business: AvinturaBusinessDetail) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra("sms_body", "Hey! Check this place out!\n${business.url}")
        }
        startActivity(shareIntent)
    }

    private fun setAddressAndNavigation(business: AvinturaBusinessDetail) {
        if (business.displayAddress != null) {
            val parsedAddress = business.displayAddress.split("|") // should split into two, street and city
            binding.basicAddressText.text = parsedAddress[STREET_ADDRESS_INDEX]
            binding.cityAddressText.text = parsedAddress[CITY_ADDRESS_INDEX]

            binding.navigationButton.setOnClickListener {
                val addressStringList = parsedAddress[STREET_ADDRESS_INDEX].split(' ').toMutableList()
                addressStringList.addAll(parsedAddress[CITY_ADDRESS_INDEX].split(' '))

                val gmmIntentUri = Uri.parse("google.navigation:q=${addressStringList.joinToString("+")}")
                val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri )
                val chooser = Intent.createChooser(intent, null)
                try {
                    startActivity(chooser)
                } catch (e: ActivityNotFoundException) {

                }
            }
        }
    }

    private fun setClaimedStatus(business: AvinturaBusinessDetail) {
        if (business.isClaimed != null) {
            if (business.isClaimed) {
                binding.claimedImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_verified_24))
                binding.claimedText.text = "Claimed"
            }
        }
    }

    private fun setRatingImage(business: AvinturaBusinessDetail) {
        binding.reviewImage.setImageDrawable(
            business.businessBasic.rating.getStarRatingRegularDrawable(
                requireContext()
            )
        )
    }

    private fun setScrollToSectionClickListeners() {
        binding.reviewImage.setOnClickListener {
            scrollToView(binding.reviewCardView)
        }
        binding.reviewCount.setOnClickListener {
            scrollToView(binding.reviewCardView)
        }

        binding.openOrClosed.setOnClickListener {
            scrollToView(binding.locationAndHoursCardView)
        }
        binding.hoursOfOperationToday.setOnClickListener {
            scrollToView(binding.locationAndHoursCardView)
        }
    }

    private fun scrollToView(view: View) {
        val scrollTo: Int = view.top - (8 * resources.displayMetrics.density).toInt() // padding
        binding.nestedScrollView.smoothScrollTo(0, scrollTo)
    }

    private fun setPaletteColors() {
        val bitmap = binding.imageViewCollapsing.drawable.toBitmap()
        Palette.from(bitmap).generate { palette ->
            if (palette != null) {
                val vibrantSwatch = palette.vibrantSwatch
                val lightVibrantSwatch = palette.lightVibrantSwatch
                val darkVibrantSwatch = palette.darkVibrantSwatch
                val mutedSwatch = palette.mutedSwatch
                val lightMutedSwatch = palette.lightMutedSwatch
                val darkMutedSwatch = palette.darkMutedSwatch

                if (lightVibrantSwatch != null)
                    animateBackgroundColorChange(binding.nestedScrollView, lightVibrantSwatch.rgb)

                if (vibrantSwatch != null) {
                    // Title texts on cards
                    setCardViewTitleColor(vibrantSwatch.rgb)

                    // tool bar color
                    binding.collapsingToolbarLayout.apply {
                        setContentScrimColor(vibrantSwatch.rgb)
                        setCollapsedTitleTextColor(vibrantSwatch.titleTextColor)
                    }

                    // back arrow icon color
                    val icon = (binding.detailToolbar.navigationIcon as DrawerArrowDrawable?)
                    icon?.color = vibrantSwatch.titleTextColor

                    // overflow icon color
                    binding.detailToolbar.overflowIcon?.setTint(vibrantSwatch.titleTextColor)

                    setNavigationButtonColor(vibrantSwatch.titleTextColor, vibrantSwatch.rgb)

                    setFavoriteMenuItemColor(vibrantSwatch.titleTextColor)
                    titleTextColor = vibrantSwatch.titleTextColor
                }

                if (darkVibrantSwatch != null) {
                    // status bar color
                    setStatusBarColor(darkVibrantSwatch.rgb)

                    binding.navigationButton.apply {
                        rippleColor = darkVibrantSwatch.rgb
                    }

                    DrawableCompat.setTint(
                        DrawableCompat.wrap(binding.phoneIcon.drawable),
                        darkVibrantSwatch.rgb
                    )
                }
            }
        }
    }

    private fun animateBackgroundColorChange(view: View, color: Int) {
        val animator = ObjectAnimator.ofArgb(view, "backgroundColor",
            ContextCompat.getColor(requireContext(), R.color.middle_blue_green), color
        )
        animator.duration = 500
        animator.start()
    }

    private fun animateTextColorChange(textView: TextView, color: Int): ObjectAnimator? {
        val animator = ObjectAnimator.ofArgb(textView, "textColor",
            ContextCompat.getColor(requireContext(), R.color.blue_sapphire), color
        )
        animator.duration = 500
       return animator
    }

    private fun setStatusBarColor(color: Int) {
        // status bar color
        val window = requireActivity().window
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        // window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    private fun setCardViewTitleColor(color: Int) {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            animateTextColorChange(binding.detailTitle, color),
            animateTextColorChange(binding.photosTitle, color),
            animateTextColorChange(binding.reviewTitle, color),
            animateTextColorChange(binding.locationTitle, color),
            animateTextColorChange(binding.hoursTitle, color),
            animateTextColorChange(binding.contactTitle, color)
        )
        animatorSet.start()
    }

    private fun setNavigationButtonColor(colorIcon: Int, colorBackground: Int) {
        binding.navigationButton.apply {
            drawable.apply {
                setTint(colorIcon)
            }
            backgroundTintList = ColorStateList.valueOf(colorBackground)
        }
    }

    private fun setFavoriteMenuItemColor(color: Int) {
        // fav icon color
        val heartItem = binding.detailToolbar.menu.findItem(R.id.action_add)
        val favoriteStatus = businessDetailViewModel.business.value?.businessBasic?.favorite

        if (favoriteStatus != null) {
            if (favoriteStatus) {
                heartItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
            } else {
                heartItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_border_24)
            }
            DrawableCompat.setTint(
                DrawableCompat.wrap(heartItem.icon),
                color
            )
        }
    }
}