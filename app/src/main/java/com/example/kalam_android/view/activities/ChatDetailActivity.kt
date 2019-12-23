package com.example.kalam_android.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.*
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.helper.MyChatMediaHelper
import com.example.kalam_android.helper.MyVoiceToTextHelper
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.repository.model.MediaResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.webrtc.AudioCallActivity
import com.example.kalam_android.webrtc.VideoCallActivity
import com.example.kalam_android.wrapper.GlideDownloder
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nagihong.videocompressor.VideoCompressor
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sandrios.sandriosCamera.internal.ui.model.Media
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.header_chat.view.*
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class ChatDetailActivity : BaseActivity(), View.OnClickListener,
    SocketCallback, MessageTypingListener, View.OnTouchListener,
    ResultVoiceToText {

    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatId = -1
    private var receiverId: String? = null
    private var userRealName: String? = null
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var binding: ActivityChatDetailBinding
    lateinit var viewModel: ChatMessagesViewModel
    private val delay: Long = 1000
    private var lastTextEdit: Long = 0
    private var index = 0
    var handler = Handler()
    private var chatList1: ArrayList<ChatData> = ArrayList()
    private var profileImage: String? = null
    private var loading = false
    private var isChatIdAvailable = false
    private var callerID: Long = -1
    private var myChatMediaHelper: MyChatMediaHelper? = null
    private var lastMessage: String? = null
    private var lastMsgTime: Long = 0
    private var myVoiceToTextHelper: MyVoiceToTextHelper? = null
    private var galleryList: ArrayList<MediaList>? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        viewModel.mediaResponse().observe(this, Observer {
            consumeMediaResponse(it)
        })
        myChatMediaHelper = MyChatMediaHelper(this@ChatDetailActivity, binding)
        handleIntent(intent)
        initListeners()
        checkSomeoneTyping()
        SocketIO.setSocketCallbackListener(this)
        SocketIO.setTypingListeners(this)
        myVoiceToTextHelper = MyVoiceToTextHelper(this, this)
        myVoiceToTextHelper?.checkPermissionForVoiceToText()
        applyPagination()
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
        logE("isChatIDAvailabel: $isChatIdAvailable")
        logE("chatId: $chatId")
        userRealName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
        setUserData()
        initAdapter()
        if (isChatIdAvailable) {
            Global.currentChatID = chatId
            hitConversationApi(0)
            SocketIO.emitReadAllMessages(
                chatId.toString(),
                sharedPrefsHelper.getUser()?.id.toString()
            )
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
        } else {
            receiverId = intent.getStringExtra(AppConstants.RECEIVER_ID)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", receiverId)
            SocketIO.socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                this.chatId = chatId
                Global.currentChatID = chatId
                runOnUiThread {
                    hitConversationApi(0)
                }
            })
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 1)
        }
    }

    override fun onResume() {
        super.onResume()
        myVoiceToTextHelper = MyVoiceToTextHelper(this, this)
        myVoiceToTextHelper?.checkPermissionForVoiceToText()
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

    private fun applyPagination() {
        binding.chatMessagesRecycler.addOnScrollListener(object :
            PaginationScrollListener(binding.chatMessagesRecycler.layoutManager as LinearLayoutManager) {
            override val isLastPage: Boolean
                get() = chatList1.size == 0
            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                binding.pbHeader.visibility = View.VISIBLE
                loading = true
                index += 20
                hitConversationApi(index)
            }
        })
    }

    fun hitConversationApi(offset: Int) {
        val params = HashMap<String, String>()
        params["chat_id"] = this.chatId.toString()
        params["offset"] = offset.toString()
        viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)

    }

    private fun setUserData() {
//        userRealName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
        profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
//        isFromOutside = intent.getBooleanExtra(AppConstants.IS_FROM_OUTSIDE, false)
        callerID = intent.getLongExtra(AppConstants.CALLER_USER_ID, 0)
        binding.header.tvName.text = userRealName
        GlideDownloder.load(
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
                renderResponse(apiResponse.data as ChatMessagesResponse)
                logE("+${apiResponse.data}")
            }
            Status.ERROR -> {
                loading = false
                binding.pbCenter.visibility = View.GONE
                binding.pbHeader.visibility = View.GONE
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderResponse(response: ChatMessagesResponse?) {
        logE("socketResponse: $response")
        response?.let { it ->
            it.data?.let {
                chatList1 = it
                logE("All Messages Api Called")
                (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(it)
            }
        }
    }

    private fun consumeMediaResponse(apiResponse: ApiResponse<MediaResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                logE("Loading Audio")
            }
            Status.SUCCESS -> {
                renderMediaResponse(apiResponse.data as MediaResponse)
            }
            Status.ERROR -> {
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderMediaResponse(response: MediaResponse?) {
        logE("socketResponse: $response")
        response?.let {
            it.data?.let { list ->
                emitNewMessageToSocket(
                    "",
                    list[0].type.toString(),
                    list[0].file_id.toString(),
                    list[0].duration.toLong(),
                    list[0].thumbnail,
                    list[0].identifier,
                    list[0].groupId,
                    list[0].isGroup
                )
            }
        }
    }

    private fun uploadMedia(
        identifier: String,
        file: String,
        duration: Long,
        type: String,
        groupId: Long,
        isGroup: Int
    ) {
        val params = HashMap<String, RequestBody>()
        params["identifier"] = RequestBody.create(MediaType.parse("text/plain"), identifier)
        params["duration"] = RequestBody.create(MediaType.parse("text/plain"), duration.toString())
        params["type"] = RequestBody.create(MediaType.parse("text/plain"), type)
        params["group_id"] = RequestBody.create(MediaType.parse("text/plain"), groupId.toString())
        params["is_group"] = RequestBody.create(MediaType.parse("text/plain"), isGroup.toString())
        viewModel.hitUploadAudioApi(
            sharedPrefsHelper.getUser()?.token, params,
            getFileBody(file, "file")
        )
    }

    private fun sendMediaMessage(
        file: String,
        type: String,
        duration: Long,
        groupId: Long,
        isGroup: Int
    ) {
        val identifier = System.currentTimeMillis().toString()
        when (type) {
            AppConstants.AUDIO_MESSAGE -> {
                lastMessage = "Audio"
                createChatObject(
                    AppConstants.DUMMY_STRING,
                    file,
                    type,
                    identifier
                    , groupId, isGroup
                )
                uploadMedia(identifier, file, duration, type, groupId, isGroup)
            }
            AppConstants.IMAGE_MESSAGE -> {
                lastMessage = "Image"
                createChatObject(
                    AppConstants.DUMMY_STRING,
                    file,
                    type,
                    identifier
                    , groupId, isGroup
                )
//                logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                val convertedFile = Compressor(this).compressToFile(File(file))
//                logE("After Conversion : ${getReadableFileSize(convertedFile.length())}")
                uploadMedia(
                    identifier,
                    convertedFile.absolutePath,
                    duration,
                    type,
                    groupId,
                    isGroup
                )
            }
            AppConstants.VIDEO_MESSAGE -> {
                lastMessage = "Video"
                createChatObject(
                    AppConstants.DUMMY_STRING,
                    file,
                    type,
                    identifier,
                    groupId, isGroup
                )
                val fileSize = getFileSizeInBytes(file)
                if (fileSize < 2000) {
                    logE("File size is less than 2MB : $fileSize")
                    uploadMedia(identifier, file, duration, type, groupId, isGroup)
                } else {
                    logE("File size is greater than 2MB : $fileSize")
//                    logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                    val output =
                        Environment.getExternalStorageDirectory().toString() + File.separator + System.currentTimeMillis() + ".mp4"
                    object : Thread() {
                        override fun run() {
                            super.run()
                            VideoCompressor().compressVideo(file, output)
                            runOnUiThread {
                                logE("File size after conversion : ${getFileSizeInBytes(output)}")
                                //                                logE("updatePosts: isVideo = 1")
//                                logE(
//                                    "After Conversion : ${getReadableFileSize(File(output).length())}"
//                                )
                                uploadMedia(identifier, output, duration, type, groupId, isGroup)
                            }
                        }
                    }.start()
                }
            }
        }
    }

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            logE("User Stops Typing")
            SocketIO.typingEvent(
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    logE("User is typing")
                    SocketIO.typingEvent(
                        AppConstants.START_TYPING,
                        sharedPrefsHelper.getUser()?.id.toString(),
                        chatId.toString()
                    )
                    handler.removeCallbacks(inputFinishChecker)
                    logE("id: ${sharedPrefsHelper.getUser()?.id.toString()}, chatID: $chatId")
                }
            }
        })
    }

    private fun addMessage(chatData: ChatData) {
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatData)
        binding.chatMessagesRecycler.scrollToPosition(0)
    }

    private fun emitNewMessageToSocket(
        message: String, type: String, fileID: String,
        duration: Long, thumbnail: String?, identifier: String, groupId: String, isGroup: Int
    ) {
        SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(),
            chatId.toString(), message, type,
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(),
            fileID, duration, thumbnail.toString(), identifier
            , sharedPrefsHelper.getLanguage().toString(), groupId, isGroup
        )
    }

    private fun createChatObject(
        message: String,
        file: String,
        type: String,
        identifier: String,
        groupId: Long,
        isGroup: Int
    ) {
        lastMsgTime = System.currentTimeMillis() / 1000L
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
                lastMsgTime.toDouble(), sharedPrefsHelper.getLanguage(), groupId, isGroup
            )
        )
    }

    private fun sendMessage() {
        val identifier = System.currentTimeMillis().toString()
        createChatObject(
            binding.lvBottomChat.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE, identifier,
            0, 0
        )
        emitNewMessageToSocket(
            binding.lvBottomChat.editTextMessage.text.toString(), AppConstants.TEXT_MESSAGE,
            AppConstants.DUMMY_STRING, 0, AppConstants.DUMMY_STRING, identifier, identifier, 0
        )
        logE("Message Emitted to socket")
        lastMessage = binding.lvBottomChat.editTextMessage.text.toString()
        binding.lvBottomChat.editTextMessage.setText("")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSend -> {
                if (myChatMediaHelper?.fileOutput()?.isEmpty() == true &&
                    binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
                ) {
                    sendMessage()
                    logE("Text Message")
                } else {
                    if (myChatMediaHelper?.isFileReady() == true) {
                        logE("Audio Message")
                        myChatMediaHelper?.getTotalDuration()?.let {
                            sendMediaMessage(
                                myChatMediaHelper?.fileOutput().toString(),
                                AppConstants.AUDIO_MESSAGE, it,
                                0, 0
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
                logE("Mic Clicked")
                myChatMediaHelper?.initRecorderWithPermissions()
            }
            R.id.ivAttach -> {
                myChatMediaHelper?.openAttachments()
            }
            R.id.ivAudio -> {
                val intent = Intent(this, AudioCallActivity::class.java)
                intent.putExtra(AppConstants.INITIATOR, true)
                intent.putExtra(AppConstants.CALLER_USER_ID, callerID)
                intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
                startActivity(intent)
            }
            R.id.ivVideo -> {
                val intent = Intent(this, VideoCallActivity::class.java)
                intent.putExtra(AppConstants.INITIATOR, true)
                intent.putExtra(AppConstants.CALLER_USER_ID, callerID)
                intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
                startActivity(intent)
            }
        }
    }

    private fun sendVideoOrImage(list: ArrayList<MediaList>?) {
        myChatMediaHelper?.hideAttachments()
        val groupId = System.currentTimeMillis()
        if (list?.size!! > 3) {
            list?.let { uploadMediaList(it, groupId, 1) }

        } else {
            list?.let { uploadMediaList(it, groupId, 0) }

        }
        logE("List size : $list")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SandriosCamera.RESULT_CODE -> {
                    if (data?.getSerializableExtra(SandriosCamera.MEDIA) is Media) {
                        val media = data.getSerializableExtra(SandriosCamera.MEDIA) as Media
                        if (media.type == SandriosCamera.MediaType.PHOTO) {
                            logE("onActivity Received")
                            sendMediaMessage(
                                media.path,
                                AppConstants.IMAGE_MESSAGE, 0,
                                0, 0
                            )
                        } else if (media.type == SandriosCamera.MediaType.VIDEO) {
                            sendMediaMessage(
                                media.path,
                                AppConstants.VIDEO_MESSAGE, 0,
                                0, 0
                            )
                        }
                    }
                }
                AppConstants.SELECTED_IMAGES -> {
                    binding.lvBottomChat.lvForAttachment.visibility = View.GONE
                    if (data != null) {
                        val list =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
                        val intent = Intent(this, AttachmentActivity::class.java)
                        intent.putExtra(AppConstants.SELECTED_IMAGES_VIDEOS, list)
                        startActivityForResult(intent, AppConstants.SELECT_IMAGES_VIDEOS)

                    }
                }
                AppConstants.SELECT_IMAGES_VIDEOS -> {
                    if (data != null) {
                        val list =
                            data.getSerializableExtra(AppConstants.SELECTED_IMAGES_VIDEOS) as ArrayList<MediaList>
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
                if (myChatMediaHelper!!.myPlayer!!.isPlaying) {
                    logE("Media Player is playing")
                    myChatMediaHelper?.myPlayer?.stop()
                    myChatMediaHelper?.myPlayer?.reset()
                    myChatMediaHelper?.myPlayer?.release()
                    myChatMediaHelper?.myPlayer = null
                }
            } else {
                logE("Media Player is null")
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        Global.currentChatID = -1
        myChatMediaHelper?.myPlayer = MediaPlayer()
        myVoiceToTextHelper?.destroy()
    }

    override fun onBackPressed() {
//        val intent = Intent()
        /*if (isFromOutside) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }*/ /*else {
               if (lastMessage?.isNotEmpty() == true && lastMsgTime != null) {
                   intent.putExtra(AppConstants.LAST_MESSAGE, lastMessage.toString())
                   intent.putExtra(AppConstants.LAST_MESSAGE_TIME, lastMsgTime.toString())
                   intent.putExtra(AppConstants.IsSEEN, false)
               } else {
                   intent.putExtra(AppConstants.IsSEEN, true)
               }
               setResult(Activity.RESULT_OK, intent)
               finish()
        }*/
        setResult(Activity.RESULT_OK)
        finish()
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
                        SocketIO.emitMessageSeen(
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
                    val userId = jsonObject.getString("user_id")
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
        logE("typing response: $jsonObject")
        val chatId1 = jsonObject.getString("chat_id")
        if (chatId1.toInt() == chatId) {
            runOnUiThread {
                if (isTyping) {
                    val user: String? = jsonObject.getString("user")
                    if (user.equals(userRealName)) {
                        logE("Is Typing")
                        binding.header.tvTyping.visibility = View.VISIBLE
                    }
                } else {
                    logE("Not Typing")
                    binding.header.tvTyping.visibility = View.GONE
                }
            }
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                myVoiceToTextHelper!!.startVoiceToText()
            }
            MotionEvent.ACTION_UP -> {
                myVoiceToTextHelper!!.stopVoiceToText()
            }
        }
        return v?.onTouchEvent(event) ?: true
    }

    override fun onResultVoiceToText(list: ArrayList<String>) {
        if (list[0] == "type") {
            showKeyBoard()
        } else {
            if (list[0] == "over" && binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
            ) {
                sendMessage()

            } else {
                binding.lvBottomChat.editTextMessage.setText(list[0])
                binding.lvBottomChat.editTextMessage.setSelection(binding.lvBottomChat.editTextMessage.length())
            }
        }
    }


    private fun showKeyBoard() {
        val imm =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        setFocusCursor()
    }

    private fun setFocusCursor() {
        binding.lvBottomChat.editTextMessage.isFocusable = true
        binding.lvBottomChat.editTextMessage.isFocusableInTouchMode = true
        binding.lvBottomChat.editTextMessage.requestFocus()
    }

    private fun uploadMediaList(list: ArrayList<MediaList>, groupId: Long, isGroup: Int) {
        list.let {
            it?.forEach { media ->
                if (media.type == 0) {

                    sendMediaMessage(media.file, AppConstants.IMAGE_MESSAGE, 0, groupId, isGroup)
                } else {
                    sendMediaMessage(media.file, AppConstants.VIDEO_MESSAGE, 0, groupId, isGroup)
                }
            }
        }
    }
}