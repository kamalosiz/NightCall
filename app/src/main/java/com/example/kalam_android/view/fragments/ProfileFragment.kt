package com.example.kalam_android.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ProfileFragmentBinding
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.wrapper.GlideDownloder
import kotlinx.android.synthetic.main.content_profile.view.*
import javax.inject.Inject

class ProfileFragment : Fragment() {

    private lateinit var binding: ProfileFragmentBinding

    private var userName: String = ""
    private var userImage: String = ""
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.profile_fragment, container, false
        )
        MyApplication.getAppComponent(activity as Context).doInjection(this)
        setUserData()
        toolbarTextAppearance()
        return binding.root
    }

    private fun setUserData() {
        userName =
            sharedPrefsHelper.getUser()?.firstname + " " + sharedPrefsHelper.getUser()?.lastname
        userImage = sharedPrefsHelper.getUser()?.profile_image.toString()
        binding.content.tvName.text = userName
        GlideDownloder.load(
            activity,
            binding.ivUserImage,
            userImage,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        binding.content.tvEmail.text = sharedPrefsHelper.getUser()?.email.toString()
//        binding.content.tvPhone.text = sharedPrefsHelper.getNumber().toString()
    }

    private fun toolbarTextAppearance() {
        binding.collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandAppBar)
        binding.collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapseAppBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}