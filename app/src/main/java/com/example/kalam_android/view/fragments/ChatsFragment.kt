package com.example.kalam_android.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.callbacks.ResultVoiceToText
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.databinding.ChatsFragmentBinding
import com.example.kalam_android.helper.MyVoiceToTextHelper
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.repository.model.AllChatListResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.adapter.AllChatListAdapter
import com.example.kalam_android.viewmodel.AllChatListViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.SocketIO
import org.json.JSONObject
import javax.inject.Inject

class ChatsFragment : Fragment(), SocketCallback, MyClickListener,
    View.OnTouchListener, ResultVoiceToText {

    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ChatsFragmentBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: AllChatListViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatList: ArrayList<ChatListData> = ArrayList()
    private var chatIDs: ArrayList<Int> = ArrayList()
    var position = -1
    private var isRefresh = false
    private var myVoiceToTextHelper: MyVoiceToTextHelper? = null
    var fromSearch = 0

    @SuppressLint("ClickableViewAccessibility")
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
        /* viewModel.allChatLocalResponse().observe(this, Observer {
             consumeLocalResponse(it)
         })*/
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        /*if (sharedPrefsHelper.isAllChatsItemsSynced()) {
            viewModel.getAllchatItemFromDB()
            logE("loaded from local db")
        } else {
            hitAllChatApi()
            logE("loaded from live server")
        }*/
        hitAllChatApi()

        binding.chatRecycler.adapter = AllChatListAdapter(activity as Context, this)
//        SocketIO.setSocketCallbackListener(this)
        SocketIO.getInstance().setSocketCallbackListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            isRefresh = true
            hitAllChatApi()
        }
        binding.fabSpeech.isClickable = false
        binding.fabSpeech.setOnTouchListener(this)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    fromSearch = 0
                    binding.pbCenter.visibility = View.VISIBLE
                    hitAllChatApi()
                } else {
                    fromSearch = 1
                    hitSearchMessage(s.toString())
                }
            }
        })
        myVoiceToTextHelper = MyVoiceToTextHelper(activity as Activity, this)
        myVoiceToTextHelper?.checkPermissionForVoiceToText()
        return binding.root
    }

    private fun consumeResponse(apiResponse: ApiResponse<AllChatListResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                binding.pbCenter.visibility = View.VISIBLE
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
//                toast(activity, "Something went wrong please try again")
            }
            else -> {
            }
        }
    }
    /* private fun consumeLocalResponse(apiResponse: ApiResponse<List<ChatListData>>?) {
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
     }*/

    /* private fun renderLocalResponse(list: List<ChatListData>?) {
         logE("renderLocalResponse: $list")
         list?.let {
             chatList.clear()
             chatIDs.clear()

             if (list.isNotEmpty()) {
                 binding.fabSpeech.isClickable = true
             }
             chatList.addAll(list)
             list.forEach {
                 chatIDs.add(it.chat_id)
             }
             (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
         }
     }*/

    private fun renderResponse(response: AllChatListResponse?) {
        logE("socketResponse: $response")
        response?.let { it ->
            if (it.data?.isNotEmpty() == true) {
                it.data.reverse()
                chatList.clear()
                chatList = it.data
                /*chatIDs.clear()
                it.data.forEach {
                    chatIDs.add(it.chat_id)
                }*/
                (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
                /*if (isRefresh) {
                    isRefresh = false
                    viewModel.deleteAllChats()
                }*/
                binding.tvNoChat.visibility = View.GONE
//                viewModel.addAllChatItemsToDB(chatList)
//                sharedPrefsHelper.allChatItemSynced()
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

    private fun hitSearchMessage(query: String) {
        val params = HashMap<String, String>()
        params["query"] = query
        viewModel.hitSearchMessage(sharedPrefsHelper.getUser()?.token.toString(), params)
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

    override fun socketResponse(jsonObject: JSONObject, type: String) {
        if (type == AppConstants.NEW_MESSAGE) {
//            val gson = Gson()
//            logE("New Message : $jsonObject")
//            val newChat = gson.fromJson(jsonObject.toString(), ChatData::class.java)
//            val unixTime = System.currentTimeMillis() / 1000L
            activity?.runOnUiThread {
                /* val name = newChat.sender_name.split(" ")
                 val item = ChatListData(
                     newChat.chat_id, "", unixTime, name[0], name[1],
                     "", newChat.message, 1
                 )
                 if (chatIDs.contains(newChat.chat_id)) {
                     logE("Chat ID matched")
                     for (x in chatList.indices) {
                         if (chatList[x].chat_id == newChat.chat_id) {
                             chatList[x].un_read_count += 1
                             modifyItem(x, item.message.toString(), unixTime, chatList[x].un_read_count)
                         }
                     }
                 } else {
                     logE("This chat is not present")
                     if (chatList.size == 0) {
                         binding.tvNoChat.visibility = View.GONE
                     }
                     chatList.add(0, item)
                     chatIDs.add(newChat.chat_id)
                     (binding.chatRecycler.adapter as AllChatListAdapter).newChatInserted(chatList)
                     viewModel.insertChat(item)
                 }*/
                hitAllChatApi()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.CHAT_FRAGMENT_CODE -> {
                    /*val isSeen = data?.getBooleanExtra(AppConstants.IsSEEN, false)
                    if (isSeen == true) {
                        logE("If Part")
                        if (chatList[position].un_read_count != 0) {
                            chatList[position].un_read_count = 0
                            (binding.chatRecycler.adapter as AllChatListAdapter).updateReadCount(
                                chatList, position
                            )
                            viewModel.updateReadCountDB(chatList[position].chat_id, 0)
                        }
                    } else {
                        logE("Else Part")
                        val lastMessage = data?.getStringExtra(AppConstants.LAST_MESSAGE)
                        val lastMsgTime = data?.getStringExtra(AppConstants.LAST_MESSAGE_TIME)
                        modifyItem(position, lastMessage.toString(), lastMsgTime?.toLong(), 0)
                    }*/
                    binding.etSearch.setText("")
                    logE("onActivityResult of chats Fragment is called")
                    SocketIO.getInstance().setSocketCallbackListener(this)
//                    hitAllChatApi()
                }

            }
        }
    }

    /* private fun modifyItem(position: Int, lastMessage: String, unixTime: Long?, unReadCount: Int) {
         chatList[position].message = lastMessage
         chatList[position].unix_time = unixTime
         chatList[position].un_read_count = unReadCount
         val item = chatList[position]
         chatList.remove(chatList[position])
         chatList.add(0, item)
         (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
         binding.chatRecycler.scrollToPosition(0)
         viewModel.updateItemToDB(
             unixTime.toString(), lastMessage,
             item.chat_id, unReadCount
         )
     }*/

    override fun myOnClick(view: View, position: Int) {
        when (view.id) {
            R.id.rlItem -> {
                startActivity(position)
            }
        }
    }

    fun startActivity(position: Int) {
        val item = chatList[position]
        this.position = position
        val name = if (item.nickname.isNullOrEmpty()) {
            StringBuilder(item.firstname).append(" ").append(item.lastname).toString()
        } else {
            item.nickname.toString()
        }
        val intent = Intent(activity, ChatDetailActivity::class.java)
        intent.putExtra(AppConstants.CHAT_ID, item.chat_id)
        intent.putExtra(AppConstants.IS_CHATID_AVAILABLE, true)
        intent.putExtra(AppConstants.CALLER_USER_ID, item.user_id)
        intent.putExtra(AppConstants.CHAT_USER_NAME, name)
        intent.putExtra(AppConstants.CHAT_USER_PICTURE, item.profile_image)
        if (fromSearch == 1) {
            intent.putExtra(AppConstants.MSG_ID, item.last_message_id)
            intent.putExtra(AppConstants.FROM_SEARCH, fromSearch)
        }
        startActivityForResult(
            intent,
            AppConstants.CHAT_FRAGMENT_CODE
        )
    }

    override fun onResume() {
        super.onResume()
        if (sharedPrefsHelper[AppConstants.IS_FROM_CONTACTS, 0] == 1) {
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
            isRefresh = true
            hitAllChatApi()
            SocketIO.getInstance().setSocketCallbackListener(this)
            logE("OnResume of Chat Fragment")
        }
    }

    override fun onPause() {
        super.onPause()
        myVoiceToTextHelper?.destroy()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                myVoiceToTextHelper?.startVoiceToText()
            }
            MotionEvent.ACTION_UP -> {
                myVoiceToTextHelper?.stopVoiceToText()

            }


        }
        return v?.onTouchEvent(event) ?: true
    }

    override fun onResultVoiceToText(list: ArrayList<String>) {

        if (list.size > 0 && chatList.size > 0) {
            list.forEach { speechName: String ->

                for (i in chatList.indices) {
                    if (speechName.equals(
                            "chat with" + " " + chatList[i].firstname + " " + chatList[i].lastname,
                            true
                        ) || speechName.equals(
                            "chat with" + " " + chatList[i].firstname,
                            true
                        ) || speechName.equals(
                            "chat with" + " " + chatList[i].lastname,
                            true
                        )
                    ) {
                        toast(
                            activity,
                            "chat with ${chatList[i].firstname + " " + chatList[i].lastname}"
                        )
                        startActivity(i)
                        return
                    }
                }
            }
        }
    }
}