package com.example.kalam_android.view.activities

import android.Manifest
import android.content.Context
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityVoiceRecordingBinding
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.helper.MyChatMediaHelper
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_for_recoder.view.*
import omrecorder.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class VoiceRecordingActivity : AppCompatActivity() {
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
    var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable: Runnable
    var isRelease = true
    var isPlayerRunning = false
    var alreadyClicked = false
    var prePos: Long = -111098
    var myPlayer: MediaPlayer? = null
    private var totalDuration: Long = 0

    private lateinit var binding: ActivityVoiceRecordingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_voice_recording)
    }

    private fun initRecorderWithPermissions() {
        Debugger.e("initRecorderWithPermissions","")
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {

                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).check()
    }

    private fun playPauseRecorder() {

        if (!isResumeRecorder) {
            startChronometer()
            initializeRecorder()
            recorder?.startRecording()
//            updateDrawables(recordGray, pauseGreen, stopGreen)
            updateClickable(
                    canClickRecorder = false,
                    canClickPlay = true,
                    canClickStop = true
            )
            isRecording = true
            isStopRecording = true
        } else {
            resumeChronometer()
//            updateDrawables(recordGray, pauseGreen, stopGreen)
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

//        binding.lvBottomChat.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
//        binding.lvBottomChat.lvForRecorder.chronometer.start()
    }

    private fun stopChronometer() {

//        binding.lvBottomChat.lvForRecorder.chronometer.base = SystemClock.elapsedRealtime()
//        binding.lvBottomChat.lvForRecorder.chronometer.stop()
    }

    private fun pauseChronometer() {

//        timeWhenPause =
//                binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime()
//        binding.lvBottomChat.lvForRecorder.chronometer.stop()

    }

    private fun resumeChronometer() {

//        binding.lvBottomChat.lvForRecorder.chronometer.base =
//                SystemClock.elapsedRealtime() + timeWhenPause
//        binding.lvBottomChat.lvForRecorder.chronometer.start()

    }

    private fun updateDrawables(
            recordButton: Int, playButton: Int, stopButton: Int
    ) {
//        binding.lvBottomChat.lvForRecorder.ivRecord.setImageResource(recordButton)
//        binding.lvBottomChat.lvForRecorder.ivPlay.setImageResource(playButton)
//        binding.lvBottomChat.lvForRecorder.ivStop.setImageResource(stopButton)
    }

    private fun updateClickable(
            canClickRecorder: Boolean,
            canClickPlay: Boolean,
            canClickStop: Boolean
    ) {
//        binding.lvBottomChat.lvForRecorder.ivRecord.isClickable = canClickRecorder
//        binding.lvBottomChat.lvForRecorder.ivPlay.isClickable = canClickPlay
//        binding.lvBottomChat.lvForRecorder.ivStop.isClickable = canClickStop
    }

    private fun pauseRecorder() {
        pauseChronometer()
//        updateDrawables(recordRed, pauseGray, stopGreen)
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
//                    updateDrawables(recordGray, playGreen, stopGreen)
                    updateClickable(
                            canClickRecorder = false,
                            canClickPlay = true,
                            canClickStop = true
                    )
                } else {
                    mediaPlayer?.pause()
//                    updateDrawables(recordGray, pauseGreen, stopGreen)
                    updateClickable(
                            canClickRecorder = false,
                            canClickPlay = true,
                            canClickStop = true
                    )
                    mediaPlayer?.start()
                }

            } else {
//                updateDrawables(recordGray, pauseGreen, stopGreen)
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
            Debugger.e("excep:","${e.message}")
        }

        mediaPlayer?.setOnCompletionListener {

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayerRelease = true
            isStopPlayer = false
//            binding.lvBottomChat.lvForRecorder.tvTimer.text = "00:00"
//            binding.lvBottomChat.lvForRecorder.seekBar.max = 0
            updateClickable(
                    canClickRecorder = false,
                    canClickPlay = true,
                    canClickStop = false
            )
//            updateDrawables(recordGray, playGreen, stopGray)

        }
    }

    private fun initializeSeekBar() {

//        binding.lvBottomChat.lvForRecorder.seekBar.max = mediaPlayer?.duration!!
//        binding.lvBottomChat.lvForRecorder.tvTotalTime.text =
//                timeFormatter.format(mediaPlayer?.duration!!)
        runnable = Runnable {
            try {
                if (mediaPlayer != null) {
                    val currentSeconds = mediaPlayer?.currentPosition
                    currentSeconds?.let {
//                        binding.lvBottomChat.lvForRecorder.seekBar.progress = it
                    }

//                    binding.lvBottomChat.lvForRecorder.tvTimer.text =
//                            timeFormatter.format(currentSeconds)
                    handler.postDelayed(runnable, 1)
                }
            } catch (e: IllegalStateException) {

            }

        }
        handler.postDelayed(runnable, 1)

        /*binding.lvBottomChat.lvForRecorder.seekBar.setOnSeekBarChangeListener(object :
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
        })*/
    }
    fun playVoiceMsg(
            binding: ItemChatRightBinding,
            voiceMessage: String,
            currentPos: Long,
            context: Context,
            unixTime: Double,
            language: String
    ) {
        if (!File(context.getExternalFilesDir(null)?.absolutePath + "/" + language + "$unixTime.mp3").exists() && voiceMessage.contains("https://")) {
            MyChatMediaHelper.DownloadAudioFileFromURL(context, binding, unixTime, language).execute(voiceMessage)

        } else {
            if (!alreadyClicked) {
                try {
                    if (isRelease || currentPos != prePos) {
                        alreadyClicked = true
                        if (isPlayerRunning) {
                            if (myPlayer?.isPlaying == true) {
                                binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
//                                logE("already playing")
                                myPlayer?.stop()
                                alreadyClicked = false
                                myPlayer = MediaPlayer()
                            }
                        }

                        Debugger.e("ChatMessagesAdapter", "If voiceMessage : $voiceMessage")
                        Debugger.e("ChatMessagesAdapter", "If currentPos : $currentPos")
                        binding.audioPlayer.ivPlayPause.visibility = View.GONE
                        binding.audioPlayer.ivPlayProgress.visibility = View.VISIBLE
                        prePos = currentPos
                        myPlayer = MediaPlayer()
                        myPlayer?.setAudioAttributes(
                                AudioAttributes
                                        .Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                        .build()
                        )
                        isRelease = false
                        if (voiceMessage.contains("https://")) {
                            val path =
                                    File(context.getExternalFilesDir(null)?.absolutePath + "/" + language + "$unixTime.mp3").absolutePath
                            myPlayer?.setDataSource(path)
                        } else {
                            myPlayer?.setDataSource(voiceMessage)
                        }

//                        myPlayer?.setDataSource(path)
                        myPlayer?.prepareAsync()
                        myPlayer?.setOnPreparedListener {
                            binding.audioPlayer.ivPlayPause.visibility = View.VISIBLE
                            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_pause_audio)
                            binding.audioPlayer.ivPlayProgress.visibility = View.GONE
                            myPlayer?.start()
                            initializeSeekBar()
                            alreadyClicked = false
                            isPlayerRunning = true
                        }
                    } else {
                        Debugger.e("ChatMessagesAdapter", "Else voiceMessage : $voiceMessage")
                        Debugger.e("ChatMessagesAdapter", "Else currentPos : $currentPos")
                        if (myPlayer?.isPlaying == false) {
                            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_pause_audio)
                            myPlayer?.start()
                        } else {
                            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                            myPlayer?.pause()
                        }
                    }

                } catch (e: IllegalStateException) {
                    Debugger.e("ChatMessagesAdapter", "exception:${e.message}")
                }

                myPlayer?.setOnCompletionListener { mp ->
                    binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                    mp.stop()
                    mp.reset()
                    mp.release()
                    isRelease = true
                    alreadyClicked = false
                    isPlayerRunning = false
                    Debugger.e("ChatMessagesAdapter", "setOnCompletionListener")
                    binding.audioPlayer.seekBar.max = 0
                }
                myPlayer?.setOnErrorListener { mp, what, extra ->
//                    logE("on Error Called")
                    true
                }

                binding.audioPlayer.seekBar.setOnSeekBarChangeListener(object :
                        SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {
                        if (fromUser) {
                            myPlayer?.seekTo(progress * 1000)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                })

            } else {
                toast(context, "Processing previous media...")
            }
        }


    }
    private fun stopPlayer() {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlayerRelease = true
        isStopPlayer = false
//        binding.lvBottomChat.lvForRecorder.seekBar.max = 0
//        updateDrawables(recordGray, playGreen, stopGray)
        updateClickable(
                canClickRecorder = false,
                canClickPlay = true,
                canClickStop = false
        )
//        binding.lvBottomChat.lvForRecorder.tvTimer.text = "00:00"

    }
    private fun stopRecord() {
        if (isStopRecording) {
            isFileReady = true
            recorder?.stopRecording()
            recorder = null
//            logE("Recorder Time : ${binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime()}")
//            totalDuration =
//                    abs(binding.lvBottomChat.lvForRecorder.chronometer.base - SystemClock.elapsedRealtime())
//            logE("Recorder Time totalDuration $totalDuration")
//            visibility(visible, visible, gone)
//            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                    canClickRecorder = false,
                    canClickPlay = true,
                    canClickStop = false
            )
            isStopRecording = false
            isRecording = false
        } else {
//            stopPlayer()
//            updateDrawables(recordGray, playGreen, stopGray)
            updateClickable(
                    canClickRecorder = true,
                    canClickPlay = false,
                    canClickStop = false
            )
            isResumeRecorder = true
        }
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


}
