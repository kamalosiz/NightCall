package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.util.SharedPrefsHelper
import javax.inject.Inject

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MyApplication.getAppComponent(this).doInjection(this)
        var intent = Intent(this, LoginActivity::class.java)
        if (sharedPrefsHelper.isLoggedIn()) {
            intent = Intent(this, MainActivity::class.java)
        }
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, 2000)
    }
}
