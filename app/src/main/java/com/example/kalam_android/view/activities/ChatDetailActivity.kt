package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.*
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.ResultVoiceToText
import com.example.kalam_android.callbacks.SelectItemListener
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.helper.MyChatMediaHelper
import com.example.kalam_android.helper.MyVoiceToTextHelper
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.repository.model.ChatDetailResponse
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.repository.model.Point
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.services.RxMediaWorker
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.webrtc.CallActivity
import com.example.kalam_android.wrapper.GlideDownloader
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sandrios.sandriosCamera.internal.ui.model.Media
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_edit_message.view.*
import kotlinx.android.synthetic.main.layout_for_attachment.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatDetailActivity : BaseActivity(), View.OnClickListener,
    SocketCallback, MessageTypingListener, View.OnTouchListener,
    ResultVoiceToText, SelectItemListener {

    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatId = 0
    private var lastMsgID: Long? = 0
    private var fromSearch = 0
    private var userRealName: String? = null
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var binding: ActivityChatDetailBinding
    lateinit var viewModel: ChatMessagesViewModel
    private val delay: Long = 1000
    private var lastTextEdit: Long = 0
    var handler = Handler()
    private var chatMessagesList: ArrayList<ChatData> = ArrayList()
    private var selectedMsgsIds: ArrayList<Long> = ArrayList()
    private var senderIds: ArrayList<Int?> = ArrayList()
    private var chatResponse: ChatDetailResponse? = null
    private var profileImage: String? = null
    private var loading = false
    private var isChatIdAvailable = false
    private var callerID: Int = -1
    private var myChatMediaHelper: MyChatMediaHelper? = null
    private var myVoiceToTextHelper: MyVoiceToTextHelper? = null
    private var myName = ""
    private var onlineStatus = ""
    private var lastMessage: String? = null
    private var lastMsgTime: Long = 0
    private var lastMessageSenderID = 0
    private var lastMessageStatus = 0
    private var isEditableEnabled = false
    private var editingMessage = false
    private var showingLocation = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mainHandler: Handler
    lateinit var runnable: Runnable

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        myChatMediaHelper = MyChatMediaHelper(this@ChatDetailActivity, binding.lvBottomChat, false)
        handleIntent(intent)
        initListeners()
        checkSomeoneTyping()
        SocketIO.getInstance().setSocketCallbackListener(this)
        SocketIO.getInstance().setTypingListeners(this)
        downwardPagination(fromSearch != 0)
        upwardPagination()
        binding.fabSpeech.setOnTouchListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (chatResponse != null) {
            if (lastMessage?.isNotEmpty() == true) {
                viewModel.updateItemToDB(
                    lastMsgTime.toString(), lastMessage.toString(),
                    chatId, 0, lastMessageSenderID, lastMessageStatus
                )
            } else {
                viewModel.updateChatItemDB(chatId, 0, lastMessageStatus)
            }
        }
        if (intent != null) handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        lastMessage = null
        lastMsgTime = 0
        lastMessageSenderID = 0
        lastMessageStatus = 0
        binding.pbCenter.visibility = View.VISIBLE
        isChatIdAvailable = intent.getBooleanExtra(AppConstants.IS_CHATID_AVAILABLE, false)
        chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
        lastMsgID = intent.getLongExtra(AppConstants.MSG_ID, 0)
        fromSearch = intent.getIntExtra(AppConstants.FROM_SEARCH, 0)
        userRealName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
        profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
        callerID = intent.getIntExtra(AppConstants.CALLER_USER_ID, 0)
        setUserData()
        initAdapter()
        if (isChatIdAvailable) {
            Global.currentChatID = chatId
            hitConversationApi(lastMsgID, 0, fromSearch)
            SocketIO.getInstance().emitReadAllMessages(
                chatId.toString(),
                sharedPrefsHelper.getUser()?.id.toString()
            )
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
        } else {
            val jsonObject = JsonObject()
            logE("callerId : $callerID")
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", callerID)
            SocketIO.getInstance().socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                this.chatId = chatId
                Global.currentChatID = chatId
                runOnUiThread {
                    hitConversationApi(0, 0, 0)
                }
            })
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 1)
        }
        SocketIO.getInstance()
            .emitGetNickName(sharedPrefsHelper.getUser()?.id.toString(), callerID.toString())
        SocketIO.getInstance().checkUserStatus(callerID.toString())
    }

    private fun initListeners() {
        binding.lvBottomChat.ivSend.setOnClickListener(this)
        binding.lvBottomChat.ivCamera.setOnClickListener(this)
        binding.lvBottomChat.ivAttach.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        binding.lvBottomChat.ivMic.setOnClickListener(this)
        binding.header.llProfile.setOnClickListener(this)
        binding.header.ivAudio.setOnClickListener(this)
        binding.header.ivVideo.setOnClickListener(this)
        binding.edit.ivCancel.setOnClickListener(this)
        binding.edit.llDelete.setOnClickListener(this)
        binding.edit.llCopy.setOnClickListener(this)
        binding.edit.llEdit.setOnClickListener(this)
        binding.edit.llForward.setOnClickListener(this)
        binding.lvBottomChat.lvForAttachment.llLocation.setOnClickListener(this)
    }

    private fun initAdapter() {
        val linearLayout = LinearLayoutManager(this)
        linearLayout.reverseLayout = true
        linearLayout.stackFromEnd = false
        binding.chatMessagesRecycler.layoutManager = linearLayout
        binding.chatMessagesRecycler.adapter =
            ChatMessagesAdapter(
                this,
                sharedPrefsHelper.getUser()?.id.toString(),
                userRealName.toString(),
                profileImage.toString(),
                sharedPrefsHelper.getTransState(),
                sharedPrefsHelper.getLanguage(), myChatMediaHelper, this
            )
        ((binding.chatMessagesRecycler.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
    }

    private fun upwardPagination() {
        binding.chatMessagesRecycler.addOnScrollListener(object :
            PaginationScrollUpListener(binding.chatMessagesRecycler.layoutManager as LinearLayoutManager) {
            override val isLastPage: Boolean
                get() = chatResponse?.data?.is_last_page == 1
            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                logE("Upward Pagination")
                binding.pbHeader.visibility = View.VISIBLE
                loading = true
                lastMsgID = chatMessagesList[chatMessagesList.size - 1].id
                hitConversationApi(lastMsgID, 0, fromSearch)
            }
        })
    }

    private fun downwardPagination(isFromSearch: Boolean) {
        if (isFromSearch) {
            binding.chatMessagesRecycler.addOnScrollListener(object :
                PaginationScrollDownListener(binding.chatMessagesRecycler.layoutManager as LinearLayoutManager) {
                override val isFirstPage: Boolean
                    get() = chatResponse?.data?.is_first_page == 1
                override val isLoading: Boolean
                    get() = loading

                override fun loadMoreItems() {
                    logE("Downward Pagination")
                    binding.pbFooter.visibility = View.VISIBLE
                    loading = true
                    lastMsgID = chatMessagesList[0].id
                    hitConversationApi(lastMsgID, 1, fromSearch)
                }
            })
        }
    }

    fun hitConversationApi(offset: Long?, swipe: Int, fromSearch: Int) {
        val params = HashMap<String, String>()
        params["chat_id"] = this.chatId.toString()
        params["offset"] = offset.toString()
        params["swipe_up"] = swipe.toString()
        params["from_search"] = fromSearch.toString()
        viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)

    }

    private fun setUserData() {
        binding.header.tvName.text = userRealName
        GlideDownloader.load(
            this,
            binding.header.ivProfileImage,
            profileImage,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
    }

    private fun consumeResponse(apiResponse: ApiResponse<ChatDetailResponse>?) {
        when (apiResponse?.status) {
            Status.LOADING -> {
                loading = true
                logE("Loading Chat Messages")
            }
            Status.SUCCESS -> {
                loading = false
                binding.pbCenter.visibility = View.GONE
                binding.pbHeader.visibility = View.GONE
                binding.pbFooter.visibility = View.GONE
                renderResponse(apiResponse.data as ChatDetailResponse)
            }
            Status.ERROR -> {
                loading = false
                binding.pbCenter.visibility = View.GONE
                binding.pbHeader.visibility = View.GONE
                binding.pbFooter.visibility = View.GONE
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderResponse(mResponse: ChatDetailResponse?) {
        logE("socketResponse: $mResponse")
        mResponse?.let { response ->
            chatResponse = response
            response.data?.chats?.let {
                if (fromSearch == 1) {
                    fromSearch = 0
                    for (x in it.indices) {
                        if (it[x].id == lastMsgID) {
                            binding.chatMessagesRecycler.scrollToPosition(x)
                        }
                    }
                }
                if (response.data.swipe_up == 0) {
                    chatMessagesList.addAll(it)
                    if (it.isNotEmpty())
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(
                            chatMessagesList
                        )
                } else if (response.data.swipe_up == 1) {
                    if (it.isNotEmpty()) {
                        var i = it.size - 1
                        while (i > -1) {
                            chatMessagesList.add(0, it[i])
                            (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(
                                chatMessagesList
                            )
                            i--
                        }
                    }
                }
                lastMessageStatus = it[0].is_read
            }
        }
    }

    private fun uploadMedia(
        identifier: String,
        file: String,
        duration: Long,
        type: String,
        groupID: String
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(RxMediaWorker::class.java)
            .setInputData(
                createInputData(
                    identifier,
                    file,
                    duration.toString(),
                    type,
                    sharedPrefsHelper.getUser()?.token.toString(),
                    groupID
                )
            )
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setConstraints(constraints).build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
    }

    private fun createInputData(
        identifier: String,
        file: String,
        duration: String,
        type: String,
        token: String,
        groupID: String
    ): Data {
        return Data.Builder()
            .putString("identifier", identifier)
            .putString("file", file)
            .putString("duration", duration)
            .putString("type", type)
            .putString("token", token)
            .putString("id", sharedPrefsHelper.getUser()?.id.toString())
            .putString("chatId", chatId.toString())
            .putString("name", myName)
            .putString("language", sharedPrefsHelper.getLanguage().toString())
            .putString("group_id", groupID)
            .putString("profile_image", sharedPrefsHelper.getUser()?.profile_image.toString())
            .build()
    }

    private fun sendMediaMessage(file: String, type: String, duration: Long, groupID: String) {
        val identifier = System.currentTimeMillis().toString()
        when (type) {
            AppConstants.AUDIO_MESSAGE -> {
                createChatObject("Audio", file, type, identifier)
                uploadMedia(identifier, file, duration, type, groupID)
            }
            AppConstants.IMAGE_MESSAGE -> {
                createChatObject("Image", file, type, identifier)
                val convertedFile = Compressor(this).compressToFile(File(file))
                uploadMedia(identifier, convertedFile.absolutePath, duration, type, groupID)
            }
            AppConstants.VIDEO_MESSAGE -> {
                createChatObject(
                    "Video",
                    file,
                    type,
                    identifier
                )
                uploadMedia(identifier, file, duration, type, groupID)
            }
        }
    }

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            SocketIO.getInstance().typingEvent(
                AppConstants.STOP_TYPING,
                sharedPrefsHelper.getUser()?.id.toString(),
                chatId.toString()
            )
        }
    }

    private fun checkSomeoneTyping() {
        binding.lvBottomChat.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    lastTextEdit = System.currentTimeMillis()
                    handler.postDelayed(inputFinishChecker, delay)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    SocketIO.getInstance().typingEvent(
                        AppConstants.START_TYPING,
                        sharedPrefsHelper.getUser()?.id.toString(),
                        chatId.toString()
                    )
                    handler.removeCallbacks(inputFinishChecker)
                }
            }
        })
    }

    private fun addMessage(chatData: ChatData) {
        lastMsgTime = chatData.unix_time.toLong()
        lastMessage = chatData.message
        chatData.sender_id?.let {
            lastMessageSenderID = it
        }

        lastMessageStatus = 0
        chatMessagesList.add(0, chatData)
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatMessagesList)
        binding.chatMessagesRecycler.scrollToPosition(0)
    }

    private fun createChatObject(message: String, file: String, type: String, identifier: String) {
        addMessage(
            ChatData(
                AppConstants.DUMMY_STRING,
                file,
                AppConstants.DUMMY_STRING,
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                ).toString(),
                identifier.toLong(),
                chatId,
                sharedPrefsHelper.getUser()?.id,
                AppConstants.DUMMY_DATA,
                message,
                AppConstants.DUMMY_DATA,
                AppConstants.DUMMY_DATA,
                type,
                file,
                0,
                0,
                message,
                identifier,
                System.currentTimeMillis() / 1000L.toDouble(),
                sharedPrefsHelper.getLanguage(),
                sharedPrefsHelper.getUser()?.profile_image.toString(),
                AppConstants.DUMMY_STRING
            )
        )
    }

    private fun sendMessage() {
        val identifier = System.currentTimeMillis().toString()
        createChatObject(
            binding.lvBottomChat.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE, identifier
        )
        SocketIO.getInstance().emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(),
            chatId.toString(),
            binding.lvBottomChat.editTextMessage.text.toString(),
            AppConstants.TEXT_MESSAGE,
            myName,
            AppConstants.DUMMY_STRING,
            0,
            AppConstants.DUMMY_STRING,
            identifier,
            sharedPrefsHelper.getLanguage().toString(),
            identifier, sharedPrefsHelper.getUser()?.profile_image.toString()
        )
        logE("Message Emitted to socket")
        binding.lvBottomChat.editTextMessage.setText("")
    }

    //Socket Listeners

    override fun socketResponse(jsonObject: JSONObject, type: String) {
        runOnUiThread {
            when (type) {
                AppConstants.NEW_MESSAGE -> {
                    val gson = Gson()
                    val data = gson.fromJson(jsonObject.toString(), ChatData::class.java)
                    logE("data: $data")
                    if (data.chat_id == chatId) {
                        addMessage(data)
                        SocketIO.getInstance().emitMessageSeen(
                            data.chat_id.toString(),
                            data.id.toString(),
                            sharedPrefsHelper.getUser()?.id.toString()
                        )
                    }
                }
                AppConstants.ALL_MESSAGES_READ -> {
                    logE("Chat ID received : $jsonObject")
                    lastMessageStatus = 2
                    val chatId = jsonObject.getString("chat_id").toInt()
                    if (this.chatId == chatId) {
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateReadStatus(
                            true
                        )
                    }
                }
                AppConstants.MESSAGE_DELIVERED -> {
                    lastMessageStatus = 1
                    val chatId = jsonObject.getString("chat_id")
//                    val userId = jsonObject.getString("user_id")
                    if (chatId.toInt() == this.chatId) {
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateReadStatus(
                            false
                        )
                    }
                }
                AppConstants.SEND_MESSAGE -> {
                    logE("AppConstants.SEND_MESSAGE status: $jsonObject")
                    val isDelivered = jsonObject.getBoolean("delivered")
                    if (isDelivered) lastMessageStatus = 1
                    val identifier = jsonObject.getString("identifier")
                    val msgId = jsonObject.getString("message_id")
                    chatMessagesList.let {
                        for (x in it.indices) {
                            if (chatMessagesList[x].identifier == identifier) {
                                chatMessagesList[x].identifier = ""
                                chatMessagesList[x].id = msgId.toLong()
                                if (isDelivered) {
                                    chatMessagesList[x].is_read = 1
                                } else {
                                    chatMessagesList[x].is_read = 0
                                }
                                (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).itemChanged(
                                    chatMessagesList, x
                                )
                                if (chatMessagesList[x].type == AppConstants.LOCATION_MESSAGE) {
                                    showingLocation = true
                                    mainHandler = Handler(Looper.getMainLooper())
                                    runnable = object : Runnable {
                                        override fun run() {
                                            initMap(true, msgId)
                                            mainHandler.postDelayed(this, 3000)
                                        }

                                    }
                                    mainHandler.post(runnable)
                                }
                            }
                        }
                    }
                }
                AppConstants.SEEN_MESSAGE -> {
                    logE("SEEN_MESSAGE :$jsonObject")
                    lastMessageStatus = 2
                    val msgId = jsonObject.getString("message_id")
                    val chatId = jsonObject.getString("chat_id")
                    if (chatId.toInt() == this.chatId) {
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateSeenStatus(
                            msgId.toLong()
                        )
                    }
                }
                AppConstants.GET_MY_NICKNAME -> {
                    myName = jsonObject.getString("nickname")
                }
                AppConstants.CHECK_USER_STATUS -> {
                    val userId = jsonObject.getInt("user_id")
                    val status = jsonObject.getInt("status")
                    checkUserStatus(userId, status)
                }
                AppConstants.USER_STATUS -> {
                    val userId = jsonObject.getInt("user_id")
                    val status = jsonObject.getInt("status")
                    checkUserStatus(userId, status)
                }
                AppConstants.MESSAGE_DELETED -> {
                    logE("MESSAGE_DELETED :$jsonObject")
                    val messages = jsonObject.getJSONArray("messages")
                    val chatId = jsonObject.getString("chat_id")
                    if (this.chatId == chatId.toInt()) {
                        deleteMessages(messages)
                    }
                }
                AppConstants.DELETE_MESSAGE -> {
                    val messages = jsonObject.getJSONArray("messages")
                    deleteMessages(messages)
                    editingMessage = false

                }
                AppConstants.EDIT_MESSAGE -> {
                    val chatId = jsonObject.getString("chat_id")
                    val messageId = jsonObject.getString("message_id")
                    val message = jsonObject.getString("message")
                    if (this.chatId == chatId.toInt()) {
                        val item = chatMessagesList.single { it.id == messageId.toLong() }
                        val index = chatMessagesList.indexOf(item)
                        item.message = message
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).itemChanged(
                            chatMessagesList, index
                        )
                    }
                }
            }
        }
    }

    private fun checkUserStatus(userId: Int, status: Int) {
        if (callerID == userId) {
            if (status == 1) {
                onlineStatus = "Online"
                binding.header.tvTyping.text = onlineStatus
            } else {
                onlineStatus = "Away"
                binding.header.tvTyping.text = onlineStatus
            }
        }
    }

    private fun resetSelection() {
        binding.edit.visibility = View.GONE
        isEditableEnabled = false
        selectedMsgsIds.clear()
        senderIds.clear()
    }

    private fun removeSelectItems() {
        if (selectedMsgsIds.isNotEmpty()) {
            selectedMsgsIds.forEach { id ->
                val item = chatMessagesList.single { data -> data.id == id }
                val index = chatMessagesList.indexOf(item)
                item.is_selected = false
                (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).itemChanged(
                    chatMessagesList,
                    index
                )
            }
            editingMessage = false
            resetSelection()
        }
    }

    private fun deleteMessages(messages: JSONArray) {
        for (x in 0 until messages.length()) {
            val id = messages.getLong(x)
            val msg = chatMessagesList.filter { it.id == id }
            val index = chatMessagesList.indexOf(msg[0])
            chatMessagesList.remove(msg[0])
            (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).itemRemoved(
                chatMessagesList, index
            )
        }
        resetSelection()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSend -> {
                if (!editingMessage) {
                    if (myChatMediaHelper?.fileOutput()?.isEmpty() == true &&
                        binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
                    ) {
                        sendMessage()
                    } else {
                        if (myChatMediaHelper?.isFileReady() == true) {
                            val groupID = System.currentTimeMillis().toString()
                            myChatMediaHelper?.getTotalDuration()?.let {
                                sendMediaMessage(
                                    myChatMediaHelper?.fileOutput().toString(),
                                    AppConstants.AUDIO_MESSAGE, it, groupID
                                )
                            }
                            myChatMediaHelper?.hideRecorder()
                            myChatMediaHelper?.cancel(false)
                        }
                    }
                } else {
                    logE("editingMessage else")
                    SocketIO.getInstance().emitEditMsg(
                        selectedMsgsIds[0].toString(),
                        chatId.toString(),
                        binding.lvBottomChat.editTextMessage.text.toString(), callerID.toString()
                    )
                    logE("deletedItems :${selectedMsgsIds[0]}")
                    binding.lvBottomChat.editTextMessage.setText("")
                    removeSelectItems()
                }
            }
            R.id.rlBack -> {
                onBackPressed()
            }
            R.id.llProfile -> {
                val intent = Intent(this@ChatDetailActivity, UserProfileActivity::class.java)
                intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
                intent.putExtra(AppConstants.CALLER_USER_ID, callerID.toString())
                val transitionName = getString(R.string.profile_trans)
                val options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        binding.header.ivProfileImage,
                        transitionName
                    )
                ActivityCompat.startActivity(this, intent, options.toBundle())
            }
            R.id.ivCamera -> {
                SandriosCamera
                    .with()
                    .setShowPicker(false)
                    .setMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH)
                    .enableImageCropping(true)
                    .launchCamera(this)
            }
            R.id.ivMic -> {
                myChatMediaHelper?.initRecorderWithPermissions()
            }
            R.id.ivAttach -> {
                myChatMediaHelper?.openAttachments()
            }
            R.id.ivAudio -> {
                startNewActivity(CallActivity::class.java, false)
            }
            R.id.ivVideo -> {
                startNewActivity(CallActivity::class.java, true)
            }
            R.id.ivCancel -> {
                removeSelectItems()
            }
            R.id.llDelete -> {
                if (selectedMsgsIds.isNotEmpty()) {
                    SocketIO.getInstance().emitDeleteMsg(
                        chatId.toString(),
                        selectedMsgsIds.toString(),
                        callerID.toString()
                    )
                }
            }
            R.id.llCopy -> {
                val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                var text = ""
                selectedMsgsIds.forEach { id ->
                    val item = chatMessagesList.single { it.id == id }
                    text += item.message
                }
                val clipData = ClipData.newPlainText("text", text)
                clipManager.setPrimaryClip(clipData)
                removeSelectItems()
                toast("Text Copied to Clipboard")
            }
            R.id.llEdit -> {
                val item = chatMessagesList.single { it.id == selectedMsgsIds[0] }
                val text = item.message.toString()
                binding.lvBottomChat.editTextMessage.setText(text)
                editingMessage = true
            }
            R.id.llLocation -> {
                locationPermissions()
            }
            R.id.llForward -> {
                val intent = Intent(this, ContactListActivity::class.java)
                intent.putExtra(AppConstants.IS_FORWARD_MESSAGE, true)
                intent.putExtra(AppConstants.SELECTED_MSGS_IDS, selectedMsgsIds.toString())
                intent.putExtra(AppConstants.CALLER_USER_ID, callerID)
                startActivityForResult(intent, AppConstants.FORWARD_REQUEST_CODE)
            }
        }
    }

    fun openLocationDialog() {
        val builder1 = AlertDialog.Builder(this@ChatDetailActivity)
        builder1.setTitle("Share Location")
        builder1.setMessage("Do you really want to share your location?")
        builder1.setCancelable(true)
        builder1.setPositiveButton("Yes") { dialog, id ->
            val identifier = System.currentTimeMillis().toString()
            initMap(false, identifier)
            myChatMediaHelper?.hideAttachments()
            dialog.dismiss()
        }
        builder1.setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
        builder1.create().show()
    }

    private fun locationPermissions() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    openLocationDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    response?.requestedPermission
                }

            }).onSameThread().check()
    }

    private fun initMap(isResendLocation: Boolean, msgId: String) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                logE("latitude :${it.latitude}")
                logE("longitude :${it.longitude}")
                val currentPoint = Point(it.latitude, it.longitude)
                if (!isResendLocation) {
                    createChatObject(
                        StringBuilder(currentPoint.lat.toString()).append(",").append(currentPoint.long.toString()).toString(),
                        AppConstants.DUMMY_STRING,
                        AppConstants.LOCATION_MESSAGE,
                        msgId
                    )
                    SocketIO.getInstance().emitNewMessage(
                        sharedPrefsHelper.getUser()?.id.toString(),
                        chatId.toString(),
                        StringBuilder(currentPoint.lat.toString()).append(",").append(currentPoint.long.toString()).toString(),
                        AppConstants.LOCATION_MESSAGE,
                        myName,
                        AppConstants.DUMMY_STRING,
                        0,
                        AppConstants.DUMMY_STRING,
                        msgId,
                        sharedPrefsHelper.getLanguage().toString(),
                        msgId, sharedPrefsHelper.getUser()?.profile_image.toString()
                    )
                } else {
                    SocketIO.getInstance().emitEditLocation(
                        msgId.toInt(),
                        chatId,
                        callerID,
                        it.latitude,
                        it.longitude
                    )
                }
            }
        }
    }

    private fun startNewActivity(mClass: Class<*>, isVideo: Boolean) {
        val intent = Intent(this, mClass)
        intent.putExtra(AppConstants.INITIATOR, true)
        intent.putExtra(AppConstants.CALLER_USER_ID, callerID)
        intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
        intent.putExtra(AppConstants.GET_MY_NICKNAME, myName)
        intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
        intent.putExtra(AppConstants.IS_VIDEO_CALL, isVideo)
        startActivity(intent)
        overridePendingTransition(R.anim.bottom_up, R.anim.anim_nothing)
    }

    override fun typingResponse(jsonObject: JSONObject, isTyping: Boolean) {
        val chatId1 = jsonObject.getString("chat_id")
        if (chatId1.toInt() == chatId) {
            runOnUiThread {
                if (isTyping) {
                    val userId = jsonObject.getInt("user_id")
                    if (userId == callerID) {
                        binding.header.tvTyping.text = "Typing..."
                    }
                } else {
                    binding.header.tvTyping.text = onlineStatus
                }
            }
        }
    }

    override fun itemListener(view: View, position: Int, isLongClick: Boolean) {
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        view.setBackgroundResource(outValue.resourceId)
        if (isLongClick) {
            isEditableEnabled = true
            performClickAction(position)
        } else {
            if (isEditableEnabled) {
                performClickAction(position)
            }
        }
    }

    private fun performClickAction(position: Int) {
        if (chatMessagesList[position].is_selected) {
            chatMessagesList[position].is_selected = false
            selectedMsgsIds.remove(chatMessagesList[position].id)
            senderIds.remove(chatMessagesList[position].sender_id)
        } else {
            chatMessagesList[position].is_selected = true
            selectedMsgsIds.add(chatMessagesList[position].id)
            senderIds.add(chatMessagesList[position].sender_id)
        }
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).itemChanged(
            chatMessagesList,
            position
        )
        if (senderIds.contains(callerID)) {
            binding.edit.llDelete.setOnClickListener(null)
            binding.edit.llDelete.alpha = 0.5f
        } else {
            binding.edit.llDelete.setOnClickListener(this)
            binding.edit.llDelete.alpha = 1f
        }
        if (selectedMsgsIds.size > 1 || senderIds.contains(callerID) || chatMessagesList[position].type != AppConstants.TEXT_MESSAGE) {
            binding.edit.llEdit.setOnClickListener(null)
            binding.edit.llEdit.alpha = 0.5f
        } else {
            binding.edit.llEdit.setOnClickListener(this)
            binding.edit.llEdit.alpha = 1f
        }
        if (selectedMsgsIds.size == 0) {
            binding.edit.visibility = View.GONE
            isEditableEnabled = false
        } else binding.edit.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SandriosCamera.RESULT_CODE -> {
                    if (data?.getSerializableExtra(SandriosCamera.MEDIA) is Media) {
                        val groupID = System.currentTimeMillis().toString()
                        val media = data.getSerializableExtra(SandriosCamera.MEDIA) as Media
                        if (media.type == SandriosCamera.MediaType.PHOTO) {
                            logE("onActivity Received")
                            sendMediaMessage(
                                media.path,
                                AppConstants.IMAGE_MESSAGE, 0, groupID
                            )
                        } else if (media.type == SandriosCamera.MediaType.VIDEO) {
                            sendMediaMessage(
                                media.path,
                                AppConstants.VIDEO_MESSAGE, 0, groupID
                            )
                        }
                    }
                }
                AppConstants.SELECTED_IMAGES -> {
                    if (data != null) {
                        val list =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
                        val intent =
                            Intent(this@ChatDetailActivity, AttachmentActivity::class.java)
                        intent.putExtra(AppConstants.SELECTED_IMAGES_VIDEOS, list)
                        startActivityForResult(intent, AppConstants.SELECT_IMAGES_VIDEOS)

                    }
                }
                AppConstants.SELECT_IMAGES_VIDEOS -> {
                    if (data != null) {
                        val list =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
                        Debugger.e("List", "$list")
                        sendVideoOrImage(list)
                    }
                }
                AppConstants.FORWARD_REQUEST_CODE -> {
                    /*val isContainReceiver = data?.getBooleanExtra("is_contain_receiver", false)
                    if (isContainReceiver == true) {
                        logE("isContainReceiver: $isContainReceiver")
                        selectedMsgsIds.forEach { id ->
                            val msgObject = chatMessagesList.single { it.id == id }
                            logE("msgObject: $msgObject")
                            addMessage(msgObject)
                        }
                    }*/
                    SocketIO.getInstance().setSocketCallbackListener(this)
                    toast("Forwarded successfully")

                    removeSelectItems()
                }
            }
        }
    }

    private fun sendVideoOrImage(list: ArrayList<MediaList>?) {
        myChatMediaHelper?.hideAttachments()
        list?.let {
            it.forEach { media ->
                val groupID = System.currentTimeMillis().toString()
                when (media.type) {
                    0 -> sendMediaMessage(media.file, AppConstants.IMAGE_MESSAGE, 0, groupID)
                    1 -> sendMediaMessage(media.file, AppConstants.VIDEO_MESSAGE, 0, groupID)
                    2 -> sendMediaMessage(media.file, AppConstants.AUDIO_MESSAGE, 0, groupID)
                }
            }
        }
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


    override fun onPause() {
        super.onPause()
        try {
            if (myChatMediaHelper?.myPlayer != null) {
                if (myChatMediaHelper?.myPlayer?.isPlaying == true) {
                    logE("Media Player is playing")
                    myChatMediaHelper?.myPlayer?.stop()
                    myChatMediaHelper?.myPlayer?.reset()
                    myChatMediaHelper?.myPlayer?.release()
                    myChatMediaHelper?.myPlayer = null
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        Global.currentChatID = -1
        myChatMediaHelper?.myPlayer = MediaPlayer()
        myVoiceToTextHelper?.destroy()
    }

    override fun onResume() {
        super.onResume()
        myVoiceToTextHelper = MyVoiceToTextHelper(this, this)
        myVoiceToTextHelper?.checkPermissionForVoiceToText()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onBackPressed() {
        if (chatResponse != null) {
            if (lastMessage?.isNotEmpty() == true) {
                viewModel.updateItemToDB(
                    lastMsgTime.toString(), lastMessage.toString(),
                    chatId, 0, lastMessageSenderID, lastMessageStatus
                )
            } else {
                viewModel.updateChatItemDB(chatId, 0, lastMessageStatus)
            }
        }
        if (showingLocation)
            mainHandler.removeCallbacks(runnable)
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onResultVoiceToText(list: ArrayList<String>) {
        if (list[0] == "type") {
            Global.showKeyBoard(this, binding.lvBottomChat.editTextMessage)
        } else if (list[0] == "type a message") {
            Global.showKeyBoard(this, binding.lvBottomChat.editTextMessage)
        } else {
            if (list[0] == "over" && binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
            ) {
                sendMessage()
            }
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}