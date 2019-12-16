package com.example.kalam_android.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityChatDetailBinding
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import com.example.kalam_android.view.activities.GalleryPostActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
import kotlinx.android.synthetic.main.layout_for_attachment.view.*
import kotlinx.android.synthetic.main.layout_for_recoder.view.*
import omrecorder.*
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MyChatMediaHelper(
    val context: AppCompatActivity,
    val binding: ActivityChatDetailBinding
) : View.OnClickListener {
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
    var mediaPlayer: MediaPlayer? = null
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val playGreen = R.drawable.ic_play_green
    private val playGray = R.drawable.ic_play_gray
    private val pauseGreen = R.drawable.ic_pause_green
    private val pauseGray = R.drawable.ic_pause_gray
    private val stopGreen = R.drawable.ic_stop_green
    private val stopGray = R.drawable.ic_stop_gray
    private val recordGreen = R.drawable.ic_record_red
    private val recordGray = R.drawable.ic_record_gray
    private val recordRed = R.drawable.ic_record_red
    private lateinit var runnable: Runnable
    private var totalDuration: Long = 0
    private var isAttachmentOpen = true


    @SuppressLint("CheckResult")
    fun initRecorderWithPermissions() {
        isAttachmentOpen = true
        initRecorderListener()
        logE("initRecorderWithPermissions")
        Dexter.withActivity(context).withPermissions(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    binding.lvBottomChat.lvForAttachment.visibility = View.GONE
                    binding.lvBottomChat.lvForRecorder.visibility = View.VISIBLE
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

    private fun initRecorderListener() {
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
        return (totalDuration / 1000) % 60
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

    fun openAttachments() {
        initAttachmentListeners()
        if (isAttachmentOpen) {
            isAttachmentOpen = false
            binding.lvBottomChat.lvForRecorder.visibility = View.GONE
            binding.lvBottomChat.lvForAttachment.visibility = View.VISIBLE
            logE("Attachment should open")
        } else {
            logE("Attachment not open")
            isAttachmentOpen = true
            binding.lvBottomChat.lvForAttachment.visibility = View.GONE
        }
    }

    private fun initAttachmentListeners() {
        binding.lvBottomChat.lvForAttachment.llGallery.setOnClickListener(this)
    }

    fun hideAttachments() {
        isAttachmentOpen = true
        binding.lvBottomChat.lvForAttachment.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
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
            R.id.llGallery -> {
                context.startActivityForResult(
                    Intent(
                        context,
                        GalleryPostActivity::class.java
                    ), AppConstants.SELECTED_IMAGES
                )


            }
        }
    }

    var isRelease = true
    var isPlayerRunning = false
    var alreadyClicked = false
    var prePos: Long = -111098
    var myPlayer: MediaPlayer? = null

    fun playVoiceMsg(
        binding: ItemChatRightBinding,
        voiceMessage: String,
        currentPos: Long,
        context: Context,
        unixTime: Double,
        language: String
    ) {
        if (!File(context.getExternalFilesDir(null)?.absolutePath + "/" + language + "$unixTime.mp3").exists() && voiceMessage.contains("https://")) {
            DownloadAudioFileFromURL(context, binding, unixTime, language).execute(voiceMessage)

        } else {
            if (!alreadyClicked) {
                try {
                    if (isRelease || currentPos != prePos) {
                        alreadyClicked = true
                        if (isPlayerRunning) {
                            if (myPlayer?.isPlaying == true) {
                                binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                                logE("already playing")
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
                            initializeSeekBar(binding)
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
                    logE("on Error Called")
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

    private fun initializeSeekBar(binding: ItemChatRightBinding) {
        binding.audioPlayer.seekBar.max = myPlayer?.duration!!
        runnable = Runnable {
            try {
                val currentSeconds = myPlayer?.currentPosition
                currentSeconds.let {
                    if (it != null) {
                        binding.audioPlayer.seekBar.progress = it
                    }
                }
                handler.postDelayed(runnable, 1)
            } catch (e: IllegalStateException) {

            }
        }
        handler.postDelayed(runnable, 1)
    }

    internal class DownloadAudioFileFromURL(
        val context: Context,
        val binding: ItemChatRightBinding
        , val unixTime: Double, val language: String
    ) :
        AsyncTask<String?, String?, String?>() {
        private val TAG = this.javaClass.simpleName
        override fun doInBackground(vararg f_url: String?): String? {
            var count: Int = 0
            try {
                val url = URL(f_url[0])
                val connection: URLConnection = url.openConnection()
                connection.connect()
                val lenghtOfFile: Int = connection.contentLength
                // download the file
                val input: InputStream = BufferedInputStream(
                    url.openStream(),
                    8192
                )
                val path =
                    context.getExternalFilesDir(null)?.absolutePath + "/" + language + "$unixTime.mp3"
                val output: OutputStream = FileOutputStream(path)
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                    output.write(data, 0, count)
                }
                // flushing output
                output.flush()
                // closing streams
                output.close()
                input.close()
            } catch (e: Exception) {
                logE("Error: ${e.message}")
            }
            return null
        }


        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            binding.audioPlayer.ivPlayProgress.progress = values[0]!!.toInt()

        }

        override fun onPreExecute() {
            super.onPreExecute()
            binding.audioPlayer.ivPlayProgress.visibility = View.VISIBLE
            binding.audioPlayer.ivPlayPause.visibility = View.GONE
        }

        override fun onPostExecute(file_url: String?) {
            binding.audioPlayer.ivPlayProgress.visibility = View.GONE
            binding.audioPlayer.ivPlayPause.visibility = View.VISIBLE
        }

        private fun logE(msg: String) {
            Debugger.e(TAG, msg)
        }
    }

    private fun getAudio(): Array<File>? {
        var fileOutput: String? = null
        var dirFiles: Array<File>? = null
        val fileDirectory =
            File(context.getExternalFilesDir(null)?.absolutePath)
        if (fileDirectory.exists()) {
            dirFiles = fileDirectory.listFiles()

            /*if (dirFiles.isNotEmpty()) { // loops through the array of files, outputing the name to console
                for (ii in dirFiles.indices) {
                    if (dirFiles[ii].absolutePath.contains(unixTime.toString())){
                        fileOutput = dirFiles[ii].toString()

                    }
                }
            }*/
        }
        return dirFiles
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

}