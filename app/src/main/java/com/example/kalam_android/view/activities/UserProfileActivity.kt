package com.example.kalam_android.view.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.databinding.ActivityUserProfileBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.wrapper.GlideDownloader


class UserProfileActivity : BaseActivity() {

    private var userName: String = ""
    private var userImage: String = ""
    private lateinit var binding: ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        setUserData()
        toolbarTextAppearance()
    }

    private fun setUserData() {
        if (intent != null) {
            val bundle = intent.extras
            userName = bundle?.getString(AppConstants.CHAT_USER_NAME).toString()
            userImage = bundle?.getString(AppConstants.CHAT_USER_PICTURE).toString()
            binding.collapsingToolbar.title = userName
//            binding.contentUser.tvMobileNumber.text =
            GlideDownloader.load(
                this,
                binding.ivUserImage,
                userImage,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
        }
    }

    private fun toolbarTextAppearance() {

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandAppBar)
        binding.collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapseAppBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
