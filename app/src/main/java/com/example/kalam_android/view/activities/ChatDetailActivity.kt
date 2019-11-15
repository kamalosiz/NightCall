package com.example.kalam_android.view.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.NewMessageListener
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.repository.model.AudioResponse
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloder
import com.example.kalam_android.helper.MyMediaRecorder
import com.example.kalam_android.wrapper.SocketIO
import com.fxn.pix.Pix
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sandrios.sandriosCamera.internal.ui.model.Media
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class ChatDetailActivity : BaseActivity(), View.OnClickListener,
    NewMessageListener, MessageTypingListener {

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
    private var isFromChatFragment = false
    private var messageType = ""
    private var myMediaRecorder: MyMediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        viewModel.audioResponse().observe(this, Observer {
            consumeAudioResponse(it)
        })
        gettingChatId()
        setUserData()
        initAdapter()
        initListeners()
        checkSomeoneTyping()
        SocketIO.setListener(this)
        SocketIO.setTypingListener(this)
        myMediaRecorder = MyMediaRecorder.getInstance(this, binding)
        applyPagination()
    }

    private fun initListeners() {
        binding.lvBottomChat.ivSend.setOnClickListener(this)
        binding.lvBottomChat.ivCamera.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        binding.lvBottomChat.ivMic.setOnClickListener(this)
        binding.header.tvName.setOnClickListener(this)
    }

    private fun initAdapter() {
        val linearLayout = LinearLayoutManager(this)
        linearLayout.reverseLayout = true
        linearLayout.stackFromEnd = false
        binding.chatMessagesRecycler.layoutManager = linearLayout
        binding.chatMessagesRecycler.adapter =
            ChatMessagesAdapter(this, sharedPrefsHelper.getUser()?.id.toString())
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
                hitAllChatApi(index)
            }
        })
    }

    private fun gettingChatId() {
        isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            hitAllChatApi(0)
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 2)
        } else {
            receiverId = intent.getStringExtra(AppConstants.RECEIVER_ID)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", receiverId)
            SocketIO.socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                this.chatId = chatId
                runOnUiThread {
                    hitAllChatApi(0)
                }
            })
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 1)
        }
    }

    fun hitAllChatApi(offset: Int) {
        val params = HashMap<String, String>()
        params["chat_id"] = this.chatId.toString()
        params["offset"] = offset.toString()
        viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
    }

    private fun setUserData() {
        userRealName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
        profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
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

    private fun consumeAudioResponse(apiResponse: ApiResponse<AudioResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                logE("Loading Audio")
            }
            Status.SUCCESS -> {
                renderAudioResponse(apiResponse.data as AudioResponse)
            }
            Status.ERROR -> {
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderAudioResponse(response: AudioResponse?) {
        logE("socketResponse: $response")
        response?.let {
            myMediaRecorder?.getTotalDuration()?.let { it1 ->
                emitNewMessageToSocket(
                    it.data?.message.toString(),
                    AppConstants.AUDIO_MESSAGE,
                    it.data?.file_id.toString(),
                    it1
                )
            }
            (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateIdentifier(it.data?.identifier.toString())
        }
    }

    private fun uploadMedia(identifier: String, file: String, duration: Long) {
        val params = HashMap<String, RequestBody>()
        params["identifier"] = RequestBody.create(MediaType.parse("text/plain"), identifier)
        params["duration"] = RequestBody.create(MediaType.parse("text/plain"), duration.toString())
        viewModel.hitUploadAudioApi(
            sharedPrefsHelper.getUser()?.token, params,
            getFileBody(file, "file")
        )
    }

    private fun sendMediaMessage(file: String, type: String, isImage: String, duration: Long) {
        val identifier = System.currentTimeMillis().toString()
        createChatObject(AppConstants.DUMMY_STRING, file, type, identifier)
        when (isImage) {
            AppConstants.AUDIO_MESSAGE -> {
                messageType = type
                uploadMedia(identifier, file, duration)
                logE("Audio Api hit successfully")
            }
            AppConstants.IMAGE_MESSAGE -> {
                messageType = type
//            logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                val convertedFile = Compressor(this).compressToFile(File(file))
//            logE("After Conversion : ${getReadableFileSize(convertedFile.length())}")
                uploadMedia(identifier, convertedFile.absolutePath, duration)
                logE("Image Api hit successfully")
            }
            AppConstants.VIDEO_MESSAGE -> {

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
        message: String, type: String, fileID: String, duration: Long
    ) {
        SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(), chatId.toString(), message, type,
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(), fileID, duration
        )
    }

    private fun createChatObject(message: String, file: String, type: String, identifier: String) {
        addMessage(
            ChatData(
                AppConstants.DUMMY_STRING,
                file, AppConstants.DUMMY_STRING,
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                ).toString(),
                AppConstants.DUMMY_DATA, chatId, sharedPrefsHelper.getUser()?.id,
                AppConstants.DUMMY_DATA, message, AppConstants.DUMMY_DATA, AppConstants.DUMMY_DATA,
                type, file, 0, 0, message, identifier
            )
        )
    }

    override fun socketResponse(jsonObject: JSONObject) {
        val gson = Gson()
        logE("New Message : $jsonObject")
        val data = gson.fromJson(jsonObject.toString(), ChatData::class.java)
        if (data.chat_id == chatId) {
            runOnUiThread {
                addMessage(data)
            }
        }
    }


    private fun sendMessage() {
        createChatObject(
            binding.lvBottomChat.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE, AppConstants.DUMMY_STRING
        )
        emitNewMessageToSocket(
            binding.lvBottomChat.editTextMessage.text.toString(),
            AppConstants.TEXT_MESSAGE, AppConstants.DUMMY_STRING, 0
        )
        logE("Message Emitted to socket")
        binding.lvBottomChat.editTextMessage.setText("")
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSend -> {
                if (myMediaRecorder?.fileOutput()?.isEmpty() == true &&
                    binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
                ) {
                    sendMessage()
                    logE("Text Message")
                } else {
                    if (myMediaRecorder?.isFileReady() == true) {
                        logE("Audio Message")
                        myMediaRecorder?.getTotalDuration()?.let {
                            sendMediaMessage(
                                myMediaRecorder?.fileOutput().toString(),
                                AppConstants.AUDIO_MESSAGE,
                                AppConstants.AUDIO_MESSAGE, it
                            )
                        }
                        myMediaRecorder?.hideRecorder()
                        myMediaRecorder?.cancel(false)
                    }
                }
            }
            R.id.rlBack -> {
                onBackPressed()
            }
            R.id.tvName -> {
                val intent = Intent(this@ChatDetailActivity, UserProfileActivity::class.java)
                intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
                intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
                startActivity(intent)
            }
            R.id.ivCamera -> {
                /* checkPixPermission(
                     this@ChatDetailActivity,
                     AppConstants.CHAT_IMAGE_CODE
                 )*/
                SandriosCamera
                    .with()
                    .setShowPicker(false)
                    .setMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH)
                    .enableImageCropping(false)
                    .launchCamera(this)
            }
            R.id.ivMic -> {
                myMediaRecorder?.initRecorderWithPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SandriosCamera.RESULT_CODE -> {
                    if (data?.getSerializableExtra(SandriosCamera.MEDIA) is Media) {
                        val media = data.getSerializableExtra(SandriosCamera.MEDIA) as Media
                        if (media.type == SandriosCamera.MediaType.PHOTO) {
                            sendMediaMessage(
                                media.path,
                                AppConstants.IMAGE_MESSAGE,
                                AppConstants.IMAGE_MESSAGE, 0
                            )
                            logE("File: ${media.path}")
                            logE("Type: ${media.type}")
                        } else if (media.type == SandriosCamera.MediaType.VIDEO) {
                            sendMediaMessage(
                                media.path,
                                AppConstants.VIDEO_MESSAGE,
                                AppConstants.VIDEO_MESSAGE, 0
                            )
                            logE("File: ${media.path}")
                            logE("Type: ${media.type}")
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}