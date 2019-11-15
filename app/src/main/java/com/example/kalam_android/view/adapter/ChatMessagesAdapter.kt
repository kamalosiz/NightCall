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
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.showAlertDialoge
import com.example.kalam_android.wrapper.GlideDownloder
import kotlinx.android.synthetic.main.audio_player_item.view.*


class ChatMessagesAdapter(val context: Context, private val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = this.javaClass.simpleName
    private var chatList: ArrayList<ChatData>? = ArrayList()


    fun updateList(list: ArrayList<ChatData>) {
        chatList?.addAll(list)
        notifyDataSetChanged()
    }

    fun updateIdentifier(identifier: String) {
        chatList?.let {
            for (x in it) {
                if (x.identifier == identifier) {
                    x.identifier = ""
                    notifyDataSetChanged()
                }
            }
        }
    }

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
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.itemChat.tvMessage.text = item.message
                itemHolder.binding.itemChat.llOriginal.setOnClickListener {
                    showAlertDialoge(context, "Original Message", item.original_message.toString())
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.END
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                    itemHolder.binding.itemChat.tvTime.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_send_message)
                    itemHolder.binding.itemChat.view.setBackgroundResource(R.color.white)
                    itemHolder.binding.itemChat.tvOriginal.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.icon_receive_message)
                    itemHolder.binding.itemChat.tvTime.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.view.setBackgroundResource(R.color.black)
                    itemHolder.binding.itemChat.tvOriginal.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                }
            }
            AppConstants.AUDIO_MESSAGE -> {
                logE("Identifier: ${item.identifier}")
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.audioPlayer.tvTime.text = "3:15 pm"
                } else {
                    itemHolder.binding.audioPlayer.tvTime.text = "Uploading Audio..."
                }
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.VISIBLE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.audioPlayer.rlPlay.setOnClickListener {
                    Global.playVoiceMsg(itemHolder.binding, item.audio_url.toString(), position)
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                    itemHolder.binding.audioPlayer.cvPlayer.setBackgroundResource(R.drawable.audio_bubble_right)
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                    itemHolder.binding.audioPlayer.cvPlayer.setBackgroundResource(R.drawable.audio_bubble_left)
                }
            }
            AppConstants.IMAGE_MESSAGE -> {
                logE("Identifier: ${item.identifier}")
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.imageHolder.tvTime.text = "3:15 pm"
                } else {
                    itemHolder.binding.imageHolder.tvTime.text = "Uploading Image..."
                }
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.VISIBLE
                GlideDownloder.load(
                    context,
                    itemHolder.binding.imageHolder.ivImage,
                    item.file.toString(),
                    R.drawable.dummy_placeholder,
                    R.drawable.dummy_placeholder
                )
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.imageHolder.rlImageItem.gravity = Gravity.END
                } else {
                    itemHolder.binding.imageHolder.rlImageItem.gravity = Gravity.END
                }
            }
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}