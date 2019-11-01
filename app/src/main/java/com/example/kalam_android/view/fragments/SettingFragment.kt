package com.example.kalam_android.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.FragmentSettingBinding
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.FindsFriendsActivity
import com.example.kalam_android.view.activities.SelectLanguage
import javax.inject.Inject

class SettingFragment : Fragment(), View.OnClickListener {

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
                startActivity(Intent(activity, SelectLanguage::class.java))
            }
        }
    }

}
