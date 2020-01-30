package com.example.kalam_android.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ProfileFragmentBinding
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.repository.model.UserProfile
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.adapter.ViewPagerAdapterFragment
import com.example.kalam_android.viewmodel.UserProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import javax.inject.Inject


class UserProfileActivity : BaseActivity() {

    private var userName: String = ""
    private var userImage: String = ""
    private var userId: String = ""
    private lateinit var binding: ProfileFragmentBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: UserProfileViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.profile_fragment)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(UserProfileViewModel::class.java)
        viewModel.userProfileResponse().observe(this, Observer {
            consumeApiResponse(it)
        })

        binding.btnEditProfile.visibility = View.GONE
        binding.lvBack.visibility = View.VISIBLE

        setUserData()
        val params = HashMap<String, String>()
        params["user_id"] = userId
        viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)

        binding.llBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setUserData() {
        if (intent != null) {
            val bundle = intent.extras
            userName = bundle?.getString(AppConstants.CHAT_USER_NAME).toString()
            userImage = bundle?.getString(AppConstants.CHAT_USER_PICTURE).toString()
            userId = bundle?.getString(AppConstants.CALLER_USER_ID).toString()
            binding.tvUsername.text = userName
            GlideDownloader.load(
                    this,
                    binding.ivProfile,
                    userImage,
                    R.color.darkGrey,
                    R.color.darkGrey
            )
        }
    }

    private fun consumeApiResponse(response: ApiResponse<UserProfile>) {
        Debugger.e("Response : ", "${response}")
        when (response.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                Debugger.e("data : ", "${response.data}")
                renderResponse(response.data?.data!!)

            }
            Status.ERROR -> {
                Debugger.e("data : ", "${response.error}")

            }
            else -> {

            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun renderResponse(userList: ArrayList<ProfileData>) {
        GlideDownloader.load(
                this,
                binding.ivUserProfile,
                userList[0].wall_image,
                R.color.darkGrey,
                R.color.darkGrey
        )
        binding.tvJobDescription.text = userList[0].work
        binding.viewpager.adapter = ViewPagerAdapterFragment(supportFragmentManager, userList)
        binding.tabs.setupWithViewPager(binding.viewpager)
        (binding.viewpager.adapter as ViewPagerAdapterFragment).notifyDataSetChanged()
    }

}
