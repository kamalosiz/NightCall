package com.example.kalam_android.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.callbacks.ResultVoiceToText
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.callbacks.StatusCallback
import com.example.kalam_android.databinding.ChatsFragmentBinding
import com.example.kalam_android.helper.MyVoiceToTextHelper
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.repository.model.AllChatListResponse
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
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ChatsFragment : Fragment(), SocketCallback, MyClickListener,
    View.OnTouchListener, ResultVoiceToText, StatusCallback {

    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ChatsFragmentBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: AllChatListViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatList: ArrayList<ChatListData> = ArrayList()
    private var chatIDs: ArrayList<Int> = ArrayList()
    private var userIds: ArrayList<Int> = ArrayList()
    var position = -1
    private var myVoiceToTextHelper: MyVoiceToTextHelper? = null
    private var fromSearch = 0


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

        viewModel.allChatLocalResponse().observe(this, Observer {
            consumeLocalResponse(it)
        })
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        viewModel.getAllchatItemFromDB()
        hitAllChatApi()
        binding.chatRecycler.adapter = AllChatListAdapter(activity as Context, this)
        ((binding.chatRecycler.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        SocketIO.getInstance().setSocketCallbackListener(this)
        SocketIO.getInstance().setStatusCallbackListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            hitAllChatApi()
        }
        binding.fabSpeech.setOnTouchListener(this)
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                logE("onTextChanged length is not 0")
                fromSearch = 1
                hitSearchMessage(binding.etSearch.text.toString())
            }
            false
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    fromSearch = 0
                    logE("onTextChanged length is 0")
                    viewModel.getAllchatItemFromDB()
                    hitAllChatApi()
                }
            }
        })
        return binding.root
    }

    private fun createIdsJson(list: MutableList<Int>): JsonArray {
        val jsonArray = JsonArray()
        for (x in list.indices) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", list[x])
            jsonObject.addProperty("status", 0)
            jsonArray.add(jsonObject)
        }
        logE("Json Array : $jsonArray")
        return jsonArray
    }

    private fun consumeResponse(apiResponse: ApiResponse<AllChatListResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
//                binding.pbCenter.visibility = View.VISIBLE
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                logE("consumeResponse : ${apiResponse.data}")
                renderResponse(apiResponse.data as AllChatListResponse)
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

    private fun consumeLocalResponse(apiResponse: ApiResponse<List<ChatListData>>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                renderLocalResponse(apiResponse.data)
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

    private fun renderLocalResponse(list: List<ChatListData>?) {
        logE("renderLocalResponse: $list")
        list?.let {
            if (fromSearch == 0) {
                logE("fromSearch == 0 in renderLocalResponse")
                chatList.clear()
                chatIDs.clear()

                if (list.isNotEmpty()) {
                    binding.fabSpeech.isClickable = true
                }
                chatList.addAll(list)
                list.forEach {
                    chatIDs.add(it.chat_id)
                }
                logE("Chats are added from local")
                (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
            }
        }
    }

    private fun renderResponse(response: AllChatListResponse?) {
        logE("renderResponse: $response")
        response?.let { it ->
            if (it.data?.isNotEmpty() == true) {
                it.data.reverse()
                chatList.clear()
                chatList = it.data
                userIds.clear()
                binding.tvNoChat.visibility = View.GONE
                (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
                if (fromSearch == 0) {
                    logE("fromSearch == 0 in renderResponse")
                    chatIDs.clear()
                    it.data.forEach {
                        chatIDs.add(it.chat_id)
                        userIds.add(it.user_id)
                    }
                    SocketIO.getInstance().getUserStatuses(createIdsJson(userIds))
                    viewModel.deleteAllChats()
                    viewModel.addAllChatItemsToDB(chatList)
                    sharedPrefsHelper.allChatItemSynced()
                }
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
        activity?.runOnUiThread {
            when (type) {
                AppConstants.NEW_MESSAGE -> {
                    val gson = Gson()
                    logE("New Message : $jsonObject")
                    val newChat = gson.fromJson(jsonObject.toString(), ChatData::class.java)
                    val unixTime = System.currentTimeMillis() / 1000L
                    activity?.runOnUiThread {
                        //                        val name = newChat.sender_name.split(" ")
                        val item = ChatListData(
                            newChat.chat_id,
                            "",
                            unixTime.toDouble(),
                            newChat.sender_name, "",
                            newChat.profile_image.toString(),
                            newChat.message,
                            1,
                            newChat.sender_id!!,
                            newChat.sender_name,
                            newChat.id,
                            newChat.is_read,
                            newChat.receiver_id
                        )
                        if (chatIDs.contains(newChat.chat_id)) {
                            logE("Chat ID matched")
                            val objItem = chatList.single { it.chat_id == newChat.chat_id }
                            val x = chatList.indexOf(objItem)
                            objItem.un_read_count += 1
                            modifyItem(
                                x,
                                item.message.toString(),
                                unixTime,
                                chatList[x].un_read_count,
                                item.user_id,
                                item.is_read
                            )
                        } else {
                            logE("This chat is not present")
                            if (chatList.size == 0) {
                                binding.tvNoChat.visibility = View.GONE
                            }
                            item.is_read = 3
                            chatList.add(0, item)
                            chatIDs.add(newChat.chat_id)

                            (binding.chatRecycler.adapter as AllChatListAdapter).newChatInserted(
                                chatList
                            )
                            viewModel.insertChat(item)
                        }
                    }
                }
                AppConstants.ALL_MESSAGES_READ -> {
                    logE("ALL_MESSAGES_READ in ChatsFragment: $jsonObject")
                    val chatId = jsonObject.getString("chat_id").toInt()
                    updateStatus(chatId, 2)
                }
                AppConstants.SEEN_MESSAGE -> {
                    logE("SEEN_MESSAGE in ChatsFragment: $jsonObject")
                    val chatId = jsonObject.getString("chat_id").toInt()
                    updateStatus(chatId, 2)
                }
                AppConstants.MESSAGE_DELIVERED -> {
                    logE("MESSAGE_DELIVERED in ChatsFragment : $jsonObject")
                    val chatId = jsonObject.getString("chat_id").toInt()
                    updateStatus(chatId, 1)
                }
                AppConstants.SEND_MESSAGE -> {
                    logE("SEND_MESSAGE in ChatsFragment : $jsonObject")
                    val isDelivered = jsonObject.getBoolean("delivered")
                    val chatId = jsonObject.getString("chat_id").toInt()
                    if (isDelivered) {
                        updateStatus(chatId, 1)
                    }
                }
                AppConstants.USER_STATUS -> {
                    logE("USER_STATUS : $jsonObject")
                    val userId = jsonObject.getInt("user_id")
                    val status = jsonObject.getInt("status")
                    for (x in chatList.indices) {
                        if (chatList[x].user_id == userId) {
                            (binding.chatRecycler.adapter as AllChatListAdapter).updateOnlineStatus(
                                x, status
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateStatus(chatId: Int, isRead: Int) {
        for (x in chatIDs.indices) {
            if (chatIDs[x] == chatId) {
                chatList[x].is_read = isRead
                (binding.chatRecycler.adapter as AllChatListAdapter).updateItem(
                    chatList, x
                )
                viewModel.updateChatItemDB(chatIDs[x], isRead)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.CHAT_FRAGMENT_CODE -> {
                    viewModel.getAllchatItemFromDB()
                    hitAllChatApi()
                    logE("onActivityResult of chats Fragment is called")
                    SocketIO.getInstance().setSocketCallbackListener(this)
                    SocketIO.getInstance().getUserStatuses(createIdsJson(userIds))
                }
            }
        }
    }

    private fun modifyItem(
        position: Int,
        lastMessage: String,
        unixTime: Long?,
        unReadCount: Int,
        senderId: Int?,
        isRead: Int?
    ) {
        chatList[position].message = lastMessage
        chatList[position].unix_time = unixTime?.toDouble()
        chatList[position].un_read_count = unReadCount
        chatList[position].sender_id = senderId
        chatList[position].is_read = isRead
        val item = chatList[position]
        chatList.remove(chatList[position])
        chatList.add(0, item)
        (binding.chatRecycler.adapter as AllChatListAdapter).updateList(chatList)
        binding.chatRecycler.scrollToPosition(0)
        viewModel.updateItemToDB(
            unixTime.toString(), lastMessage,
            item.chat_id, unReadCount, item.sender_id, isRead
        )
    }

    override fun myOnClick(view: View, position: Int) {
        when (view.id) {
            R.id.rlItem -> {
                startActivity(position)
            }
        }
    }

    fun startActivity(position: Int) {
        val item = chatList[position]
        logE("startActivity :$item")
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
        myVoiceToTextHelper = MyVoiceToTextHelper(activity as Activity, this)
        myVoiceToTextHelper?.checkPermissionForVoiceToText()
        if (sharedPrefsHelper[AppConstants.IS_FROM_CONTACTS, 0] == 1) {
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
            hitAllChatApi()
            SocketIO.getInstance().setSocketCallbackListener(this)
            logE("OnResume of Chat Fragment")
        }
    }

    override fun onPause() {
        super.onPause()
        myVoiceToTextHelper?.destroy()
    }

    @SuppressLint("ClickableViewAccessibility")
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
                            "chat with" + " " + chatList[i].nickname,
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
                            "chat with ${chatList[i].nickname}"
                        )
                        startActivity(i)
                        return
                    }
                }
            }
        }
    }

    override fun onStatusCallback(array: JSONArray) {
        activity?.runOnUiThread {
            logE("get array : $array")
            for (i in 0 until array.length()) {
                val jsonObject = array.getJSONObject(i)
                val userId = jsonObject.getInt("user_id")
                val status = jsonObject.getInt("status")
                if (chatList.size != 0) {
                    val obj = chatList.single { it.user_id == userId }
                    val index = chatList.indexOf(obj)
                    (binding.chatRecycler.adapter as AllChatListAdapter).updateOnlineStatus(
                        index, status
                    )
                }
            }
        }
    }
}