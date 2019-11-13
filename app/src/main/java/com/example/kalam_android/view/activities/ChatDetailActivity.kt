package com.example.kalam_android.view.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.SeekBar
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
import com.example.kalam_android.repository.model.AudioUploadResponse
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
import com.example.kalam_android.wrapper.GlideDownloder
import com.example.kalam_android.wrapper.SocketIO
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_for_recoder.view.*
import omrecorder.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private var output: String = ""
    private val delay: Long = 1000
    private var lastTextEdit: Long = 0
    private var index = 0
    var handler = Handler()
    private var chatList1: ArrayList<ChatData> = ArrayList()
    private var profileImage: String = ""
    private var loading = false
    private var isFromChatFragment = false
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var recorder: Recorder? = null
    private var timeWhenPause: Long = 0;
    private var isResumeRecorder = false
    private var isRecording = false
    private var isPlayPlayer = false
    private var isStopPlayer = false
    private var isStopRecording = false
    private var isPlayerRelease = false
    private var isFileReady = false
    private var mediaPlayer: MediaPlayer? = null
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val playGreen = R.drawable.ic_play_green
    private val playGray = R.drawable.ic_play_gray
    private val pauseGreen = R.drawable.ic_pause_green
    private val pauseGray = R.drawable.ic_pause_gray
    private val stopGreen = R.drawable.ic_stop_green
    private val stopGray = R.drawable.ic_stop_gray
    private val recordGreen = R.drawable.ic_record_green
    private val recordGray = R.drawable.ic_record_gray
    private val recordRed = R.drawable.ic_record_red
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
        binding.lvRecoder.ivSend.setOnClickListener(this)
        binding.header.rlBack.setOnClickListener(this)
        checkSomeoneTyping()
        SocketIO.setListener(this)
        SocketIO.setTypingListener(this)
        clickListener()
        applyPagination()
    }

    private fun initMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.pause()
                    updateDrawables(recordGray, playGreen, stopGreen)
                    updateClickable(
                        canClickRecoder = false,
                        canClickPlay = true,
                        canClickStop = true
                    )
                } else {
                    mediaPlayer?.pause()
                    updateDrawables(recordGray, pauseGreen, stopGreen)
                    updateClickable(
                        canClickRecoder = false,
                        canClickPlay = true,
                        canClickStop = true
                    )
                    mediaPlayer?.start()
                }

            } else {
                updateDrawables(recordGray, pauseGreen, stopGreen)
                updateClickable(
                    canClickRecoder = false,
                    canClickPlay = true,
                    canClickStop = true
                )
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
        } catch (e: IOException) {
            logE("excep:${e.message}")
        }

        mediaPlayer?.setOnCompletionListener {

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayerRelease = true
            isStopPlayer = false
            binding.lvRecoder.lvForRecorder.tvTimer.text = "00:00"
            binding.lvRecoder.lvForRecorder.seekBar.max = 0
            updateClickable(
                canClickRecoder = false,
                canClickPlay = true,
                canClickStop = false
            )
            updateDrawables(recordGray, playGreen, stopGray)

        }

    }

    private fun clickListener() {
        binding.header.tvName.setOnClickListener {
            val intent = Intent(this@ChatDetailActivity, UserProfileActivity::class.java)
            intent.putExtra(AppConstants.CHAT_USER_NAME, userRealName)
            intent.putExtra(AppConstants.CHAT_USER_PICTURE, profileImage)
            startActivity(intent)
        }
        binding.lvRecoder.ivMic.setOnClickListener {
            initRecorderWithPermissions()
        }

        binding.lvRecoder.lvForRecorder.ivRecord.setOnClickListener {
            playPauseRecorder()
        }

        /*binding.lvRecoder.lvForRecorder.ivPause.setOnClickListener {

            if (isPlayPlayer) {
                isPlayPlayer = false
                pausePlayer()

            } else {
                pauseRecorder()
            }
        }*/

        binding.lvRecoder.lvForRecorder.ivStop.setOnClickListener {

            if (isStopPlayer) {
                stopPlayer()
            } else {
                stopRecord()
            }

        }

        binding.lvRecoder.lvForRecorder.ivPlay.setOnClickListener {
            if (isRecording) {

                pauseRecorder()

            } else {
                playAudio()
            }
        }

        binding.lvRecoder.lvForRecorder.ivCancel.setOnClickListener {
            cancel(true)
        }
    }

    private fun playPauseRecorder() {

        if (!isResumeRecorder) {
            startChronometer()
            initializeRecorder()
            recorder?.startRecording()
            updateDrawables(recordGray, pauseGreen, stopGreen)
            updateClickable(
                canClickRecoder = false,
                canClickPlay = true,
                canClickStop = true
            )
            isRecording = true
            isStopRecording = true
        } else {
            resumeChronometer()
            updateDrawables(recordGray, pauseGreen, stopGreen)
            updateClickable(
                canClickRecoder = false,
                canClickPlay = true,
                canClickStop = true
            )
            recorder?.resumeRecording()
            isStopRecording = true
        }
    }

    private fun pauseRecorder() {
        pauseChronometer()
        updateDrawables(recordRed, pauseGray, stopGreen)
        updateClickable(
            canClickRecoder = true,
            canClickPlay = false,
            canClickStop = true
        )
        recorder?.pauseRecording()
        isResumeRecorder = true
    }


    private fun stopPlayer() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlayerRelease = true
        isStopPlayer = false
        binding.lvRecoder.lvForRecorder.seekBar.max = 0
        updateDrawables(recordGray, playGreen, stopGray)
        updateClickable(
            canClickRecoder = false,
            canClickPlay = true,
            canClickStop = false
        )
        binding.lvRecoder.lvForRecorder.tvTimer.text = "00:00"

    }

    private fun stopRecord() {
        if (isStopRecording) {
            isFileReady = true
            recorder?.stopRecording()
            recorder = null
            stopChronometer()
            visibility(visible, visible, gone)
            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                canClickRecoder = false,
                canClickPlay = true,
                canClickStop = false
            )
            isStopRecording = false
            isRecording = false
        } else {
            stopPlayer()
            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                canClickRecoder = true,
                canClickPlay = false,
                canClickStop = false
            )
            isResumeRecorder = true
        }
    }

    private fun playAudio() {

        isPlayPlayer = true
        isStopPlayer = true
        initMediaPlayer()

    }


    private fun cancel(deleteFile: Boolean) {
        isFileReady = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        recorder = null
        timeWhenPause = 0
        isResumeRecorder = false
        isPlayPlayer = false
        isStopPlayer = false
        isStopRecording = false
        isPlayerRelease = false
        stopChronometer()
        binding.lvRecoder.lvForRecorder.seekBar.max = 0
        binding.lvRecoder.lvForRecorder.tvTotalTime.text = "00:00"
        updateDrawables(recordGreen, playGray, stopGray)
        updateClickable(
            canClickRecoder = true,
            canClickPlay = false,
            canClickStop = false
        )
        visibility(gone, gone, visible)
        if (deleteFile) {
            val file = File(output)
            if (file.exists()) {
                file.delete()
            }
        }
        output = ""
    }

    private fun visibility(recorderVisibility: Int, seekbarVisibility: Int, chronometer: Int) {

        binding.lvRecoder.lvForRecorder.visibility = recorderVisibility
        binding.lvRecoder.lvForRecorder.lvSeekBar.visibility = seekbarVisibility
        binding.lvRecoder.chronometer.visibility = chronometer
    }

    private fun updateDrawables(
        recordButton: Int, playButton: Int, stopButton: Int
    ) {
        binding.lvRecoder.lvForRecorder.ivRecord.setImageResource(recordButton)
        binding.lvRecoder.lvForRecorder.ivPlay.setImageResource(playButton)
        binding.lvRecoder.lvForRecorder.ivStop.setImageResource(stopButton)
    }

    private fun updateClickable(
        canClickRecoder: Boolean,
        canClickPlay: Boolean,
        canClickStop: Boolean
    ) {
        binding.lvRecoder.lvForRecorder.ivRecord.isClickable = canClickRecoder
        binding.lvRecoder.lvForRecorder.ivPlay.isClickable = canClickPlay
        binding.lvRecoder.lvForRecorder.ivStop.isClickable = canClickStop
    }

    private fun startChronometer() {

        binding.lvRecoder.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
        binding.lvRecoder.lvForRecorder.chronometer.start()
    }

    private fun stopChronometer() {

        binding.lvRecoder.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
        binding.lvRecoder.lvForRecorder.chronometer.stop()
    }

    private fun pauseChronometer() {

        timeWhenPause =
            binding.lvRecoder.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime()
        binding.lvRecoder.lvForRecorder.chronometer.stop()

    }

    private fun resumeChronometer() {

        binding.lvRecoder.lvForRecorder.chronometer.base =
            SystemClock.elapsedRealtime() + timeWhenPause
        binding.lvRecoder.lvForRecorder.chronometer.start()

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
                Debugger.e(TAG, "ID $chatId")
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
            emitNewMessageToSocket(
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
                        binding.lvRecoder.lvForRecorder.visibility = View.VISIBLE
                    }

                    override fun onPermissionDenied() {
                        logE("onPermissionDenied")
                    }
                }).build().init()
            }, 100
        )
    }

    private fun uploadAudioMedia() {
        viewModel.hitUploadAudioApi(
            sharedPrefsHelper.getUser()?.token,
            getFileBody(output, "file")
        )
    }

    private fun sendVoiceMessage() {
        logE("Output Message $output")
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
        binding.lvRecoder.editTextMessage.addTextChangedListener(object : TextWatcher {
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
            binding.lvRecoder.editTextMessage.text.toString(), AppConstants.DUMMY_STRING,
            AppConstants.TEXT_MESSAGE
        )
        emitNewMessageToSocket(
            binding.lvRecoder.editTextMessage.text.toString(),
            AppConstants.TEXT_MESSAGE, "", 0
        )
        logE("Message Emitted to socket")
        binding.lvRecoder.editTextMessage.setText("")
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

    private fun createChatObject(message: String, file: String, type: String) {
        addMessage(
            ChatData(
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                ).toString(),
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
                , ""
            )
        )
    }

    private fun addMessage(chatData: ChatData) {
        (binding.chatMessagesRecycler.adapter as ChatMessagesAdapter).addMessage(chatData)
        binding.chatMessagesRecycler.scrollToPosition(0)
    }

    private fun emitNewMessageToSocket(message: String, type: String, file: String, duration: Int) {
        SocketIO.emitNewMessage(
            sharedPrefsHelper.getUser()?.id.toString(), chatId.toString(), message, type,
            sharedPrefsHelper.getUser()?.firstname.toString()
                    + " " + sharedPrefsHelper.getUser()?.lastname.toString(), file, duration
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSend -> {
                if (output.isEmpty() && binding.lvRecoder.editTextMessage.text.toString().isNotEmpty()) {
                    sendMessage()
                    logE("Text Message")
                } else {
                    if (isFileReady) {
                        logE("Audio Message")
                        sendVoiceMessage()
                        binding.lvRecoder.lvForRecorder.visibility = View.GONE
                        cancel(false)
                    }
                }
            }
            R.id.rlBack -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}