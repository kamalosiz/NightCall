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
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.Debugger
import java.io.IOException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import java.io.File
import java.io.FileInputStream


class ChatMessagesAdapter(val context: Context, private val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private var isStop: Boolean = false
    private var isPause: Boolean = false
    private var isFirstPlay: Boolean = true
    private var currentPos = -1
    private var prePos = -1
    private var isPlayerRelease : Boolean =true
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
        isFirstPlay = true
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
        if (item?.sender_id == userId.toInt()) {
            itemHolder.binding.rlMessage.gravity = Gravity.END
            itemHolder.binding.ivMessage.setBackgroundResource(R.drawable.icon_send_message)
        } else {
            itemHolder.binding.rlMessage.gravity = Gravity.START
            itemHolder.binding.ivMessage.setBackgroundResource(R.drawable.icon_receive_message)
        }
        when (item?.type) {
            "text" -> {
                itemHolder.binding.cvPlayer.visibility = View.GONE
                itemHolder.binding.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.tvMessage.text = item?.message

            }
            "audio" -> {
                itemHolder.binding.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.rlMessage.visibility = View.GONE
            }
        }

        itemHolder.binding.ivPlayPause.setOnClickListener {

            currentPos = position
            playVoiceMsg(itemHolder.binding, item?.message.toString())

        }
        itemHolder.binding.ivStop.setOnClickListener {
            itemHolder.binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
            itemHolder.binding.seekBar.max = 0
            isStop = false
            mediaPlayer.stop()
            mediaPlayer.release()
            isFirstPlay = true
            isPlayerRelease = true
        }

    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun playVoiceMsg(binding: ItemChatRightBinding, voiceMessage: String) {


        try {

            if (isPlayerRelease && currentPos != prePos) {
                prePos = currentPos
                mediaPlayer = MediaPlayer()
                val audioAttributes = AudioAttributes.Builder()
                audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                audioAttributes.setLegacyStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setAudioAttributes(audioAttributes.build())
                isStop = true
                val file = File(voiceMessage)
                val fd = FileInputStream(file)
                binding.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
                mediaPlayer.setDataSource(fd.fd)
                initializeSeekBar(binding)
                mediaPlayer.prepare()
                mediaPlayer.start()
                binding.seekBar.max = mediaPlayer.seconds
                isFirstPlay = false

            }

            /*if (isFirstPlay) {



            }*/ else {

                if (!mediaPlayer.isPlaying) {
                    binding.ivPlayPause.setBackgroundResource(R.drawable.icon_pause)
                    mediaPlayer.start()
                } else {
                    binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
                    mediaPlayer.pause()
                    isStop = false
                }
            }

        } catch (e: IllegalStateException) {
            logE("exception:${e.message}")
        }

        mediaPlayer.setOnCompletionListener { mp ->

            binding.ivPlayPause.setBackgroundResource(R.drawable.icon_play)
            isStop = false
            mp.stop()
            mp.release()
            binding.seekBar.max = 0
            isFirstPlay = true
            isPlayerRelease = true
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
            var currentSeconds: Int = 0
            try {
                currentSeconds = mediaPlayer.currentPosition / 1000
                binding.seekBar.progress = currentSeconds
                binding.tvPlayerTime.text =
                    timeFormatter.format(Date((currentSeconds * 1000).toLong()))
                val difference = mediaPlayer.seconds - mediaPlayer.currentSeconds
                handler.postDelayed(runnable, 1000)
            } catch (e: IllegalStateException) {

            }
        }
        handler.postDelayed(runnable, 1000)
    }

    val MediaPlayer.seconds: Int
        get() {
            var seconds: Int = 0
            try {
                seconds = mediaPlayer.duration / 1000

            } catch (e: IllegalStateException) {
            }
            return seconds
        }
    val MediaPlayer.currentSeconds: Int
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