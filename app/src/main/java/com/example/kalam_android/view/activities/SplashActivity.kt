package com.example.kalam_android.view.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    private val TAG = this.javaClass.simpleName

    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MyApplication.getAppComponent(this).doInjection(this)
        val chatId = intent.getStringExtra("chat_id")
        logE("Chat ID starting: $chatId")
        val name = intent.getStringExtra("sender_name")

        var intent = Intent(this, LoginActivity::class.java)
        if (sharedPrefsHelper.isLoggedIn()) {
            intent = Intent(this, MainActivity::class.java)
            if (chatId?.isNotEmpty() == true) {
                intent.putExtra(AppConstants.CHAT_ID, chatId.toInt())
                intent.putExtra(AppConstants.IS_FROM_OUTSIDE, true)
                intent.putExtra(AppConstants.CHAT_USER_NAME, name)
            }
        }
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
