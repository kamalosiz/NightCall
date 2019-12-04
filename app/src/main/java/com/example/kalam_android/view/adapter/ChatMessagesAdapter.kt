package com.example.kalam_android.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.showAlertDialoge
import com.example.kalam_android.view.activities.OpenMediaActivity
import com.example.kalam_android.wrapper.GlideDownloder


class ChatMessagesAdapter(
    val context: Context,
    private val userId: String,
    val name: String,
    private val profile: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = this.javaClass.simpleName
    private var chatList: ArrayList<ChatData>? = ArrayList()


    fun updateList(list: ArrayList<ChatData>) {
        chatList?.addAll(list)
        notifyDataSetChanged()
    }

    fun updateIdentifier(identifier: String, isDelivered: Boolean) {
        chatList?.let {
            for (x in it.indices) {
                if (chatList?.get(x)?.identifier == identifier) {
                    chatList?.get(x)?.identifier = ""
                    if (isDelivered) {
                        chatList?.get(x)?.is_read = 1
                    } else {
                        chatList?.get(x)?.is_read = 0
                    }
//                    notifyDataSetChanged()
                    notifyItemChanged(x)
                }
            }
        }
    }

    fun updateReadStatus(isSeen: Boolean) {
        chatList?.let {
            for (x in it.indices) {
                if (isSeen) {
                    if (chatList?.get(x)?.is_read == 0 || chatList?.get(x)?.is_read == 1) {
                        chatList?.get(x)?.is_read = 2
                        notifyItemChanged(x)
                    }
                } else {
                    if (chatList?.get(x)?.is_read == 0) {
                        chatList?.get(x)?.is_read = 1
                        notifyItemChanged(x)
                    }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as MyHolder
        val item = chatList?.get(position)
        when (item?.type) {
            AppConstants.TEXT_MESSAGE -> {
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.itemChat.tvMessage.text = item.message
                itemHolder.binding.itemChat.llOriginal.setOnClickListener {
                    showAlertDialoge(context, "Original Message", item.original_message.toString())
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.END
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_send_background)
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_receive_background)
                }
                if (userId.toInt() == item.sender_id) {
                    itemHolder.binding.itemChat.ivDeliver.visibility = View.VISIBLE
                    when (item.is_read) {
                        0 -> itemHolder.binding.itemChat.ivDeliver.setBackgroundResource(R.drawable.icon_sent)
                        1 -> itemHolder.binding.itemChat.ivDeliver.setBackgroundResource(R.drawable.icon_message_sent)
                        2 -> itemHolder.binding.itemChat.ivDeliver.setBackgroundResource(R.drawable.icon_message_read)
                    }
                } else {
                    itemHolder.binding.itemChat.ivDeliver.visibility = View.GONE
                }
            }
            AppConstants.AUDIO_MESSAGE -> {
                logE("Identifier: ${item.identifier}")
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.audioPlayer.tvTime.text = "3:15 pm"
                } else {
                    itemHolder.binding.audioPlayer.tvTime.text = "Uploading Audio..."
                }
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.VISIBLE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.audioPlayer.ivPlayPause.setOnClickListener {
                    Global.playVoiceMsg(
                        itemHolder.binding, item.audio_url.toString(), item.id, context
                    )
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                }
                if (userId.toInt() == item.sender_id) {
                    itemHolder.binding.audioPlayer.ivDeliver.visibility = View.VISIBLE
                    when (item.is_read) {
                        0 -> itemHolder.binding.audioPlayer.ivDeliver.setBackgroundResource(R.drawable.icon_sent)
                        1 -> itemHolder.binding.audioPlayer.ivDeliver.setBackgroundResource(R.drawable.icon_message_sent)
                        2 -> itemHolder.binding.audioPlayer.ivDeliver.setBackgroundResource(R.drawable.icon_message_read)
                    }
                } else {
                    itemHolder.binding.audioPlayer.ivDeliver.visibility = View.GONE
                }
            }
            AppConstants.IMAGE_MESSAGE -> {
                logE("Identifier: ${item.identifier}")
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.imageHolder.tvTime.text = "3:15 pm"
                } else {
                    itemHolder.binding.imageHolder.tvTime.text = "Uploading Image..."
                }
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.VISIBLE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                GlideDownloder.load(
                    context,
                    itemHolder.binding.imageHolder.ivImage,
                    item.audio_url.toString(),
                    R.color.grey,
                    R.color.grey
                )
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.imageHolder.rlImageItem.gravity = Gravity.END
                } else {
                    itemHolder.binding.imageHolder.rlImageItem.gravity = Gravity.START
                }
                itemHolder.binding.imageHolder.rlImage.setOnClickListener {
                    startOpenMediaActivity(
                        item.audio_url.toString(),
                        AppConstants.IMAGE_MESSAGE,
                        itemHolder.binding.imageHolder.ivImage
                    )
                }
                if (userId.toInt() == item.sender_id) {
                    itemHolder.binding.imageHolder.ivDeliver.visibility = View.VISIBLE
                    when (item.is_read) {
                        0 -> itemHolder.binding.imageHolder.ivDeliver.setBackgroundResource(R.drawable.icon_sent)
                        1 -> itemHolder.binding.imageHolder.ivDeliver.setBackgroundResource(R.drawable.icon_message_sent)
                        2 -> itemHolder.binding.imageHolder.ivDeliver.setBackgroundResource(R.drawable.icon_message_read)
                    }
                } else {
                    itemHolder.binding.imageHolder.ivDeliver.visibility = View.GONE
                }
            }
            AppConstants.VIDEO_MESSAGE -> {
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.VISIBLE
                logE("Identifier: ${item.identifier}")
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.videoHolder.tvTime.text = "3:15 pm"
                } else {
                    itemHolder.binding.videoHolder.tvTime.text = "Uploading Video..."
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.videoHolder.rlVideoItem.gravity = Gravity.END
                } else {
                    itemHolder.binding.videoHolder.rlVideoItem.gravity = Gravity.START
                }
                GlideDownloder.load(
                    context,
                    itemHolder.binding.videoHolder.ivImage,
                    item.audio_url.toString(),
                    R.color.grey,
                    R.color.grey
                )
                itemHolder.binding.videoHolder.rlVideoItem.setOnClickListener {
                    startOpenMediaActivity(
                        item.audio_url.toString(),
                        AppConstants.VIDEO_MESSAGE,
                        itemHolder.binding.videoHolder.ivImage
                    )
                }
                if (userId.toInt() == item.sender_id) {
                    itemHolder.binding.videoHolder.ivDeliver.visibility = View.VISIBLE
                    when (item.is_read) {
                        0 -> itemHolder.binding.videoHolder.ivDeliver.setBackgroundResource(R.drawable.icon_sent)
                        1 -> itemHolder.binding.videoHolder.ivDeliver.setBackgroundResource(R.drawable.icon_message_sent)
                        2 -> itemHolder.binding.videoHolder.ivDeliver.setBackgroundResource(R.drawable.icon_message_read)
                    }
                } else {
                    itemHolder.binding.videoHolder.ivDeliver.visibility = View.GONE
                }
            }
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) : RecyclerView.ViewHolder(binding.root)

    private fun startOpenMediaActivity(file: String, type: String, view: View) {
        val intent = Intent(context, OpenMediaActivity::class.java)
        intent.putExtra(AppConstants.CHAT_FILE, file)
        intent.putExtra(AppConstants.CHAT_TYPE, type)
        intent.putExtra(AppConstants.USER_NAME, name)
        intent.putExtra(AppConstants.PROFILE_IMAGE_KEY, profile)

        val transitionName = context.getString(R.string.trans_key)
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                context as Activity,
                view,
                transitionName
            )
        ActivityCompat.startActivity(context, intent, options.toBundle())
    }


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}