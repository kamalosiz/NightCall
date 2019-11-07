package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.AudioRecordView
import com.example.kalam_android.wrapper.GlideDownloder
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_for_chat_screen.view.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatDetailActivity : BaseActivity(), AudioRecordView.RecordingListener, View.OnClickListener,
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
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var path: File
    private lateinit var file: File
    private val delay: Long = 1000
    private var lastTextEdit: Long = 0
    var handler = Handler()
    private var state: Boolean = false
    private var chatList1: ArrayList<ChatData>? = null
    private var profileImage: String = ""
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

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
        binding.recordingView.recordingListener = this
        binding.chatMessagesRecycler.adapter =
            ChatMessagesAdapter(this, sharedPrefsHelper.getUser()?.id.toString())
        binding.recordingView.imageViewSend.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        initRecorderWithPermissions()
//        moveRecyclerView()
        checkSomeoneTyping()
        SocketIO.setListener(this)
        SocketIO.setTypingListener(this)
        clickListener()
    }

    private fun clickListener() {

        binding.header.tvName.setOnClickListener {
            val intent = Intent(this@ChatDetailActivity, UserProfileActivity::class.java)
            intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
            intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
            startActivity(intent)
        }
    }

    private fun gettingChatId() {
        val isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        val params = HashMap<String, String>()
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            params["chat_id"] = chatId.toString()
            viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
        } else {
            receiverId = intent.getStringExtra(AppConstants.RECEIVER_ID)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", receiverId)
            SocketIO.socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                Debugger.e(TAG, "ID $chatId")
                this.chatId = chatId
                runOnUiThread {
                    params["chat_id"] = chatId.toString()
                    viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
                }
            })
        }
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
                binding.pbCenter.visibility = View.VISIBLE
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
        logE("socketResponse: $response")
        response?.let {
            it.data?.reverse()
            chatList1 = ArrayList()
            chatList1 = it.data
            logE("All Messages Api Called")
            (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(it.data)
            it.data?.size?.let { it1 -> binding.chatMessagesRecycler.scrollToPosition(it1 - 1) }
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
            /*SocketIO.emitNewMessage(
                sharedPrefsHelper.getUser()?.id.toString(),
                chatId.toString(), it.data?.file_url.toString(), "audio",
                sharedPrefsHelper.getUser()?.firstname.toString()
                        + " " + sharedPrefsHelper.getUser()?.lastname.toString()
            )*/
            emitNewMessage(
                "", AppConstants.AUDIO_MESSAGE, it.data?.file_url.toString(), "01:00"
            )
            logE("Audio Successfully emitted to server")
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    private fun initRecorderWithPermissions() {
        Handler().postDelayed(
            {
                PermissionHelper.withActivity(this).addPermissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).listener(object : MediaPermissionListener {
                    override fun onPermissionGranted() {
                        initRecorder()
                    }

                    override fun onPermissionDenied() {
                        logE("onPermissionDenied")
                    }
                }).build().init()
            }, 100
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun initRecorder() {
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyMMddHHmmssZ")
        path =
            File(Environment.getExternalStorageDirectory().path, "${sdf.format(date)}recording.mp3")

        try {
            file = File.createTempFile("recording", ".mp3", path)
        } catch (e: IOException) {
            Log.wtf("File Path:", e.message)
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile(path.absolutePath)
    }

    private fun startRecording() {
        initRecorder()
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (state) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                state = false
                val time = timeFormatter.format(Date(getDuration(path).toLong()))
                if (time >= "00:01") {
                    sendVoiceMessage()
                }

            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRecordingStarted() {
        startRecording()
    }

    override fun onRecordingLocked() {
    }

    override fun onRecordingCompleted() {
        stopRecording()
    }

    override fun onRecordingCanceled() {
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imageViewSend -> {
                sendMessage()
            }
            R.id.rlBack -> {
                onBackPressed()
            }
        }
    }

    private fun uploadAudioMedia() {
        viewModel.hitUploadAudioApi(
            sharedPrefsHelper.getUser()?.token,
            getFileBody(path.path.toString(), "file")
        )
    }

    private fun sendVoiceMessage() {
/*        *//* addMessage(
             path.absolutePath,
             path.absolutePath,
             sharedPrefsHelper.getUser()?.id
             , AppConstants.AUDIO_MESSAGE
         )*//*
        val message = ChatData(
            AppConstants.DUMMY_DATA,
            chatId,
            sharedPrefsHelper.getUser()?.id,
            AppConstants.DUMMY_DATA,
            path.absolutePath,
            AppConstants.DUMMY_DATA,
            AppConstants.DUMMY_DATA,
            AppConstants.AUDIO_MESSAGE,
            AppConstants.DUMMY_STRING,
            0,
            0,
            path.absolutePath
        )
        addMessage(
            *//* binding.recordingView.editTextMessage.text.toString(),
             binding.recordingView.editTextMessage.text.toString(),
             sharedPrefsHelper.getUser()?.id, AppConstants.TEXT_MESSAGE*//*
            message
        )*/
        createChatObject(AppConstants.DUMMY_STRING, path.absolutePath, AppConstants.AUDIO_MESSAGE)

        uploadAudioMedia()
        logE("Audio Api hit successfully")
        chatList1?.size?.let { binding.chatMessagesRecycler.scrollToPosition(it - 1) }
    }

    /*  private fun moveRecyclerView() {
          binding.chatMessagesRecycler.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
              chatList1?.size?.let {
                  binding.chatMessagesRecycler.scrollToPosition(
                      it - 1
                  )
              }
          }
      }*/

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
        binding.recordingView.editTextMessage.addTextChangedListener(object : TextWatcher {
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

    private fun sendMessage() {
        /*  val message = ChatData(
              AppConstants.DUMMY_DATA,
              chatId,
              sharedPrefsHelper.getUser()?.id,
              AppConstants.DUMMY_DATA,
              binding.recordingView.editTextMessage.text.toString(),
              AppConstants.DUMMY_DATA,
              AppConstants.DUMMY_DATA,
              AppConstants.TEXT_MESSAGE,
              AppConstants.DUMMY_STRING,
              0,
              0,
              binding.recordingView.editTextMessage.text.toString()
          )
          addMessage(
              *//* binding.recordingView.editTextMessage.text.toString(),
             binding.recordingView.editTextMessage.text.toString(),
             sharedPrefsHelper.getUser()?.id, AppConstants.TEXT_MESSAGE*//*
            message
        )*/
        createChatObject(
            binding.recordingView.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE
        )
        /*SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(),
            chatId.toString(),
            binding.recordingView.editTextMessage.text.toString(), "text",
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString()
        )*/
        emitNewMessage(
            binding.recordingView.editTextMessage.text.toString(),
            AppConstants.TEXT_MESSAGE, "", ""
        )
        logE("Message Emitted to socket")
        binding.recordingView.editTextMessage.setText("")
        chatList1?.size?.let { binding.chatMessagesRecycler.scrollToPosition(it - 1) }
    }

    override fun socketResponse(jsonObject: JSONObject) {
        val gson = Gson()
        val data = gson.fromJson(jsonObject.toString(), ChatData::class.java)
        logE("Chats Details New Message is called: $jsonObject")
        logE("Chats Details New Message is called: $data")
        /* val msg: String? = jsonObject.getString("msg")
         val msgType: String? = jsonObject.getString("msg_type")
         val originalMsg = jsonObject.getString("orignal_msg")
         val localChatID = jsonObject.getString("chat_id")
         val senderName = jsonObject.getString("sender_name")*/
        if (/*localChatID.toInt()*/data.chat_id == chatId) {
            runOnUiThread {
                addMessage(/*msg, originalMsg, AppConstants.DUMMY_DATA, msgType.toString()*/data)
            }
        }
    }

    override fun typingResponse(jsonObject: JSONObject, isTyping: Boolean) {
        logE("typing response: $jsonObject")
        val chatId1 = jsonObject.getString("chat_id")
        if (chatId1.toInt() == chatId) {
            if (isTyping) {
                val user: String? = jsonObject.getString("user")
                if (user.equals(userRealName)) {
                    val list: List<String>? = user?.split(" ")
                    binding.header.tvName.text =
                        StringBuilder(list?.get(0).toString()).append(" is typing...")
                }
            } else {
                binding.header.tvName.text = userRealName
            }
        }
    }

    private fun createChatObject(message: String, file: String, type: String) {
        addMessage(
            ChatData(
                AppConstants.DUMMY_DATA,
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
                message
            )
        )
    }

    private fun addMessage(/*msg: String?, originalMsg: String?, id: Int?, type: String*/chatData: ChatData) {
        /* val message = ChatData(
             AppConstants.DUMMY_DATA, chatId, id, AppConstants.DUMMY_DATA
             , msg,
             AppConstants.DUMMY_DATA, AppConstants.DUMMY_DATA,
             type, AppConstants.DUMMY_STRING, originalMsg
         )*/
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatData)
        chatList1?.size?.let { binding.chatMessagesRecycler.scrollToPosition(it - 1) }
    }

    private fun emitNewMessage(message: String, type: String, file: String, duration: String) {
        SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(), chatId.toString(), message, type,
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(), file, duration
        )
    }

    private fun getDuration(file: File): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationStr.toInt()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}