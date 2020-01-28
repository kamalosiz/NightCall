package com.example.kalam_android.view.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityStatusDetailBinding
import com.example.kalam_android.repository.model.ImageModel
import com.example.kalam_android.view.adapter.StatusPagerAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.kalam_android.callbacks.MyClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.status_bottom_sheet_fragment.*
import java.util.*
import kotlin.collections.ArrayList


class StatusDetailActivity : AppCompatActivity(), MyClickListener, View.OnTouchListener {


    private lateinit var binding: ActivityStatusDetailBinding
    private lateinit var statusPagerAdapter: StatusPagerAdapter
    private var dotsCount: Int = 0
    private lateinit var sliderItem: ArrayList<ImageModel>
    private var firstCurrentItem = 0
    private var active = 0
    private var itemPosition = 0
    private var NUM_PAGES = 0
    private var currentPage = 0
    private lateinit var runnable: Runnable
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_status_detail)

        sliderItem = ArrayList()
//        sliderItem.add(ImageModel(R.drawable.image_sample_1))
        sliderItem.add(ImageModel(R.drawable.image_sample_2))
        sliderItem.add(ImageModel(R.drawable.image_sample_3))
        sliderItem.add(ImageModel(R.drawable.image_sample_4))
        sliderItem.add(ImageModel(R.drawable.image_sample_5))
        sliderItem.add(ImageModel(R.drawable.image_sample_6))
        NUM_PAGES = sliderItem.size
        statusPagerAdapter = StatusPagerAdapter(this, sliderItem)
        binding.viewPager.adapter = statusPagerAdapter
        dotsCount = statusPagerAdapter.count
        statusPagerAdapter.setClickListener(this)

        viewPagerPagerSelected()
        initBottomSheet()
//        binding.lvStoriesView.setOnTouchListener(this)


//        autoScrollImages()
    }

    @SuppressLint("SwitchIntDef")
    private fun initBottomSheet() {
        bottomSheetBehavior =
            BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
             }

        })

    }

    private fun viewPagerPagerSelected() {
        try {
            binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    binding.viewPager.parent.requestDisallowInterceptTouchEvent(true)
                }

                override fun onPageSelected(position: Int) {
                    currentPage = position
                    if (itemPosition > position) {
                        if (active == 0) {
                            active = sliderItem.size - 1
                        } else {
                            active--
                        }
                    } else {
                        if (active >= sliderItem.size - 1) {
                            active = 0
                        } else {
                            active++
                        }
                    }

                    itemPosition = position
                    setPageIndicator(active)
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })

            for (i in sliderItem.indices) {
                val view = ImageView(this)

                view.setImageResource(R.drawable.ic_play_story_circle_filled)

                binding.SliderDots.addView(view)
            }

            setPageIndicator(firstCurrentItem)
        } catch (ex: Exception) {
//        Log.e(TAG, ex.message)
        }

    }

    fun setPageIndicator(position: Int) {
        var imageView: ImageView
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(5, 0, 5, 0)
        for (i in 0 until binding.SliderDots.getChildCount()) {
            imageView = binding.SliderDots.getChildAt(i) as ImageView
            imageView.layoutParams = lp
            if (position == i) {
                imageView.setImageResource(R.drawable.ic_play_story_circle_filled)
            } else {
                imageView.setImageResource(R.drawable.ic_play_story_circle)
            }
        }
    }

    private fun autoScrollImages() {

        val handler = Handler()
        runnable = Runnable {
            if (currentPage == NUM_PAGES) {
                currentPage = 0
                active = -1
                itemPosition = 0
            }
            binding.viewPager.setCurrentItem(currentPage++, true)
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

    }

    override fun myOnClick(view: View, position: Int) {
        slideUpDownBottomSheet()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {

            MotionEvent.ACTION_UP -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                return true
            }
            else -> {

                return false
            }
        }
    }

    fun slideUpDownBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}
