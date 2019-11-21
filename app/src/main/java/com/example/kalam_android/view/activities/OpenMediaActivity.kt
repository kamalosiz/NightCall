package com.example.kalam_android.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityOpenMediaBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.wrapper.GlideDownloder


class OpenMediaActivity : AppCompatActivity() {

    lateinit var binding: ActivityOpenMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_media)
        val type = intent.getStringExtra(AppConstants.CHAT_TYPE)
        val file = intent.getStringExtra(AppConstants.CHAT_FILE)
        val name = intent.getStringExtra(AppConstants.USER_NAME)
        val profile = intent.getStringExtra(AppConstants.PROFILE_IMAGE_KEY)
        binding.header.ivAudio.visibility = View.GONE
        binding.header.ivMore.visibility = View.GONE
        binding.header.ivVideo.visibility = View.GONE
        binding.header.rlBack.setOnClickListener {
            onBackPressed()
        }
        binding.header.llProfile.setOnClickListener {
            val intent = Intent(this@OpenMediaActivity, UserProfileActivity::class.java)
            intent.putExtra(AppConstants.CHAT_USER_NAME, name)
            intent.putExtra(AppConstants.CHAT_USER_PICTURE, profile)
            val transitionName = getString(R.string.profile_trans)
            val options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    binding.header.ivProfileImage,
                    transitionName
                )
            ActivityCompat.startActivity(this, intent, options.toBundle())
        }

        GlideDownloder.load(
            this,
            binding.header.ivProfileImage,
            profile?.toString(),
            R.color.grey,
            R.color.grey
        )
        binding.header.tvName.text = name?.toString()
        if (type == AppConstants.IMAGE_MESSAGE) {
            GlideDownloder.load(
                this,
                binding.image,
                file?.toString(),
                R.color.grey,
                R.color.grey
            )
        }
    }
}
