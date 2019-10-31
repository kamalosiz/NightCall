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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.Debugger
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatMessagesAdapter(val context: Context, private val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var pause: Boolean = false
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
    private lateinit var mediaPlayer: MediaPlayer
    private var chatList: ArrayList<ChatData>? = null
    private val TAG = this.javaClass.simpleName


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
        mediaPlayer = MediaPlayer()
        val audioAttributes = AudioAttributes.Builder()
        audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        audioAttributes.setLegacyStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setAudioAttributes(audioAttributes.build())
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
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
        itemHolder.binding.tvMessage.text = item?.message
        if (item?.sender_id == userId.toInt()) {
            itemHolder.binding.rlMessage.gravity = Gravity.END
        } else {
            itemHolder.binding.rlMessage.gravity = Gravity.START
        }
        when (item?.type) {
            "text" -> {
                itemHolder.binding.cvPlayer.visibility = View.GONE
                itemHolder.binding.tvMessage.visibility = View.VISIBLE
            }
            "audio" -> {
                itemHolder.binding.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.tvMessage.visibility = View.GONE
            }
        }
        itemHolder.binding.ivPlayPause.setOnClickListener {
            mediaPlayer.setDataSource(item?.message)
            playVoiceMsg(itemHolder.binding)
        }

    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun playVoiceMsg(binding: ItemChatRightBinding) {
        if (!pause) {
            binding.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
            if (mediaPlayer != null)
                try {
                    mediaPlayer.prepare()
                } catch (e: IOException) {
                }
            mediaPlayer.start()
            binding.seekBar.max = mediaPlayer.seconds
            initializeSeekBar(binding)
            pause = true
        } else {

            binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
            mediaPlayer.stop()
//                mediaPlayer.release()

            binding.seekBar.max = 0
            pause = false


        }
    }

    private fun initializeSeekBar(binding: ItemChatRightBinding) {

        runnable = Runnable {

            val currentSeconds = mediaPlayer.currentPosition / 1000
            binding.seekBar.progress = currentSeconds
            binding.tvPlayerTime.text = timeFormatter.format(Date((currentSeconds * 1000).toLong()))
            val difference = mediaPlayer.seconds - mediaPlayer.currentSeconds
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    val MediaPlayer.seconds: Int
        get() {
            return mediaPlayer.duration / 1000
        }
    val MediaPlayer.currentSeconds: Int
        get() {
            return mediaPlayer.currentPosition / 1000
        }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}