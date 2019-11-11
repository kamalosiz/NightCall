package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.widget.SeekBar
import android.widget.Toast
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
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_for_chat_screen.view.*
import kotlinx.android.synthetic.main.layout_for_chat_screen.view.editTextMessage
import kotlinx.android.synthetic.main.layout_for_recoder.view.*
import omrecorder.*
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
import kotlin.math.max

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
    private lateinit var path: String
    private lateinit var output: String
    private val delay: Long = 1000
    private var lastTextEdit: Long = 0
    private var index = 0
    var handler = Handler()
    private var chatList1: ArrayList<ChatData> = ArrayList()
    private var profileImage: String = ""
    private var loading = false
    private var isPaging = false
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var recorder: Recorder? = null
    private var timeWhenStopped: Long = 0;
    private var isResumeRecorder = false
    private var isPlayPlayer = false
    private var isStopPlayer = false
    private var isStopRecording = false
    private var isPlayerRelease = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable: Runnable

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
        val linearLayout = LinearLayoutManager(this)
        linearLayout.reverseLayout = true
        linearLayout.stackFromEnd = false
        binding.chatMessagesRecycler.layoutManager = linearLayout
        binding.chatMessagesRecycler.adapter =
            ChatMessagesAdapter(this, sharedPrefsHelper.getUser()?.id.toString())
        binding.recordingView.imageViewSend.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        initRecorderWithPermissions()
        checkSomeoneTyping()
        SocketIO.setListener(this)
        SocketIO.setTypingListener(this)
        clickListener()
        applyPagination()
    }

    private fun initMediaPlayer() {


        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        mediaPlayer?.setDataSource(output)
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        initializeSeekBar()

    }

    private fun clickListener() {
        binding.header.tvName.setOnClickListener {
            val intent = Intent(this@ChatDetailActivity, UserProfileActivity::class.java)
            intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
            intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
            startActivity(intent)
        }
        binding.lvRecoder.ivMic.setOnClickListener {

            binding.lvRecoder.lvForRecorder.visibility = View.VISIBLE
        }

        binding.lvRecoder.lvForRecorder.ivRecord.setOnClickListener {

            playPauseRecorder()
        }

        binding.lvRecoder.lvForRecorder.ivPause.setOnClickListener {

            if (isPlayPlayer) {
                isPlayPlayer = false
                pausePlayer()

            } else {
                pauseRecorder()
            }
        }

        binding.lvRecoder.lvForRecorder.ivStop.setOnClickListener {
            if (isStopPlayer) {

                stopPlayer()

            } else {

                stopRecord()
            }
        }

        binding.lvRecoder.lvForRecorder.ivPlay.setOnClickListener {

            playAudio()
        }

        binding.lvRecoder.lvForRecorder.ivCancel.setOnClickListener {

            cancel()
        }
    }

    private fun playPauseRecorder() {

        if (!isResumeRecorder) {

            binding.lvRecoder.chronometer.base = SystemClock.elapsedRealtime()
            binding.lvRecoder.chronometer.start()
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_green)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_green)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = true
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = true
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
            recorder?.startRecording()
            isStopRecording = true

        } else {

            binding.lvRecoder.chronometer.base = SystemClock.elapsedRealtime() - timeWhenStopped
            binding.lvRecoder.chronometer.start()
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_green)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_green)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = true
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = true
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
            recorder?.resumeRecording()
            isStopRecording = true
        }
    }

    private fun pauseRecorder() {
        binding.lvRecoder.chronometer.stop()
        timeWhenStopped = SystemClock.elapsedRealtime() - binding.lvRecoder.chronometer.base
        binding.lvRecoder.chronometer.base = SystemClock.elapsedRealtime() - timeWhenStopped
        binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_green)
        binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_green)
        binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_red)
        binding.lvRecoder.lvForRecorder.ivPause.isClickable = true
        binding.lvRecoder.lvForRecorder.ivStop.isClickable = true
        binding.lvRecoder.lvForRecorder.ivRecord.isClickable = true
        recorder?.pauseRecording()
        isResumeRecorder = true
    }

    private fun pausePlayer() {

        if (mediaPlayer?.isPlaying!!) {
            mediaPlayer?.pause()
            binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_green)
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_green)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPlay.isClickable = true
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = true
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false

        }
    }

    private fun stopPlayer() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlayerRelease = true
        isStopPlayer = false
        binding.lvRecoder.lvForRecorder.seekBar.max =  0
        binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_green)
        binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
        binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_gray)
        binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
        binding.lvRecoder.lvForRecorder.ivPlay.isClickable = true
        binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
        binding.lvRecoder.lvForRecorder.ivStop.isClickable = false
        binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false

    }

    private fun stopRecord() {
        if (isStopRecording) {

            recorder?.stopRecording()
            recorder = null
            binding.lvRecoder.chronometer.stop()
            binding.lvRecoder.lvForRecorder.lvSeekBar.visibility = View.VISIBLE
            binding.lvRecoder.chronometer.visibility = View.GONE
            binding.lvRecoder.chronometer.base = 0
            binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_green)
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_gray)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPlay.isClickable = true
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = false
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
            isResumeRecorder = false
            isStopRecording = false

        } else {

            binding.lvRecoder.chronometer.stop()
            timeWhenStopped =
                SystemClock.elapsedRealtime() - binding.lvRecoder.chronometer.base
            binding.lvRecoder.chronometer.base =
                SystemClock.elapsedRealtime() - timeWhenStopped
            binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_green)
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_gray)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPlay.isClickable = true
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = false
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
            isResumeRecorder = true
        }
    }

    private fun playAudio() {

        binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_gray)
        binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_green)
        binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_green)
        binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
        binding.lvRecoder.lvForRecorder.ivPlay.isClickable = false
        binding.lvRecoder.lvForRecorder.ivPause.isClickable = true
        binding.lvRecoder.lvForRecorder.ivStop.isClickable = true
        binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
        isPlayPlayer = true
        isStopPlayer = true
        if (mediaPlayer != null && !mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
        } else {
            initMediaPlayer()
        }

        mediaPlayer?.setOnCompletionListener {

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayerRelease = true
            isStopPlayer = false
            binding.lvRecoder.lvForRecorder.seekBar.max = 0
            binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_green)
            binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
            binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_gray)
            binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_gray)
            binding.lvRecoder.lvForRecorder.ivPlay.isClickable = true
            binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
            binding.lvRecoder.lvForRecorder.ivStop.isClickable = false
            binding.lvRecoder.lvForRecorder.ivRecord.isClickable = false
        }
    }

    fun cancel() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        recorder?.stopRecording()
        recorder = null
        timeWhenStopped = 0;
        isResumeRecorder = false
        isPlayPlayer = false
        isStopPlayer = false
        isStopRecording = false
        isPlayerRelease = false
        mediaPlayer = null
        binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(R.drawable.ic_play_gray)
        binding.lvRecoder.lvForRecorder.ivPause.setImageResource(R.drawable.ic_pause_gray)
        binding.lvRecoder.lvForRecorder.ivStop.setImageResource(R.drawable.ic_stop_gray)
        binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(R.drawable.ic_record_green)
        binding.lvRecoder.lvForRecorder.ivPlay.isClickable = false
        binding.lvRecoder.lvForRecorder.ivPause.isClickable = false
        binding.lvRecoder.lvForRecorder.ivStop.isClickable = false
        binding.lvRecoder.lvForRecorder.ivRecord.isClickable = true
        binding.lvRecoder.lvForRecorder.visibility = View.GONE
        binding.lvRecoder.lvForRecorder.lvSeekBar.visibility = View.GONE
        binding.lvRecoder.chronometer.visibility = View.VISIBLE
        val file = File(output)
        if (file.exists()) {
            file.delete()

        }
    }

    private fun initializeSeekBar() {

        binding.lvRecoder.lvForRecorder.seekBar.max = mediaPlayer?.duration!!
        binding.lvRecoder.lvForRecorder.tvTotalTime.text =
            timeFormatter.format(mediaPlayer?.duration!!)
        runnable = Runnable {
            try {
                if (mediaPlayer != null) {
                    val currentSeconds = mediaPlayer?.currentPosition
                    currentSeconds?.let {
                        binding.lvRecoder.lvForRecorder.seekBar.progress = it
                    }

                    binding.lvRecoder.lvForRecorder.tvTimer.text =
                        timeFormatter.format(currentSeconds)
//                val difference = seconds - currentSeconds
                    handler.postDelayed(runnable, 1)
                }
            } catch (e: IllegalStateException) {

            }

        }
        handler.postDelayed(runnable, 1)

        binding.lvRecoder.lvForRecorder.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initializeRecorder() {

        path = "/recording_${System.currentTimeMillis()}.wav"
        output = getExternalFilesDir(null)?.absolutePath + path

        recorder = OmRecorder.wav(
            PullTransport.Default(mic()), file()
        )
    }

    private fun mic(): PullableSource {
        return PullableSource.Default(
            AudioRecordConfig.Default(
                MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO, 44100
            )
        )
    }

    private fun file(): File {
        return File(output)
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
                isPaging = true
                loading = true
                index += 20
                logE("chatListSize ${chatList1.size}")
                hitAllChatApi(index)
            }
        })
    }

    private fun gettingChatId() {
        val isFromChatFragment = intent.getBooleanExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, false)
        if (isFromChatFragment) {
            chatId = intent.getIntExtra(AppConstants.CHAT_ID, 0)
            hitAllChatApi(0)
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
                    hitAllChatApi(0)
                }
            })
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
                logE("Loading Chat Messages")
                loading = true
            }
            Status.SUCCESS -> {
                binding.pbCenter.visibility = View.GONE
                binding.pbHeader.visibility = View.GONE
                renderResponse(apiResponse.data as ChatMessagesResponse)
                logE("+${apiResponse.data}")
                loading = false
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
            emitNewMessage(
                "", AppConstants.AUDIO_MESSAGE, it.data?.file_url.toString(), 0
            )
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
                        initializeRecorder()
                    }

                    override fun onPermissionDenied() {
                        logE("onPermissionDenied")
                    }
                }).build().init()
            }, 100
        )
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
            getFileBody(output, "file")
        )
    }

    private fun sendVoiceMessage() {
        createChatObject(AppConstants.DUMMY_STRING, output, AppConstants.AUDIO_MESSAGE)
        uploadAudioMedia()
        logE("Audio Api hit successfully")
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
        createChatObject(
            binding.recordingView.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE
        )
        emitNewMessage(
            binding.recordingView.editTextMessage.text.toString(),
            AppConstants.TEXT_MESSAGE, "", 0
        )
        logE("Message Emitted to socket")
        binding.recordingView.editTextMessage.setText("")
    }

    override fun socketResponse(jsonObject: JSONObject) {
        val gson = Gson()
        val data = gson.fromJson(jsonObject.toString(), ChatData::class.java)
        if (data.chat_id == chatId) {
            runOnUiThread {
                addMessage(data)
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

    private fun addMessage(chatData: ChatData) {
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatData)
        binding.chatMessagesRecycler.scrollToPosition(0)
    }

    private fun emitNewMessage(message: String, type: String, file: String, duration: Int) {
        SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(), chatId.toString(), message, type,
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(), file, duration
        )
    }


    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}