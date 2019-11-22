package com.example.kalam_android.view.activities

import android.app.Activity
import android.content.Intent
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.MediaListCallBack
import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.NewMessageListener
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.helper.MyChatMediaHelper
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
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class ChatDetailActivity : BaseActivity(), View.OnClickListener,
    NewMessageListener, MessageTypingListener, MediaListCallBack {

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
    private var myChatMediaHelper: MyChatMediaHelper? = null
    private var lastMessage: String? = null
    private var lastMsgTime: Long? = null
    private var multiPartList: ArrayList<MultipartBody.Part> = ArrayList()


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
        myChatMediaHelper = MyChatMediaHelper.getInstance(this@ChatDetailActivity, binding, this)
        applyPagination()
    }

    private fun initListeners() {
        binding.lvBottomChat.ivSend.setOnClickListener(this)
        binding.lvBottomChat.ivCamera.setOnClickListener(this)
//        binding.lvBottomChat.ivAttach.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        binding.lvBottomChat.ivMic.setOnClickListener(this)
        binding.header.llProfile.setOnClickListener(this)
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
                profileImage.toString()
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

    private fun gettingChatId() {
        isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            hitConversationApi(0)
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
                    hitConversationApi(0)
                }
            })
            sharedPrefsHelper.put(AppConstants.IS_FROM_CONTACTS, 1)
        }
        logE("Chat ID  : $chatId")
    }

    fun hitConversationApi(offset: Int) {
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

    private fun consumeAudioResponse(apiResponse: ApiResponse<MediaResponse>?) {
        when (apiResponse?.status) {

            Status.LOADING -> {
                logE("Loading Audio")
            }
            Status.SUCCESS -> {
                renderAudioResponse(apiResponse.data as MediaResponse)
            }
            Status.ERROR -> {
                toast("Something went wrong please try again")
                logE("consumeResponse ERROR: " + apiResponse.error.toString())
            }
            else -> {
            }
        }
    }

    private fun renderAudioResponse(response: MediaResponse?) {
        logE("socketResponse: $response")
        response?.let {
            myChatMediaHelper?.getTotalDuration()?.let { it1 ->
                emitNewMessageToSocket(
                    it.data?.message.toString(),
                    messageType,
                    it.data?.file_id.toString(),
                    it1
                )
            }
            lastMsgTime = System.currentTimeMillis() / 1000L
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

    private fun sendMediaMessage(file: String, type: String, duration: Long) {
        val identifier = System.currentTimeMillis().toString()
        when (type) {
            AppConstants.AUDIO_MESSAGE -> {
                lastMessage = "Audio"
                messageType = type
                createChatObject(AppConstants.DUMMY_STRING, file, type, identifier)
                uploadMedia(identifier, file, duration)
            }
            AppConstants.IMAGE_MESSAGE -> {
                lastMessage = "Image"
                messageType = type
                createChatObject(AppConstants.DUMMY_STRING, file, type, identifier)
//                logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                val convertedFile = Compressor(this).compressToFile(File(file))
//                logE("After Conversion : ${getReadableFileSize(convertedFile.length())}")
                uploadMedia(identifier, convertedFile.absolutePath, duration)
            }
            AppConstants.VIDEO_MESSAGE -> {
                lastMessage = "Video"
                messageType = type
                val thumb = ThumbnailUtils.createVideoThumbnail(
                    File(file).path,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
                createChatObject(
                    AppConstants.DUMMY_STRING,
                    Global.bitmapToFile(applicationContext, thumb).absolutePath,
                    type,
                    identifier
                )
//                logE("Before Conversion : ${getReadableFileSize(File(file).length())}")
                val output =
                    Environment.getExternalStorageDirectory().toString() + File.separator + System.currentTimeMillis() + ".mp4"
                object : Thread() {
                    override fun run() {
                        super.run()
                        VideoCompressor().compressVideo(file, output)
                        runOnUiThread {
                            /*logE("updatePosts: isVideo = 1")
                            logE(
                                "After Conversion : ${getReadableFileSize(File(output).length())}"
                            )*/
                            uploadMedia(identifier, output, duration)
                        }
                    }
                }.start()
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
        lastMessage = binding.lvBottomChat.editTextMessage.text.toString()
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
                if (myChatMediaHelper?.fileOutput()?.isEmpty() == true &&
                    binding.lvBottomChat.editTextMessage.text.toString().isNotEmpty()
                ) {
                    sendMessage()
                    logE("Text Message")
                    lastMsgTime = System.currentTimeMillis() / 1000L
                } else {
                    if (myChatMediaHelper?.isFileReady() == true) {
                        logE("Audio Message")
                        myChatMediaHelper?.getTotalDuration()?.let {
                            sendMediaMessage(
                                myChatMediaHelper?.fileOutput().toString(),
                                AppConstants.AUDIO_MESSAGE, it
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
                        binding.header.ivProfileImage, // Starting view
                        transitionName    // The String
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
//            R.id.ivAttach -> {
//                myChatMediaHelper?.openAttachments()
//            }
        }
    }

    /* private fun createMultiPartList(list: ArrayList<MediaList>) {
         for (i in list.indices) {
             val file = File(list[i])
             val requestFileProfile =
                 RequestBody.create(MediaType.parse("multipart/form-data"), file)
             val body = MultipartBody.Part.createFormData("images$i", file.name, requestFileProfile)
             multiPartList.add(body)
         }
     }*/

    override fun mediaListResponse(list: ArrayList<MediaList>?) {
        myChatMediaHelper?.hideAttachments()
         list?.let {
           /*  if (it.size < 4) {
                 it.forEach { media ->
                     if (media.type == 0) {
                         sendMediaMessage(media.file, AppConstants.IMAGE_MESSAGE, 0)
                     } else {
                         sendMediaMessage(media.file, AppConstants.VIDEO_MESSAGE, 0)
                     }
                 }
             }*/
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
                                AppConstants.IMAGE_MESSAGE, 0
                            )
                        } else if (media.type == SandriosCamera.MediaType.VIDEO) {
                            sendMediaMessage(
                                media.path,
                                AppConstants.VIDEO_MESSAGE, 0
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        if (lastMessage?.isNotEmpty() == true && lastMsgTime != null) {
            intent.putExtra(AppConstants.LAST_MESSAGE, lastMessage.toString())
            intent.putExtra(AppConstants.LAST_MESSAGE_TIME, lastMsgTime.toString())
            intent.putExtra(AppConstants.IsSEEN, false)
        } else {
            intent.putExtra(AppConstants.IsSEEN, true)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}