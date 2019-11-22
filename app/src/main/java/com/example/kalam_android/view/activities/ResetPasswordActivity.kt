package com.example.kalam_android.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
