package com.example.kalam_android.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.helper.MyChatMediaHelper
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.getTimeStamp
import com.example.kalam_android.view.activities.OpenMediaActivity
import com.example.kalam_android.wrapper.GlideDownloader


class ChatMessagesAdapter(
    val context: Context,
    private val userId: String,
    val name: String,
    private val profile: String,
    private val translateState: Int?,
    private val language: String?,
    private val myChatMediaHelper: MyChatMediaHelper?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = this.javaClass.simpleName
    private var chatList: ArrayList<ChatData>? = ArrayList()

    fun updateList(list: ArrayList<ChatData>, isDown: Boolean) {
        if (isDown) {
            logE("isDown is true")
            var i = list.size - 1
            while (i > -1) {
                chatList?.add(0, list[i])
                notifyItemInserted(0)
                i--
            }
        } else {
            chatList?.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun updateIdentifier(identifier: String, isDelivered: Boolean, msgId: String) {
        chatList?.let {
            for (x in it.indices) {
                if (chatList?.get(x)?.identifier == identifier) {
                    chatList?.get(x)?.identifier = ""
                    chatList?.get(x)?.id = msgId.toLong()
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

    fun updateSeenStatus(msgId: Long) {
        chatList?.let {
            for (x in it.indices) {
                if (chatList?.get(x)?.id?.toInt() == msgId.toInt()) {
                    chatList?.get(x)?.is_read = 2
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
                hideShowViewOriginal(
                    itemHolder.binding.itemChat.llOriginal,
                    item.language.toString()
                )
                itemHolder.binding.itemChat.tvTime.text = getTimeStamp(item.unix_time.toLong())
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.VISIBLE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.itemChat.tvMessage.text = item.message
                val regex = "(?s).*\\p{InArabic}.*"
                if (item.message?.matches(Regex(regex)) == true) {
                    itemHolder.binding.itemChat.tvMessage.typeface = Global.changeText(context, 0)
                } else {
                    itemHolder.binding.itemChat.tvMessage.typeface = Global.changeText(context, 1)
                }
                itemHolder.binding.itemChat.llOriginal.setOnClickListener {
                    if (itemHolder.binding.itemChat.tvOriginal.text == "View Original") {
                        itemHolder.binding.itemChat.tvOriginal.text = "View Translated"
                        itemHolder.binding.itemChat.tvMessage.text = item.original_message
                    } else {
                        itemHolder.binding.itemChat.tvOriginal.text = "View Original"
                        itemHolder.binding.itemChat.tvMessage.text = item.message
                    }
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.END
                    itemHolder.binding.itemChat.llOriginal.visibility = View.GONE
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_send_background)
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.llOriginal.gravity = View.VISIBLE
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_receive_background)
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.itemChat.ivDeliver, item.is_read
                )
            }
            AppConstants.AUDIO_MESSAGE -> {
                hideShowViewOriginal(
                    itemHolder.binding.audioPlayer.llOriginal,
                    item.language.toString()
                )
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.audioPlayer.tvTime.text =
                        getTimeStamp(item.unix_time.toLong())
                } else {
                    itemHolder.binding.audioPlayer.tvTime.text = "Uploading Audio..."
                }
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.VISIBLE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.audioPlayer.ivPlayPause.setOnClickListener {
                    myChatMediaHelper?.playVoiceMsg(
                        itemHolder.binding, item.audio_url.toString(), item.id, context
                    )
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                    itemHolder.binding.audioPlayer.llOriginal.gravity = View.GONE
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                    itemHolder.binding.audioPlayer.llOriginal.gravity = View.VISIBLE
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.audioPlayer.ivDeliver, item.is_read
                )
                var audioUrl = ""
                itemHolder.binding.audioPlayer.llOriginal.setOnClickListener {
                    if (itemHolder.binding.audioPlayer.tvOriginal.text == "Play Original") {
                        itemHolder.binding.audioPlayer.tvOriginal.text = "Play Translated"
                        audioUrl = item.audio_url.toString()
                        item.audio_url = item.original_audio_url
                    } else {
                        itemHolder.binding.audioPlayer.tvOriginal.text = "Play Original"
                        item.audio_url = audioUrl
                    }
                }
            }
            AppConstants.IMAGE_MESSAGE -> {
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.imageHolder.tvTime.text =
                        getTimeStamp(item.unix_time.toLong())
                } else {
                    itemHolder.binding.imageHolder.tvTime.text = "Uploading Image..."
                }
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.VISIBLE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                GlideDownloader.load(
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
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.imageHolder.ivDeliver, item.is_read
                )
            }
            AppConstants.VIDEO_MESSAGE -> {
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.VISIBLE
                if (item.identifier.isNullOrEmpty()) {
                    itemHolder.binding.videoHolder.tvTime.text =
                        getTimeStamp(item.unix_time.toLong())
                } else {
                    itemHolder.binding.videoHolder.tvTime.text = "Uploading Video..."
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.videoHolder.rlVideoItem.gravity = Gravity.END
                } else {
                    itemHolder.binding.videoHolder.rlVideoItem.gravity = Gravity.START
                }
                GlideDownloader.load(
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
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.videoHolder.ivDeliver, item.is_read
                )
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

    private fun applyReadStatus(userId: Int, senderId: Int?, view: ImageView, isRead: Int) {
        if (userId == senderId) {
            view.visibility = View.VISIBLE
            when (isRead) {
                0 -> view.setBackgroundResource(R.drawable.icon_sent)
                1 -> view.setBackgroundResource(R.drawable.icon_message_sent)
                2 -> view.setBackgroundResource(R.drawable.icon_message_read)
            }
        } else {
            view.visibility = View.GONE
        }
    }

    private fun hideShowViewOriginal(view: LinearLayout, msgLng: String) {
        if (translateState == 1) {
            view.visibility = View.VISIBLE
            if (language == msgLng) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }
        } else {
            view.visibility = View.GONE
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}