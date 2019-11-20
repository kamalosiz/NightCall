package com.example.kalam_android.util

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

object Global {
    private var isRelease = false
    private lateinit var mediaPlayer: MediaPlayer
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var prePos = -1

    fun playVoiceMsg(binding: ItemChatRightBinding, voiceMessage: String, currentPos: Int) {
        try {
            if (isRelease || currentPos != prePos) {
                Debugger.e("ChatMessagesAdapter","voiceMessage : $voiceMessage")
                Debugger.e("ChatMessagesAdapter","currentPos : $currentPos")
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
                }
            } else {
                if (!mediaPlayer.isPlaying) {
                    binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_pause_audio)
                    mediaPlayer.start()
                } else {
                    binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
                    mediaPlayer.pause()
                }
            }

        } catch (e: IllegalStateException) {
//            logE("exception:${e.message}")
        }

        mediaPlayer.setOnCompletionListener { mp ->
            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
            mp.stop()
            mp.reset()
            mp.release()
            isRelease = true
            binding.audioPlayer.seekBar.max = 0
        }

        binding.audioPlayer.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

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

    fun bitmapToFile(context: Context, bitmap: Bitmap?): File {
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
}