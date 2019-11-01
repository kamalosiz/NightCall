package com.example.kalam_android.view.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityFindsFriendsBinding
import com.example.kalam_android.repository.model.FindFriendList
import com.example.kalam_android.repository.model.FindFriends
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.AdapterForFindFriends
import com.example.kalam_android.viewmodel.FindFriendsViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import javax.inject.Inject

class FindsFriendsActivity : BaseActivity(), TextWatcher {


    private lateinit var binding: ActivityFindsFriendsBinding
    private lateinit var adapterForFindFriends: AdapterForFindFriends
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: FindFriendsViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var friendList: MutableList<FindFriendList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_finds_friends)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(FindFriendsViewModel::class.java)
        viewModel.searchResponse().observe(this, Observer {
            consumeApiResponse(it)
        })
        binding.etSearch.addTextChangedListener(this)
        createdAdapter()
    }

    private fun createdAdapter() {
        adapterForFindFriends = AdapterForFindFriends(this, friendList)
        binding.rvSearchFriends.adapter = adapterForFindFriends
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val params = HashMap<String, String>()
        params["query"] = s.toString()
        viewModel.hitSearchFriends(sharedPrefsHelper.getUser()?.token.toString(), params)
    }

    private fun consumeApiResponse(response: ApiResponse<FindFriends>) {
        logE("response: $response")
        when (response.status) {

            Status.LOADING -> {
                binding.pbCenter.visibility = View.VISIBLE
            }
            Status.SUCCESS -> {
                logE("data: ${response.data}")
                binding.pbCenter.visibility = View.GONE
                renderLocalResponse(response.data)

            }
            Status.ERROR -> {
                binding.pbCenter.visibility = View.GONE
                toast(this, "Something went wrong please try again")
                logE("Error:${response.error}")
            }
            else -> {

            }

        }

    }

    private fun renderLocalResponse(response: FindFriends?) {
        response?.let { it ->
            it.data?.let {
                (binding.rvSearchFriends.adapter as AdapterForFindFriends).updateList(it)
            }

        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

}
