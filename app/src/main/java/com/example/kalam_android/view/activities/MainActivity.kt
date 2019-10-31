package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityMainBinding
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.adapter.HomePagerAdapter
import com.example.kalam_android.wrapper.SocketIO
import javax.inject.Inject

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MyApplication.getAppComponent(this).doInjection(this)
        val homePagerAdapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = homePagerAdapter
        binding.viewPager.offscreenPageLimit = 3
        binding.llChat.setOnClickListener { binding.viewPager.setCurrentItem(0, true) }
        binding.llCall.setOnClickListener { binding.viewPager.setCurrentItem(1, true) }
        binding.llStories.setOnClickListener { binding.viewPager.setCurrentItem(2, true) }
        binding.llProfile.setOnClickListener { binding.viewPager.setCurrentItem(3, true) }
        SocketIO.connectSocket(sharedPrefsHelper.getUser()?.token)
        binding.ivCompose.setOnClickListener {
            startActivity(Intent(this, ContactListActivity::class.java))

        }
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
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_colored)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    1 -> {
                        logE("position: $position")
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_colored)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    2 -> {
                        logE("position: $position")
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_colored)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                    }
                    3 -> {
                        logE("position: $position")
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_colored)
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = 0
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketIO.disconnectSocket()
    }
}
