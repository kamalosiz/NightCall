package com.example.kalam_android.view.adapter

import android.annotation.SuppressLint
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
import com.example.kalam_android.repository.model.AudioUploadResponse
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.showAlertDialoge


class ChatMessagesAdapter(val context: Context, private val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var currentPos = -1
    private var prePos = -1
    private lateinit var mediaPlayer: MediaPlayer
    private var chatList: ArrayList<ChatData>? = ArrayList()
    private val TAG = this.javaClass.simpleName
    private var isRelease = false
    private var isUploaded = false

    fun updateList(list: ArrayList<ChatData>) {
        chatList?.addAll(list)
        notifyDataSetChanged()
    }

   /* fun updateAudioList(audioList: ArrayList<AudioUploadResponse>, position: Int) {
        this.audioList = audioList
        notifyItemChanged(position)
    }*/

    fun addMessage(message: ChatData) {
        chatList?.add(0, message)
        notifyItemInserted(0)
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as MyHolder
        val item = chatList?.get(position)
        when (item?.type) {
            AppConstants.TEXT_MESSAGE -> {
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.itemChat.tvMessage.text = item.message
                itemHolder.binding.itemChat.llOriginal.setOnClickListener {
                    showAlertDialoge(context, "Original Message", item.original_message.toString())
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.END
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                    itemHolder.binding.itemChat.tvTime.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_send_message)
                    itemHolder.binding.itemChat.view.setBackgroundResource(R.color.white)
                    itemHolder.binding.itemChat.tvOriginal.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_receive_message)
                    itemHolder.binding.itemChat.tvTime.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                    itemHolder.binding.itemChat.view.setBackgroundResource(R.color.black)
                    itemHolder.binding.itemChat.tvOriginal.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black
                        )
                    )
                }
            }
            AppConstants.AUDIO_MESSAGE -> {
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.audioPlayer.rlPlay.setOnClickListener {
                    currentPos = position
                    playVoiceMsg(itemHolder.binding, item.file.toString())
                }
                if (item.sender_id == userId.toInt()) {

                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                    itemHolder.binding.audioPlayer.cvPlayer.setBackgroundResource(R.drawable.audio_bubble_right)
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                    itemHolder.binding.audioPlayer.cvPlayer.setBackgroundResource(R.drawable.audio_bubble_left)
                }
            }
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun playVoiceMsg(binding: ItemChatRightBinding, voiceMessage: String) {
        try {
            if (isRelease || currentPos != prePos) {
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
            logE("exception:${e.message}")
        }

        mediaPlayer.setOnCompletionListener { mp ->
            binding.audioPlayer.ivPlayPause.setBackgroundResource(R.drawable.ic_play_audio)
            mp.stop()
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


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}