package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.databinding.ActivityMainBinding
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.adapter.HomePagerAdapter
import com.example.kalam_android.webrtc.AudioCallActivity
import com.example.kalam_android.webrtc.VideoCallActivity
import com.example.kalam_android.webrtc.CustomWebSocketListener
import com.example.kalam_android.wrapper.SocketIO
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class MainActivity : BaseActivity(), WebSocketOfferCallback {

    private lateinit var binding: ActivityMainBinding
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var customWebSocketListener: CustomWebSocketListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MyApplication.getAppComponent(this).doInjection(this)
        val homePagerAdapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = homePagerAdapter
        binding.viewPager.offscreenPageLimit = 4
        binding.llChat.setOnClickListener { binding.viewPager.setCurrentItem(0, true) }
        binding.llCall.setOnClickListener { binding.viewPager.setCurrentItem(1, true) }
        binding.llStories.setOnClickListener { binding.viewPager.setCurrentItem(2, true) }
        binding.llProfile.setOnClickListener { binding.viewPager.setCurrentItem(3, true) }
        binding.ivSettings.setOnClickListener { binding.viewPager.setCurrentItem(4, true) }
        SocketIO.connectSocket(sharedPrefsHelper.getUser()?.token)
        connectWebSocket()
        binding.header.btnRight.visibility = View.GONE
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
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_colored)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                        binding.ivSettings.setBackgroundResource(R.drawable.icon_settings)
                    }
                    1 -> {
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_colored)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                        binding.ivSettings.setBackgroundResource(R.drawable.icon_settings)
                    }
                    2 -> {
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_colored)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                        binding.ivSettings.setBackgroundResource(R.drawable.icon_settings)
                    }
                    3 -> {
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_colored)
                        binding.ivSettings.setBackgroundResource(R.drawable.icon_settings)
                    }
                    4 -> {
                        binding.ivChats.setBackgroundResource(R.drawable.icon_chat_grey)
                        binding.ivCalls.setBackgroundResource(R.drawable.icon_call_grey)
                        binding.ivStories.setBackgroundResource(R.drawable.icon_stories_grey)
                        binding.ivProfile.setBackgroundResource(R.drawable.icon_profile_grey)
                        binding.ivSettings.setBackgroundResource(R.drawable.icon_settings)

                    }

                }
            }
        })
        val isOutside = intent.getBooleanExtra(AppConstants.IS_FROM_OUTSIDE, false)
        if (isOutside) {
            val chatID = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            val name = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra(AppConstants.CHAT_ID, chatID)
            intent.putExtra(AppConstants.IS_CHATID_AVAILABLE, true)
            intent.putExtra(
                AppConstants.CHAT_USER_NAME,
                name
            )
            startActivityForResult(intent, AppConstants.CHAT_FRAGMENT_CODE)
        }
    }

    private fun connectWebSocket() {
        val request = Request.Builder().url(Urls.WEB_SOCKET_URL).build()
        customWebSocketListener = CustomWebSocketListener.getInstance(sharedPrefsHelper)
        val okHttpClientBuilder = OkHttpClient.Builder()
        val webSocket1 = okHttpClientBuilder.build()
        val webSocket = webSocket1.newWebSocket(request, customWebSocketListener)
        customWebSocketListener?.setWebSocket(webSocket)
        customWebSocketListener?.setOfferListener(this)
        webSocket1.dispatcher().executorService().shutdown()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = 0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketIO.disconnectSocket()
    }

    override fun offerCallback(jsonObject: JSONObject) {
        when (jsonObject.getString(AppConstants.TYPE)) {
            AppConstants.OFFER -> {
                Debugger.e("moezzBhai", "offer : $jsonObject")
                if (jsonObject.getBoolean("isVideo")) {
                    startNewActivity(VideoCallActivity::class.java, jsonObject)
                } else {
                    startNewActivity(AudioCallActivity::class.java, jsonObject)
                }
            }
        }
    }

    private fun startNewActivity(mClass: Class<*>, jsonObject: JSONObject) {
        val intent = Intent(this, mClass)
        intent.putExtra(AppConstants.JSON, jsonObject.toString())
        startActivity(intent)
        overridePendingTransition(R.anim.bottom_up, R.anim.anim_nothing)
    }
}
