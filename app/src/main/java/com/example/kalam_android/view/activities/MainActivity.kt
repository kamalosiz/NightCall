package com.example.kalam_android.view.activities

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityMainBinding
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.view.adapter.HomePagerAdapter

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val homePagerAdapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = homePagerAdapter
        binding.viewPager.offscreenPageLimit = 3

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        logE("position: $position")
                        binding.ivChat.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCall.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    1 -> {
                        logE("position: $position")
                        binding.ivChat.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCall.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    2 -> {
                        logE("position: $position")
                        binding.ivChat.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCall.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    3 -> {
                        logE("position: $position")
                        binding.ivChat.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCall.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                }
            }

        })
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
//            finish()
        } else {
            binding.viewPager.currentItem = 0
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
