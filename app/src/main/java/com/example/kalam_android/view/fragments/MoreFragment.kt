package com.example.kalam_android.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.FragmentSettingBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.view.activities.FindsFriendsActivity
import com.example.kalam_android.view.activities.LoginActivity
import com.example.kalam_android.view.activities.SettingActivity
import com.example.kalam_android.wrapper.SocketIO

class MoreFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
        MyApplication.getAppComponent(activity as Context).doInjection(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvFindFriends.setOnClickListener(this)
        binding.tvChangeLanguage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvFindFriends -> {
                startActivity(Intent(activity, FindsFriendsActivity::class.java))
            }
            R.id.tvChangeLanguage -> {
                startActivityForResult(
                    Intent(activity, SettingActivity::class.java),
                    AppConstants.LOGOUT_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.LOGOUT_CODE -> {
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                }
            }
        }
    }
}
