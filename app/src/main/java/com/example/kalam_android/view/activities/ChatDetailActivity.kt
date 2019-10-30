package com.example.kalam_android.view.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.localdb.ContactsEntityClass
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.model.Contacts
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject

class ChatDetailActivity : BaseActivity() {
    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatId: Int? = null
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var binding: ActivityChatDetailBinding
    lateinit var viewModel: ChatMessagesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        gettingChatId()
        logE("ChatID: $chatId")
    }

    fun gettingChatId() {
        val isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        val params = HashMap<String, String>()
        logE("isFromChatFragment : $isFromChatFragment")
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            logE("chat id in if: $chatId")
            params["chat_id"] = chatId.toString()
            viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)

        } else {
            val id = intent.getStringExtra(AppConstants.RECEIVER_ID)
           /* val socketParams = HashMap<String, String>()
            socketParams["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
            socketParams["receiver_id"] = id*/
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", id)
//            val gson = Gson()
            SocketIO.socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                Debugger.e(TAG, "ID $chatId")
                this.chatId = chatId
            })
//            logE("Json: ${gson.toJson(socketParams).toString().replace("\"","")}")
            params["chat_id"] = chatId.toString()
            viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
        }
    }

    private fun consumeResponse(apiResponse: ApiResponse<ChatMessagesResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                logE("Loading Chat Messages")
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                renderResponse(apiResponse.data as ChatMessagesResponse)
                logE("+${apiResponse.data}")
            }
            Status.ERROR -> {
                binding.pbCenter.visibility = View.GONE
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderResponse(response: ChatMessagesResponse?) {
        logE("response: $response")
        response?.let {

        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}
