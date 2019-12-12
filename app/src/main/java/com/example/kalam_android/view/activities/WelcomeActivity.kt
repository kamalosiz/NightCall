package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityWelcomeBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.wrapper.GlideDownloder
import java.lang.StringBuilder
import javax.inject.Inject

class WelcomeActivity : BaseActivity() {
    lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
        binding.btnBack.setOnClickListener { finish() }
        GlideDownloder.load(
            this, binding.ivUserImage, intent.getStringExtra(AppConstants.PROFILE_IMAGE_KEY),
            R.drawable.dummy_placeholder, R.drawable.dummy_placeholder
        )
        binding.tvWelcomeUser.text =
            StringBuilder("Welcome ").append(intent.getStringExtra(AppConstants.USER_NAME))
                .append("\n").append("Have a start your Kalam")
        binding.btnFinish.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
