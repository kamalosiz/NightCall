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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.callbacks.NewMessageListener
import com.example.kalam_android.databinding.ChatsFragmentBinding
import com.example.kalam_android.repository.model.AllChatListResponse
import com.example.kalam_android.repository.model.ChatListData
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.adapter.AllChatListAdapter
import com.example.kalam_android.viewmodel.AllChatListViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.SocketIO
import com.google.gson.JsonObject
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatsFragment : Fragment(), NewMessageListener, MyClickListener {

    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ChatsFragmentBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: AllChatListViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatList: ArrayList<ChatListData>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.chats_fragment, container, false
        )
        MyApplication.getAppComponent(activity as Context).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(AllChatListViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        chatList = ArrayList()
        hitAllChatApi()
        SocketIO.checkNewMessage(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            hitAllChatApi()
        }
        logE("my Number: ${sharedPrefsHelper.getUser()?.phone}")
        return binding.root
    }

    private fun consumeResponse(apiResponse: ApiResponse<AllChatListResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                renderResponse(apiResponse.data as AllChatListResponse)
                logE("consumeResponse SUCCESS : ${apiResponse.data}")
            }
            Status.ERROR -> {
                binding.pbCenter.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
                toast(activity, "Something went wrong please try again")
            }
            else -> {
            }
        }
    }

    private fun renderResponse(response: AllChatListResponse?) {
        logE("response: $response")
        response?.let {
            if (it.data?.isNotEmpty() == true) {
                it.data.reverse()
                chatList = it.data
                binding.chatRecycler.adapter =
                    AllChatListAdapter(activity as Context, this, chatList)
                (binding.chatRecycler.adapter as AllChatListAdapter).notifyDataSetChanged()
                binding.tvNoChat.visibility = View.GONE
            } else {
                binding.tvNoChat.visibility = View.VISIBLE
            }
        }
    }

    private fun hitAllChatApi() {
        val params = HashMap<String, String>()
        params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
        viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

    override fun newMessage(jsonObject: JsonObject) {
        logE("Chats Fragment New Message is called: $jsonObject")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.CHAT_FRAGMENT_CODE -> {
                    hitAllChatApi()
                }
            }
        }
    }

    override fun myOnClick(view: View, position: Int) {
        when (view.id) {
            R.id.rlItem -> {
                val item = chatList?.get(position)
                val intent = Intent(activity, ChatDetailActivity::class.java)
                intent.putExtra(AppConstants.CHAT_ID, item?.chat_id)
                intent.putExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, true)
                (activity as Activity).startActivityForResult(
                    intent,
                    AppConstants.CHAT_FRAGMENT_CODE
                )
            }
        }
    }

}