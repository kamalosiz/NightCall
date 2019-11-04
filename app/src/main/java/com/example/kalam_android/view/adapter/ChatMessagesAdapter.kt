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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.Debugger
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChatMessagesAdapter(val context: Context, val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var pause: Boolean = false
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private lateinit var mediaPlayer: MediaPlayer
    private var chatList: ArrayList<ChatData>? = null
    private val TAG = this.javaClass.simpleName
    var isOriginal = true
    /*private val onClickListener: View.OnClickListener

    init {
        onClickListener = AdapterClickListener()
    }*/

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
            "text" -> {
                itemHolder.binding.cvPlayer.visibility = View.GONE
                itemHolder.binding.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.tvMessage.text = item.message
                itemHolder.binding.llOriginal.setOnClickListener {
                    if (isOriginal) {
                        isOriginal = false
                        itemHolder.binding.tvMessage.text = item.original_message
                        itemHolder.binding.tvOriginal.text = "Translated"
                    } else {
                        isOriginal = true
                        itemHolder.binding.tvMessage.text = item.message
                        itemHolder.binding.tvOriginal.text = "View Original"
                    }
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.rlMessage.gravity = Gravity.END
                    itemHolder.binding.ivMessage.setBackgroundResource(R.drawable.icon_send_message)
                } else {
                    itemHolder.binding.rlMessage.gravity = Gravity.START
                    itemHolder.binding.ivMessage.setBackgroundResource(R.drawable.icon_receive_message)
                }

            }
            "audio" -> {
                itemHolder.binding.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.rlMessage.visibility = View.GONE
                itemHolder.binding.ivPlayPause.setOnClickListener {
                    playVoiceMsg(itemHolder.binding, item.message.toString())
                }
            }

        }
        /* itemHolder.binding.ivPlayPause.setOnClickListener {
             playVoiceMsg(itemHolder.binding, item?.message.toString())
         }*/

    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun playVoiceMsg(binding: ItemChatRightBinding, voiceMessage: String) {
        try {
            mediaPlayer = MediaPlayer()
            val audioAttributes = AudioAttributes.Builder()
            audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            audioAttributes.setLegacyStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setAudioAttributes(audioAttributes.build())
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")

            if (!pause) {
                val file = File(voiceMessage)
                val fd = FileInputStream(file)
                binding.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
                mediaPlayer.setDataSource(fd.fd)
                mediaPlayer.prepare()

                mediaPlayer.start()
                binding.seekBar.max = seconds
                initializeSeekBar(binding)
                pause = true
            } else {
                binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
                mediaPlayer.pause()
                binding.seekBar.max = 0
                pause = false
            }
        } catch (e: IOException) {
            logE("exception:${e.message}")
        }

        mediaPlayer.setOnCompletionListener { mp ->
            if (seconds == currentSeconds) {
                binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
                pause = false
                mediaPlayer.release()
                binding.seekBar.max = 0
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress / 1000)
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
                binding.seekBar.progress = currentSeconds
                binding.tvPlayerTime.text =
                    timeFormatter.format(Date((currentSeconds * 1000).toLong()))
                val difference = seconds - currentSeconds
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
    private val currentSeconds: Int
        get() {
            var currentSeconds: Int = 0
            try {
                currentSeconds = mediaPlayer.currentPosition / 1000
            } catch (e: IllegalStateException) {

            }
            return currentSeconds
        }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}