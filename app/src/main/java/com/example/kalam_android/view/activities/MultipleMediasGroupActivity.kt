package com.example.kalam_android.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityMultipleMediasGroupBinding

class MultipleMediasGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultipleMediasGroupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_medias_group)
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }
}
