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
import com.example.kalam_android.localdb.AllChatEntityClass
import com.example.kalam_android.localdb.ContactsEntityClass
import com.example.kalam_android.repository.model.AllChatListResponse
import com.example.kalam_android.repository.model.ChatListData
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.adapter.AdapterForContacts
import com.example.kalam_android.view.adapter.AllChatListAdapter
import com.example.kalam_android.viewmodel.AllChatListViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.SocketIO
import org.json.JSONObject
import java.util.*
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
    private var chatList: ArrayList<ChatListData> = ArrayList()
        private var chatListLocal: List<AllChatEntityClass> = ArrayList()
    var position = -1

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

        /* //Local
         viewModel.allChatLocalResponse().observe(this, Observer {
             consumeLocalResponse(it)
         })
         if (sharedPrefsHelper.isAllChatsItemsSynced()) {
             viewModel.getAllchatItemFromDB()
             logE("loaded from local db")
         } else {
             hitAllChatApi()
             logE("loaded from live server")
         }*/
        hitAllChatApi()

        binding.chatRecycler.adapter = AllChatListAdapter(activity as Context, this, chatList)
        SocketIO.setListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            hitAllChatApi()
        }
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

    private fun consumeLocalResponse(apiResponse: ApiResponse<List<AllChatEntityClass>>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                renderLocalResponse(apiResponse.data)
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

    private fun renderLocalResponse(list: List<AllChatEntityClass>?) {
        logE("renderLocalResponse: $list")
        if (list?.isNotEmpty() == true) {
//            chatListLocal = list
            for (x in list) {
                logE("Added to list")
                chatList.add(
                    ChatListData(
                        x.chatID,
                        x.updated_at,
                        x.unix_time,
                        x.firstname,
                        x.lastname,
                        x.profile_image,
                        x.message,
                        x.un_read_count
                    )
                )
            }
        }
        binding.chatRecycler.adapter =
            AllChatListAdapter(activity as Context, this, chatList)
        (binding.chatRecycler.adapter as AllChatListAdapter).notifyDataSetChanged()
    }

    private fun renderResponse(response: AllChatListResponse?) {
        logE("socketResponse: $response")
        response?.let {
            if (it.data?.isNotEmpty() == true) {
                it.data.reverse()
                chatList.clear()
                chatList = it.data
                binding.chatRecycler.adapter =
                    AllChatListAdapter(activity as Context, this, chatList)
                (binding.chatRecycler.adapter as AllChatListAdapter).notifyDataSetChanged()
                binding.tvNoChat.visibility = View.GONE
                val entityClass = ArrayList<AllChatEntityClass>()
                for (x in chatList) {
                    entityClass.add(
                        AllChatEntityClass(
                            0, x.chat_id, x.updated_at, x.unix_time, x.firstname,
                            x.lastname, x.profile_image, x.message, x.un_read_count
                        )
                    )
                }

                viewModel.addAllChatItemsToDB(entityClass)
                sharedPrefsHelper.allChatItemSynced()
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

    override fun socketResponse(jsonObject: JSONObject) {
        logE("call Api")
        activity?.runOnUiThread {
            hitAllChatApi()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.CHAT_FRAGMENT_CODE -> {
                    /* val long = System.currentTimeMillis() / 1000L
                     for (x in chatListLocal.indices) {
                         logE("IDs ${chatListLocal[x].id}")
                     }
                     logE("Unix time $long  Id ${chatListLocal[position].id}")
                     logE("position $position")
                     viewModel.updateItemToDB(
                         long.toString(), "Done it",
                         chatListLocal[position].id
                     )
                     Collections.swap(chatListLocal, position, 0)
                     (binding.chatRecycler.adapter as AllChatListAdapter).notifyItemMoved(
                         position,
                         0
                     )
                     binding.chatRecycler.scrollToPosition(0)*/

                    hitAllChatApi()
                    SocketIO.setListener(this)
                }
            }
        }
    }

    override fun myOnClick(view: View, position: Int) {
        when (view.id) {
            R.id.rlItem -> {
//                this.position = position
                val item = chatList[position]
                val intent = Intent(activity, ChatDetailActivity::class.java)
                intent.putExtra(AppConstants.CHAT_ID, item.chat_id)
                intent.putExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, true)
                intent.putExtra(
                    AppConstants.CHAT_USER_NAME,
                    StringBuilder(item.firstname).append(" ").append(item.lastname).toString()
                )
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, item.profile_image)
                startActivityForResult(
                    intent,
                    AppConstants.CHAT_FRAGMENT_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (sharedPrefsHelper[AppConstants.IS_FROM_CONTACTS, 0] == 1) {
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
            hitAllChatApi()
            SocketIO.setListener(this)
            logE("OnResume of Chat Fragment")
        }
    }
}