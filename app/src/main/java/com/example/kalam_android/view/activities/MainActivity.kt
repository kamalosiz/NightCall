package com.example.kalam_android.view.activities

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.databinding.ActivityMainBinding
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.HomePagerAdapter
import com.example.kalam_android.viewmodel.MainViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.webrtc.AudioCallActivity
import com.example.kalam_android.webrtc.CustomWebSocketListener
import com.example.kalam_android.webrtc.VideoCallActivity
import com.example.kalam_android.wrapper.SocketIO
import com.sandrios.sandriosCamera.internal.SandriosCamera
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import kotlin.system.exitProcess


class MainActivity : BaseActivity(), WebSocketOfferCallback {

    private lateinit var binding: ActivityMainBinding
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var viewModel: MainViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MyApplication.getAppComponent(this).doInjection(this)
//        connectWebSocket()
        didPushReceived()
        SocketIO.getInstance().connectSocket(sharedPrefsHelper.getUser()?.token)
        SocketIO.getInstance().connectListeners()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        val homePagerAdapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = homePagerAdapter
        binding.viewPager.offscreenPageLimit = 4
        binding.llChat.setOnClickListener { binding.viewPager.setCurrentItem(0, true) }
        binding.llCall.setOnClickListener { binding.viewPager.setCurrentItem(1, true) }
        binding.llStories.setOnClickListener { binding.viewPager.setCurrentItem(2, true) }
        binding.llProfile.setOnClickListener { binding.viewPager.setCurrentItem(3, true) }
        binding.ivSettings.setOnClickListener { binding.viewPager.setCurrentItem(4, true) }
        binding.header.btnRight.visibility = View.GONE
        binding.ivCompose.setOnClickListener {
            startActivity(Intent(this, ContactListActivity::class.java))
        }
        viewModel.updateFcmTokenResponse().observe(this,
            Observer<ApiResponse<BasicResponse>> { t ->
                consumeUpdateFcmResponse(t)
            })

        if (sharedPrefsHelper.isNewFcmToken() == true) {
            val params = HashMap<String, String>()
            params["fcm_token"] = sharedPrefsHelper.getFCMToken().toString()
            viewModel.hitUpdateFcmToken(sharedPrefsHelper.getUser()?.token.toString(), params)
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

    private fun consumeUpdateFcmResponse(apiResponse: ApiResponse<BasicResponse>?) {
        when (apiResponse?.status) {
            Status.LOADING -> {
                Debugger.e(TAG, "consumeUpdateFcmResponse LOADING")
            }
            Status.SUCCESS -> {
                Debugger.e(TAG, "consumeUpdateFcmResponse SUCCESS")
                toast(apiResponse.data?.message.toString())
                apiResponse.data?.let {
                    sharedPrefsHelper.saveIsNewFcmToken(false)
                }
            }
            Status.ERROR -> {
                Debugger.e(
                    TAG,
                    "consumeUpdateFcmResponse ERROR: " + apiResponse.error.toString()
                )
            }
            else -> {
            }
        }
    }

    private fun connectWebSocket() {
        val request = Request.Builder().url(Urls.WEB_SOCKET_URL).build()
        val okHttpClientBuilder = OkHttpClient.Builder()
        val webSocket1 = okHttpClientBuilder.build()
        val webSocket =
            webSocket1.newWebSocket(request, CustomWebSocketListener.getInstance(sharedPrefsHelper))
        CustomWebSocketListener.getInstance(sharedPrefsHelper).setWebSocket(webSocket)
        CustomWebSocketListener.getInstance(sharedPrefsHelper).setOfferListener(this)
        webSocket1.dispatcher().executorService().shutdown()
    }

    private fun didPushReceived() {
        val isFromCall = intent.getBooleanExtra(AppConstants.IS_FROM_PUSH, false)
        if (isFromCall) {
            val jsonString = intent.getStringExtra(AppConstants.JSON)
            val jsonObject = JSONObject(jsonString)
//            val connectedUserId = intent.getStringExtra(AppConstants.CONNECTED_USER_ID)
//            customWebSocketListener?.setPushData(connectedUserId, isFromCall)

            if (jsonObject.getBoolean("isVideo")) {
                startNewActivity(VideoCallActivity::class.java, jsonObject, true)
            } else {
                startNewActivity(AudioCallActivity::class.java, jsonObject, true)
            }
            CustomWebSocketListener.getInstance(sharedPrefsHelper).setOfferListener(this)
        } else {
            connectWebSocket()
        }
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
        SocketIO.getInstance().disconnectSocket()
    }

    override fun offerCallback(jsonObject: JSONObject) {
        when (jsonObject.getString(AppConstants.TYPE)) {
            AppConstants.OFFER -> {
                Debugger.e("offerCallback", "json : $jsonObject")
                if (jsonObject.getBoolean("isVideo")) {
                    startNewActivity(VideoCallActivity::class.java, jsonObject, false)
                } else {
                    startNewActivity(AudioCallActivity::class.java, jsonObject, false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun startNewActivity(mClass: Class<*>, jsonObject: JSONObject, isFromPush: Boolean) {
        val intent = Intent(this, mClass)
        intent.putExtra(AppConstants.JSON, jsonObject.toString())
        if (isFromPush) {
            startActivityForResult(intent, AppConstants.CODE_FROM_PUSH)
        } else {
            startActivity(intent)
        }
        overridePendingTransition(R.anim.bottom_up, R.anim.anim_nothing)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.CODE_FROM_PUSH -> {
                    finishAndRemoveTask()
                }
            }
        }
    }
}
