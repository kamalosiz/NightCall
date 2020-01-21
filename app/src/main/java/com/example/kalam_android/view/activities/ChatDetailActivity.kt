package com.example.kalam_android.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.ResultVoiceToText
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.helper.MyChatMediaHelper
import com.example.kalam_android.helper.MyVoiceToTextHelper
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.services.RxMediaWorker
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.webrtc.VideoCallActivity
import com.example.kalam_android.wrapper.GlideDownloader
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sandrios.sandriosCamera.internal.ui.model.Media
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.header_chat.view.*
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.seconds

class ChatDetailActivity : BaseActivity(), View.OnClickListener,
    SocketCallback, MessageTypingListener, View.OnTouchListener,
    ResultVoiceToText {

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
    private var upChatList: ArrayList<ChatData> = ArrayList()
    private var downChatList: ArrayList<ChatData> = ArrayList()
    private var chatResponse: ChatMessagesResponse? = null
    private var profileImage: String? = null
    private var loading = false
    private var isChatIdAvailable = false
    private var callerID: Long = -1
    private var myChatMediaHelper: MyChatMediaHelper? = null
    private var lastMessage: String? = null
    private var lastMsgTime: Long = 0
    private var myVoiceToTextHelper: MyVoiceToTextHelper? = null
    private var lastMessageSenderID: Int? = 0


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        myChatMediaHelper = MyChatMediaHelper(this@ChatDetailActivity, binding)
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
        if (intent != null) handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        binding.pbCenter.visibility = View.VISIBLE
        isChatIdAvailable = intent.getBooleanExtra(AppConstants.IS_CHATID_AVAILABLE, false)
        chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
        lastMsgID = intent.getLongExtra(AppConstants.MSG_ID, 0)
//        logE("lastMessage ID : $lastMsgID")
        fromSearch = intent.getIntExtra(AppConstants.FROM_SEARCH, 0)
        userRealName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
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
                sharedPrefsHelper.getLanguage(), myChatMediaHelper
            )
    }

    private fun upwardPagination() {
        binding.chatMessagesRecycler.addOnScrollListener(object :
            PaginationScrollUpListener(binding.chatMessagesRecycler.layoutManager as LinearLayoutManager) {
            override val isLastPage: Boolean
                get() = chatResponse?.data?.is_last_page == 1
            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                binding.pbHeader.visibility = View.VISIBLE
                loading = true
                lastMsgID = upChatList[upChatList.size - 1].id
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
                    binding.pbFooter.visibility = View.VISIBLE
                    loading = true
                    lastMsgID = downChatList[0].id
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
        profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
        callerID = intent.getLongExtra(AppConstants.CALLER_USER_ID, 0)
        binding.header.tvName.text = userRealName
        GlideDownloader.load(
            this,
            binding.header.ivProfileImage,
            profileImage,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
    }

    private fun consumeResponse(apiResponse: ApiResponse<ChatMessagesResponse>?) {
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
                renderResponse(apiResponse.data as ChatMessagesResponse)
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

    private fun renderResponse(mResponse: ChatMessagesResponse?) {
        logE("socketResponse: $mResponse")
        mResponse?.let { response ->
            chatResponse = response
            response.data.chats?.let {
                if (fromSearch == 1) {
                    fromSearch = 0
                    for (x in it.indices) {
                        if (it[x].id == lastMsgID) {
                            binding.chatMessagesRecycler.scrollToPosition(x)
                        }
                    }
                    if (it.isNotEmpty())
                        downChatList = it
                }
                if (response.data.swipe_up == 0) {
                    if (it.isNotEmpty())
                        upChatList = it
                    (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(
                        it, false
                    )
                } else if (response.data.swipe_up == 1) {
                    if (it.isNotEmpty())
                        downChatList = it
                    (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(
                        it, true
                    )
                }

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
            .putString(
                "name", sharedPrefsHelper.getUser()?.firstname.toString()
                        + " " + sharedPrefsHelper.getUser()?.lastname.toString()
            )
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
        lastMessageSenderID = chatData.sender_id
        logE("My ID :${sharedPrefsHelper.getUser()?.id}")
        logE("chatData.sender_id :${chatData.sender_id}")
        logE("chatData.receiver_id :${chatData.receiver_id}")
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatData)
        binding.chatMessagesRecycler.scrollToPosition(0)
    }

    private fun createChatObject(message: String, file: String, type: String, identifier: String) {
        addMessage(
            ChatData(
                AppConstants.DUMMY_STRING,
                file, AppConstants.DUMMY_STRING,
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                ).toString(),
                identifier.toLong(), chatId, sharedPrefsHelper.getUser()?.id,
                AppConstants.DUMMY_DATA, message, AppConstants.DUMMY_DATA, AppConstants.DUMMY_DATA,
                type, file, 0, 0, message, identifier,
                System.currentTimeMillis() / 1000L.toDouble(), sharedPrefsHelper.getLanguage(),
                sharedPrefsHelper.getUser()?.profile_image.toString()
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
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(),
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSend -> {
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
                startNewActivity(VideoCallActivity::class.java, false)
            }
            R.id.ivVideo -> {
                startNewActivity(VideoCallActivity::class.java, true)
            }
        }
    }

    private fun startNewActivity(mClass: Class<*>, isVideo: Boolean) {
        val intent = Intent(this, mClass)
        intent.putExtra(AppConstants.INITIATOR, true)
        intent.putExtra(AppConstants.CALLER_USER_ID, callerID)
        intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
        intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
        intent.putExtra("isVideoCall", isVideo)
        startActivity(intent)
        overridePendingTransition(R.anim.bottom_up, R.anim.anim_nothing)
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
            }
        }
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
    }

    override fun onBackPressed() {
        val intent = Intent()
        /* if (isFromOutside) {
             startActivity(Intent(this, MainActivity::class.java))
             finish()

              && lastMsgTime != null
         } else {*/

        if (lastMessage?.isNotEmpty() == true) {
            intent.putExtra(AppConstants.LAST_MESSAGE, lastMessage.toString())
            intent.putExtra(AppConstants.LAST_MESSAGE_TIME, lastMsgTime.toString())
            intent.putExtra(AppConstants.IsSEEN, false)
        } else {
            intent.putExtra(AppConstants.IsSEEN, true)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
//        }
//        setResult(Activity.RESULT_OK)
//        finish()
    }
    //Socket Listeners

    override fun socketResponse(jsonObject: JSONObject, type: String) {
        runOnUiThread {
            when (type) {
                AppConstants.NEW_MESSAGE -> {
                    val gson = Gson()
                    val data = gson.fromJson(jsonObject.toString(), ChatData::class.java)
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
                    val chatId = jsonObject.getString("chat_id").toInt()
                    if (this.chatId == chatId) {
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateReadStatus(
                            true
                        )
                    }
                }
                AppConstants.MESSAGE_DELIVERED -> {
                    logE("MESSAGE_DELIVERED  : $jsonObject")
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
                    val identifier = jsonObject.getString("identifier")
                    val msgId = jsonObject.getString("message_id")
                    (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateIdentifier(
                        identifier, isDelivered, msgId
                    )
                }
                AppConstants.SEEN_MESSAGE -> {
                    logE("Message Seen")
                    val msgId = jsonObject.getString("message_id")
                    val chatId = jsonObject.getString("chat_id")
                    if (chatId.toInt() == this.chatId) {
                        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateSeenStatus(
                            msgId.toLong()
                        )
                    }
                }
            }
        }
    }

    override fun typingResponse(jsonObject: JSONObject, isTyping: Boolean) {
        val chatId1 = jsonObject.getString("chat_id")
        if (chatId1.toInt() == chatId) {
            runOnUiThread {
                if (isTyping) {
                    val user = jsonObject.getLong("user_id")
                    if (user == callerID) {
                        binding.header.tvTyping.visibility = View.VISIBLE
                    }
                } else {
                    binding.header.tvTyping.visibility = View.GONE
                }
            }
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
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
        if (list[0] == "type") {
            Global.showKeyBoard(this, binding.lvBottomChat.editTextMessage)
        } else if (list[0] == "type a message") {
            Global.showKeyBoard(this, binding.lvBottomChat.editTextMessage)
        } else {
            if (list[0] == "over" && binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
            ) {
                sendMessage()
            } else {
//                binding.lvBottomChat.editTextMessage.setText(list[0])
//                binding.lvBottomChat.editTextMessage.setSelection(binding.lvBottomChat.editTextMessage.length())
            }
        }
    }
}