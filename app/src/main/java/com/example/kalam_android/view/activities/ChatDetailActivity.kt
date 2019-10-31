package com.example.kalam_android.view.activities

import android.Manifest
import android.app.Activity
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
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.permissionHelper.helper.PermissionHelper
import com.example.kalam_android.util.permissionHelper.listeners.MediaPermissionListener
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.adapter.ChatMessagesAdapter
import com.example.kalam_android.viewmodel.ChatMessagesViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.AudioRecordView
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_for_chat_screen.view.*
import java.io.File
import java.io.IOException
import javax.inject.Inject


@Suppress("DEPRECATION")
class ChatDetailActivity : BaseActivity(), AudioRecordView.RecordingListener, View.OnClickListener {

    private val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var chatId = -1
    private var receiverId: String? = null
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
    private var audioPath: String? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private var pause: Boolean = false
    private var chatList: ArrayList<ChatData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(ChatMessagesViewModel::class.java)
        viewModel.allChatResponse().observe(this, Observer {
            consumeResponse(it)
        })
        gettingChatId()
        initRecorderWithPermissions()
        binding.recordingView.recordingListener = this
        binding.chatMessagesRecycler.adapter =
            ChatMessagesAdapter(this, sharedPrefsHelper.getUser()?.id.toString())
        binding.recordingView.imageViewSend.setOnClickListener(this)
        moveRecyclerView()
        checkSomeoneTyping()
    }

    private fun gettingChatId() {
        val isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        val params = HashMap<String, String>()
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
        } else {
            receiverId = intent.getStringExtra(AppConstants.RECEIVER_ID)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
            jsonObject.addProperty("receiver_id", receiverId)
            SocketIO.socket?.emit(AppConstants.START_CAHT, jsonObject, Ack {
                val chatId = it[0] as Int
                Debugger.e(TAG, "ID $chatId")
                this.chatId = chatId
            })
        }
        params["chat_id"] = chatId.toString()
        viewModel.hitAllChatApi(sharedPrefsHelper.getUser()?.token.toString(), params)
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
        logE("response: $response")
        response?.let {
            it.data?.reverse()
            chatList = ArrayList()
            chatList = it.data
            (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).updateList(it.data)
            it.data?.size?.let { it1 -> binding.chatMessagesRecycler.scrollToPosition(it1 - 1) }
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

    private fun initRecorder() {

        path = File(Environment.getExternalStorageDirectory().path, "recording.mp3")
        audioPath = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
        try {
            file = File.createTempFile("recording", ".mp3", path)
        } catch (e: IOException) {
            Log.wtf("File Path:", e.message)
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile(audioPath)
    }

    private fun startRecording() {
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
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    /*   private fun pauseRecording() {
           if (state) {
               if (!recordingStopped) {
                   Toast.makeText(this, "Stopped!", Toast.LENGTH_SHORT).show()
                   mediaRecorder?.pause()
                   recordingStopped = true
   //                button_pause_recording.text = "Resume"
               } else {
                   resumeRecording()
               }
           }
       }*/

    /*   @SuppressLint("RestrictedApi", "SetTextI18n")
       @TargetApi(Build.VERSION_CODES.N)
       private fun resumeRecording() {
           Toast.makeText(this, "Resume!", Toast.LENGTH_SHORT).show()
           mediaRecorder?.resume()
   //        button_pause_recording.text = "Pause"
           recordingStopped = false
       }*/

    override fun onRecordingStarted() {
        startRecording()
    }

    override fun onRecordingLocked() {
    }

    override fun onRecordingCompleted() {
        stopRecording()
        sendVoiceMessage()
    }

    override fun onRecordingCanceled() {
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imageViewSend -> {
                sendMessage()
            }
        }
    }

    private fun sendVoiceMessage() {
        val message = ChatData(
            -1, chatId, sharedPrefsHelper.getUser()?.id, -1
            , audioPath.toString(),
            -1, -1,
            "audio", -1, ""
        )
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(message)
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
        jsonObject.addProperty("chat_id", chatId.toString())
        jsonObject.addProperty("message", audioPath)
        jsonObject.addProperty("type", "audio")
        SocketIO.socket?.emit(AppConstants.SEND_MESSAGE, jsonObject)
        logE("Message Emitted to socket")
        binding.recordingView.editTextMessage.setText("")
        chatList?.size?.let { binding.chatMessagesRecycler.scrollToPosition(it - 1) }
    }

    private fun sendMessage() {
        val message = ChatData(
            -1, chatId, sharedPrefsHelper.getUser()?.id, -1
            , binding.recordingView.editTextMessage.text.toString(),
            -1, -1,
            "text", -1, ""
        )
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(message)
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", sharedPrefsHelper.getUser()?.id.toString())
        jsonObject.addProperty("chat_id", chatId.toString())
        jsonObject.addProperty("message", binding.recordingView.editTextMessage.text.toString())
        jsonObject.addProperty("type", "text")
        SocketIO.socket?.emit(AppConstants.SEND_MESSAGE, jsonObject)
        logE("Message Emitted to socket")
        binding.recordingView.editTextMessage.setText("")
        chatList?.size?.let { binding.chatMessagesRecycler.scrollToPosition(it - 1) }
    }

    private fun moveRecyclerView() {
        binding.chatMessagesRecycler.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            chatList?.size?.let {
                binding.chatMessagesRecycler.scrollToPosition(
                    it - 1
                )
            }
        }
    }

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            logE("User Stops Typing")
            SocketIO.checkSomeoneTyping(
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
                logE("User is typing")
                SocketIO.checkSomeoneTyping(
                    AppConstants.START_TYPING,
                    sharedPrefsHelper.getUser()?.id.toString(),
                    chatId.toString()
                )
                handler.removeCallbacks(inputFinishChecker)
            }

        })
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
