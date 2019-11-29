package com.example.kalam_android.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding

object Global {
    private var isRelease = true
    private var isPlayerRunning = false
    private var alreadyClicked = false
    var mediaPlayer: MediaPlayer = MediaPlayer()
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var prePos: Long = -111098

    val TAG = "ChatMessagesAdapter"

    fun playVoiceMsg(
        binding: ItemChatRightBinding,
        voiceMessage: String,
        currentPos: Long,
        context: Context
    ) {
        if (!alreadyClicked) {
            try {
                if (isRelease || currentPos != prePos) {
                    alreadyClicked = true
                    /*if (alreadyClicked) {
                        logE("alreadyClicked is true")
                        binding.audioPlayer.ivPlayPause.visibility = View.VISIBLE
                        binding.audioPlayer.ivPlayProgress.visibility = View.GONE
                        *//*mediaPlayer.stop()
                    mediaPlayer.reset()
                    mediaPlayer.release()*//*
                    isRelease = true
                }*/
                    if (isPlayerRunning) {
                        if (mediaPlayer.isPlaying) {
                            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                            logE("already playing")
                            mediaPlayer.stop()
                            alreadyClicked = false
                            mediaPlayer = MediaPlayer()
                        }
                    }

                    Debugger.e("ChatMessagesAdapter", "If voiceMessage : $voiceMessage")
                    Debugger.e("ChatMessagesAdapter", "If currentPos : $currentPos")
                    binding.audioPlayer.ivPlayPause.visibility = View.GONE
                    binding.audioPlayer.ivPlayProgress.visibility = View.VISIBLE
                    prePos = currentPos
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build()
                    )
                    isRelease = false
                    mediaPlayer.setDataSource(voiceMessage)
                    mediaPlayer.prepareAsync()
                    mediaPlayer.setOnPreparedListener {
                        binding.audioPlayer.ivPlayPause.visibility = View.VISIBLE
                        binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_pause_audio)
                        binding.audioPlayer.ivPlayProgress.visibility = View.GONE
                        mediaPlayer.start()
                        initializeSeekBar(binding)
                        alreadyClicked = false
                        isPlayerRunning = true
                    }
                } else {
                    Debugger.e("ChatMessagesAdapter", "Else voiceMessage : $voiceMessage")
                    Debugger.e("ChatMessagesAdapter", "Else currentPos : $currentPos")
                    if (!mediaPlayer.isPlaying) {
                        binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_pause_audio)
                        mediaPlayer.start()
                    } else {
                        binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                        mediaPlayer.pause()
                    }
                }

            } catch (e: IllegalStateException) {
                Debugger.e("ChatMessagesAdapter", "exception:${e.message}")
            }

            mediaPlayer.setOnCompletionListener { mp ->
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
            mediaPlayer.setOnErrorListener { mp, what, extra ->
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
                        mediaPlayer.seekTo(progress * 1000)
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

    private fun initializeSeekBar(binding: ItemChatRightBinding) {
        binding.audioPlayer.seekBar.max = mediaPlayer.duration
        runnable = Runnable {

            try {
                val currentSeconds = mediaPlayer.currentPosition
                currentSeconds.let {
                    binding.audioPlayer.seekBar.progress = it
                }
                handler.postDelayed(runnable, 1)
            } catch (e: IllegalStateException) {

            }
        }
        handler.postDelayed(runnable, 1)
    }

    fun setColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(
            context,
            color
        )
    }

    /*fun bitmapToFile(context: Context, bitmap: Bitmap?): File {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun getRealPath(context: Context, uri: Uri): String? {

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }*/

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}