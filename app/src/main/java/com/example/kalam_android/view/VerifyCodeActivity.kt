package com.example.kalam_android.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityVerifyCodeBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast

class VerifyCodeActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityVerifyCodeBinding
    val code = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_code)
        binding.btnBack.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
        binding.tvResendCode.setOnClickListener(this)
        //For Testing
//        Debugger.e("VerifyCodeActivity", intent.getStringExtra(AppConstants.VERIFICATION_CODE))

        binding.etCode.setText(intent.getIntExtra(AppConstants.VERIFICATION_CODE, 0).toString())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> finish()
            R.id.btnVerify -> {
//                toast(this, "Verified")
                startActivity(Intent(this, CreateProfileActivity::class.java))

            }
            R.id.tvResendCode -> {
                toast(this, "Code resend successfully")
            }
        }
    }

}
