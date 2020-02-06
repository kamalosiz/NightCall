package com.example.kalam_android.view.fragments

import android.annotation.SuppressLint
import android.app.Activity.RECEIVER_VISIBLE_TO_INSTANT_APPS
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ProfileFragmentBinding
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.repository.model.UserProfile
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.EditUserProfileActivity
import com.example.kalam_android.view.adapter.AdapterForProfilePhotos
import com.example.kalam_android.view.adapter.AdapterForProfileVideos
import com.example.kalam_android.view.adapter.ViewPagerAdapterFragment
import com.example.kalam_android.viewmodel.UserProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import kotlinx.android.synthetic.main.item_for_add_my_status.view.*
import kotlinx.android.synthetic.main.layout_for_user_profile_overview.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.tvName
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var binding: ProfileFragmentBinding

    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: UserProfileViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var list: ArrayList<ProfileData> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.profile_fragment, container, false
        )
        MyApplication.getAppComponent(activity!!).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(UserProfileViewModel::class.java)
        viewModel.userProfileResponse().observe(this, Observer {
            consumeApiResponse(it)
        })
        val params = HashMap<String, String>()
        params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
        viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
        onClickListener()
        return binding.root
    }

    private fun onClickListener() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(activity!!, EditUserProfileActivity::class.java)
            intent.putExtra(AppConstants.USER_DATA, list)
            startActivityForResult(intent, AppConstants.UPDATE_PROFILE)
        }
    }

    private fun consumeApiResponse(response: ApiResponse<UserProfile>) {
        when (response.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                Debugger.e("Profile Data : ", "${response.data}")
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
        if (list.size > 0) {
            list.clear()
        }
        list.addAll(userList)
        binding.tvUsername.text = userList[0].firstname + " " + userList[0].lastname
        binding.tvJobDescription.text = userList[0].work
        GlideDownloader.load(
            activity?.applicationContext,
            binding.ivProfile.ivProfile,
            userList[0].profile_image,
            R.color.grey,
            R.color.grey
        )
        GlideDownloader.load(
            activity?.applicationContext,
            binding.ivUserProfile.ivUserProfile,
            userList[0].wall_image,
            R.color.darkGrey,
            R.color.darkGrey
        )
        binding.viewpager.adapter = ViewPagerAdapterFragment(childFragmentManager, list)
        binding.tabs.setupWithViewPager(binding.viewpager)
        (binding.viewpager.adapter as ViewPagerAdapterFragment).notifyDataSetChanged()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPDATE_PROFILE) {

                val params = HashMap<String, String>()
                params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
                viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
            }
        }
    }
}