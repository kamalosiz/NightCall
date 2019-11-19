package com.example.kalam_android.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.*
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.util.Debugger
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_for_recoder.view.*
import omrecorder.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

private var instance: MyMediaRecorder? = null

class MyMediaRecorder private constructor(
    val context: Context,
    val binding: ActivityChatDetailBinding
) :
    View.OnClickListener {
    private val TAG = this.javaClass.simpleName
    private lateinit var path: String
    private var output: String = ""
    var handler = Handler()
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var recorder: Recorder? = null
    private var timeWhenPause: Long = 0
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
    private var totalDuration: Long = 0

    companion object {
        @Synchronized
        fun getInstance(context: Context, binding: ActivityChatDetailBinding): MyMediaRecorder? {
            if (instance == null) {
                instance = MyMediaRecorder(context, binding)
            }
            return instance
        }
    }

    @SuppressLint("CheckResult")
    fun initRecorderWithPermissions() {
        RxPermissions(context as AppCompatActivity)
            .request(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    binding.lvBottomChat.lvForRecorder.visibility = View.VISIBLE
                    initListeners()
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }
    }

    private fun initListeners() {
        binding.lvBottomChat.ivMic.setOnClickListener(this)
        binding.lvBottomChat.lvForRecorder.ivRecord.setOnClickListener(this)
        binding.lvBottomChat.lvForRecorder.ivStop.setOnClickListener(this)
        binding.lvBottomChat.lvForRecorder.ivPlay.setOnClickListener(this)
        binding.lvBottomChat.lvForRecorder.ivCancel.setOnClickListener(this)
    }

    fun cancel(deleteFile: Boolean) {
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
        binding.lvBottomChat.lvForRecorder.seekBar.max = 0
        binding.lvBottomChat.lvForRecorder.tvTotalTime.text = "00:00"
        updateDrawables(recordGreen, playGray, stopGray)
        updateClickable(
            canClickRecorder = true,
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

    private fun playPauseRecorder() {

        if (!isResumeRecorder) {
            startChronometer()
            initializeRecorder()
            recorder?.startRecording()
            updateDrawables(recordGray, pauseGreen, stopGreen)
            updateClickable(
                canClickRecorder = false,
                canClickPlay = true,
                canClickStop = true
            )
            isRecording = true
            isStopRecording = true
        } else {
            resumeChronometer()
            updateDrawables(recordGray, pauseGreen, stopGreen)
            updateClickable(
                canClickRecorder = false,
                canClickPlay = true,
                canClickStop = true
            )
            recorder?.resumeRecording()
            isStopRecording = true
        }
    }

    private fun startChronometer() {

        binding.lvBottomChat.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
        binding.lvBottomChat.lvForRecorder.chronometer.start()
    }

    private fun stopChronometer() {

        binding.lvBottomChat.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
        binding.lvBottomChat.lvForRecorder.chronometer.stop()
    }

    private fun pauseChronometer() {

        timeWhenPause =
            binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime()
        binding.lvBottomChat.lvForRecorder.chronometer.stop()

    }

    private fun resumeChronometer() {

        binding.lvBottomChat.lvForRecorder.chronometer.base =
            SystemClock.elapsedRealtime() + timeWhenPause
        binding.lvBottomChat.lvForRecorder.chronometer.start()

    }

    private fun initializeRecorder() {
        path = "/recording_${System.currentTimeMillis()}.wav"
        output = context.getExternalFilesDir(null)?.absolutePath + path
        recorder = OmRecorder.wav(
            PullTransport.Default(mic()), file()
        )
    }

    fun fileOutput(): String {
        return output
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

    private fun updateDrawables(
        recordButton: Int, playButton: Int, stopButton: Int
    ) {
        binding.lvBottomChat.lvForRecorder.ivRecord.setImageResource(recordButton)
        binding.lvBottomChat.lvForRecorder.ivPlay.setImageResource(playButton)
        binding.lvBottomChat.lvForRecorder.ivStop.setImageResource(stopButton)
    }

    private fun updateClickable(
        canClickRecorder: Boolean,
        canClickPlay: Boolean,
        canClickStop: Boolean
    ) {
        binding.lvBottomChat.lvForRecorder.ivRecord.isClickable = canClickRecorder
        binding.lvBottomChat.lvForRecorder.ivPlay.isClickable = canClickPlay
        binding.lvBottomChat.lvForRecorder.ivStop.isClickable = canClickStop
    }

    private fun stopPlayer() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlayerRelease = true
        isStopPlayer = false
        binding.lvBottomChat.lvForRecorder.seekBar.max = 0
        updateDrawables(recordGray, playGreen, stopGray)
        updateClickable(
            canClickRecorder = false,
            canClickPlay = true,
            canClickStop = false
        )
        binding.lvBottomChat.lvForRecorder.tvTimer.text = "00:00"

    }

    fun isFileReady(): Boolean {
        return isFileReady
    }

    private fun stopRecord() {
        if (isStopRecording) {
            isFileReady = true
            recorder?.stopRecording()
            recorder = null
            logE("Recorder Time : ${binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime()}")
            totalDuration =
                abs(binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime())
            logE("Recorder Time totalDuration $totalDuration")
            visibility(visible, visible, gone)
            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                canClickRecorder = false,
                canClickPlay = true,
                canClickStop = false
            )
            isStopRecording = false
            isRecording = false
        } else {
            stopPlayer()
            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                canClickRecorder = true,
                canClickPlay = false,
                canClickStop = false
            )
            isResumeRecorder = true
        }
    }

    fun getTotalDuration(): Long {
        return totalDuration
    }

    fun hideRecorder() {
        binding.lvBottomChat.lvForRecorder.visibility = View.GONE
    }

    private fun visibility(recorderVisibility: Int, seekbarVisibility: Int, chronometer: Int) {

        binding.lvBottomChat.lvForRecorder.visibility = recorderVisibility
        binding.lvBottomChat.lvForRecorder.lvSeekBar.visibility = seekbarVisibility
        binding.lvBottomChat.chronometer.visibility = chronometer
    }

    private fun pauseRecorder() {
        pauseChronometer()
        updateDrawables(recordRed, pauseGray, stopGreen)
        updateClickable(
            canClickRecorder = true,
            canClickPlay = false,
            canClickStop = true
        )
        recorder?.pauseRecording()
        isResumeRecorder = true
    }

    private fun playAudio() {

        isPlayPlayer = true
        isStopPlayer = true
        initMediaPlayer()

    }

    private fun initMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.pause()
                    updateDrawables(recordGray, playGreen, stopGreen)
                    updateClickable(
                        canClickRecorder = false,
                        canClickPlay = true,
                        canClickStop = true
                    )
                } else {
                    mediaPlayer?.pause()
                    updateDrawables(recordGray, pauseGreen, stopGreen)
                    updateClickable(
                        canClickRecorder = false,
                        canClickPlay = true,
                        canClickStop = true
                    )
                    mediaPlayer?.start()
                }

            } else {
                updateDrawables(recordGray, pauseGreen, stopGreen)
                updateClickable(
                    canClickRecorder = false,
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
            binding.lvBottomChat.lvForRecorder.tvTimer.text = "00:00"
            binding.lvBottomChat.lvForRecorder.seekBar.max = 0
            updateClickable(
                canClickRecorder = false,
                canClickPlay = true,
                canClickStop = false
            )
            updateDrawables(recordGray, playGreen, stopGray)

        }
    }

    private fun initializeSeekBar() {

        binding.lvBottomChat.lvForRecorder.seekBar.max = mediaPlayer?.duration!!
        binding.lvBottomChat.lvForRecorder.tvTotalTime.text =
            timeFormatter.format(mediaPlayer?.duration!!)
        runnable = Runnable {
            try {
                if (mediaPlayer != null) {
                    val currentSeconds = mediaPlayer?.currentPosition
                    currentSeconds?.let {
                        binding.lvBottomChat.lvForRecorder.seekBar.progress = it
                    }

                    binding.lvBottomChat.lvForRecorder.tvTimer.text =
                        timeFormatter.format(currentSeconds)
                    handler.postDelayed(runnable, 1)
                }
            } catch (e: IllegalStateException) {

            }

        }
        handler.postDelayed(runnable, 1)

        binding.lvBottomChat.lvForRecorder.seekBar.setOnSeekBarChangeListener(object :
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivMic -> {
                initRecorderWithPermissions()
            }
            R.id.ivRecord -> {
                playPauseRecorder()
            }
            R.id.ivStop -> {
                if (isStopPlayer) {
                    stopPlayer()
                } else {
                    stopRecord()
                }
            }
            R.id.ivPlay -> {
                if (isRecording) {
                    pauseRecorder()
                } else {
                    playAudio()
                }
            }
            R.id.ivCancel -> {
                cancel(true)
            }
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

}