package com.example.kalam_android.view.adapter

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.toast
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*


class ChatMessagesAdapter(val context: Context, val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var currentPos = -1
    private var prePos = -1
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private lateinit var mediaPlayer: MediaPlayer
    private var chatList: ArrayList<ChatData>? = null
    private val TAG = this.javaClass.simpleName
    var isOriginal = true

    fun updateList(list: ArrayList<ChatData>?) {
        chatList?.clear()
        chatList = list
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatData) {
        chatList?.add(message)
        chatList?.size?.let { notifyItemInserted(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chat_right,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as MyHolder
        val item = chatList?.get(position)
        when (item?.type) {
            AppConstants.TEXT_MESSAGE -> {
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.itemChat.tvMessage.text = item.message
                itemHolder.binding.itemChat.llOriginal.setOnClickListener {
                    if (isOriginal) {
                        isOriginal = false
                        itemHolder.binding.itemChat.tvMessage.text = item.original_message
                        itemHolder.binding.itemChat.tvOriginal.text = "Translated"
                    } else {
                        isOriginal = true
                        itemHolder.binding.itemChat.tvMessage.text = item.message
                        itemHolder.binding.itemChat.tvOriginal.text = "View Original"
                    }
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.END
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_send_message)
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_receive_message)
                }

            }
            AppConstants.AUDIO_MESSAGE -> {
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.audioPlayer.ivStop.setOnClickListener {
                    itemHolder.binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
                    itemHolder.binding.audioPlayer.seekBar.max = 0
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
                itemHolder.binding.audioPlayer.rlPlay.setOnClickListener {
                    currentPos = position
                    logE("Clicked")
                    playVoiceMsg(itemHolder.binding, item.file.toString())
                    toast(context, "Clicked")
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                    itemHolder.binding.audioPlayer.cvPlayer.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.sender_color
                        )
                    )
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                    itemHolder.binding.audioPlayer.cvPlayer.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.receiver_color
                        )
                    )
                }
            }
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun playVoiceMsg(binding: ItemChatRightBinding, voiceMessage: String) {
        logE("Progress Bar is visible")
        try {
//            if (currentPos != prePos) {
            binding.audioPlayer.ivPlayPause.visibility = View.GONE
            binding.audioPlayer.ivPlayProgress.visibility = View.VISIBLE
            prePos = currentPos
            mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            /* val audioAttributes = AudioAttributes.Builder()
             audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
             audioAttributes.setLegacyStreamType(AudioManager.STREAM_MUSIC)
             mediaPlayer.setAudioAttributes(audioAttributes.build())*/
//                binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
            mediaPlayer.setDataSource(voiceMessage)
//                initializeSeekBar(binding)
//                mediaPlayer.prepare()
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                logE("setOnPreparedListener is called")
                binding.audioPlayer.ivPlayPause.visibility = View.VISIBLE
                binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
                binding.audioPlayer.ivPlayProgress.visibility = View.GONE
                mediaPlayer.start()
                initializeSeekBar(binding)
                binding.audioPlayer.seekBar.max = seconds

            }
//                mediaPlayer.start()

//                binding.audioPlayer.seekBar.max = seconds
            logE("If section")
            /* } else {
                 logE("Else section")
                 if (!mediaPlayer.isPlaying) {
                     logE("Else if section")
                     binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
                     mediaPlayer.start()
                 } else {
                     logE("Else else section")
                     binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
                     mediaPlayer.pause()
                 }
             }*/

        } catch (e: IllegalStateException) {
            logE("exception:${e.message}")
        }


        mediaPlayer.setOnCompletionListener { mp ->

            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
            mp.stop()
            mp.release()
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

        runnable = Runnable {
            var currentSeconds = 0
            try {
                currentSeconds = mediaPlayer.currentPosition / 1000
                binding.audioPlayer.seekBar.progress = currentSeconds
                binding.audioPlayer.tvPlayerTime.text =
                    timeFormatter.format(Date((currentSeconds * 1000).toLong()))
//                val difference = seconds - currentSeconds
                handler.postDelayed(runnable, 1000)
            } catch (e: IllegalStateException) {

            }
        }
        handler.postDelayed(runnable, 1000)
    }

    private val seconds: Int
        get() {
            var seconds: Int = 0
            try {
                seconds = mediaPlayer.duration / 1000

            } catch (e: IllegalStateException) {
            }
            return seconds
        }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}