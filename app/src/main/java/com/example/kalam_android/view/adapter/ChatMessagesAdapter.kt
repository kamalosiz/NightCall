package com.example.kalam_android.view.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.SelectItemListener
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
    private val myChatMediaHelper: MyChatMediaHelper?,
    private val selectItemListener: SelectItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //    private val TAG = this.javaClass.simpleName
    private var chatList: ArrayList<ChatData>? = ArrayList()

    fun updateList(list: ArrayList<ChatData>, isDown: Boolean) {
        if (isDown) {
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

    fun updateList(list: ArrayList<ChatData>) {
        chatList = list
        notifyDataSetChanged()
    }

    fun itemChanged(list: ArrayList<ChatData>, position: Int) {
        this.chatList = list
        notifyItemChanged(position)
    }

    fun itemRemoved(list: ArrayList<ChatData>, position: Int) {
        chatList = list
        notifyItemRemoved(position)
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

    fun addMessage(list: ArrayList<ChatData>) {
        chatList = list
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
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
                itemHolder.binding.location.rlLocation.visibility = View.GONE
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
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_receive_background)
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.itemChat.tvMessageStatus, item.is_read
                )
                checkSelected(item, itemHolder.binding.itemChat.rlMessage)
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
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
                itemHolder.binding.location.rlLocation.visibility = View.GONE
                itemHolder.binding.audioPlayer.ivPlayPause.setOnClickListener {
                    myChatMediaHelper?.playVoiceMsg(
                        itemHolder.binding,
                        item.audio_url.toString(),
                        item.id,
                        context,
                        item.unix_time,
                        item.language!!
                    )
                }
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.END
                    itemHolder.binding.audioPlayer.llOriginal.visibility = View.GONE
                    itemHolder.binding.audioPlayer.llViewText.visibility = View.GONE
                } else {
                    itemHolder.binding.audioPlayer.rlAudioItem.gravity = Gravity.START
                    itemHolder.binding.audioPlayer.llViewText.visibility = View.VISIBLE
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.audioPlayer.tvMessageStatus, item.is_read
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
                checkSelected(item, itemHolder.binding.audioPlayer.rlAudioItem)
                var isExpanded = false
                itemHolder.binding.audioPlayer.llViewText.setOnClickListener {
                    if (isExpanded) {
                        isExpanded = false
                        Global.collapse(itemHolder.binding.audioPlayer.llRecordedText)
                        itemHolder.binding.audioPlayer.tvViewText.text = "View Text"
                    } else {
                        isExpanded = true
                        itemHolder.binding.audioPlayer.tvViewText.text = "Hide Text"
                        if (item.original_audio_text?.isNotEmpty() == true) {
                            Global.expand(itemHolder.binding.audioPlayer.llRecordedText)
                            itemHolder.binding.audioPlayer.tvRecordedText.text =
                                item.original_audio_text.toString()
                        }

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
                itemHolder.binding.location.rlLocation.visibility = View.GONE
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
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
                        item.audio_url.toString(), item.chat_id,
                        AppConstants.IMAGE_MESSAGE,
                        itemHolder.binding.imageHolder.ivImage
                    )
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.imageHolder.tvMessageStatus, item.is_read
                )
                checkSelected(item, itemHolder.binding.imageHolder.rlImageItem)
            }
            AppConstants.VIDEO_MESSAGE -> {
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.VISIBLE
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
                itemHolder.binding.location.rlLocation.visibility = View.GONE

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
                itemHolder.binding.videoHolder.rlVideo.setOnClickListener {
                    Debugger.e("testingVideo", "rlVideo ${item.audio_url.toString()}")
                    startOpenMediaActivity(
                        item.audio_url.toString(), item.chat_id,
                        AppConstants.VIDEO_MESSAGE,
                        itemHolder.binding.videoHolder.ivImage
                    )
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.videoHolder.tvMessageStatus, item.is_read
                )
                checkSelected(item, itemHolder.binding.videoHolder.rlVideoItem)
            }
            AppConstants.LOCATION_MESSAGE -> {
                Debugger.e("ChatMessagesAdapter", "LOCATION_MESSAGE message:${item.message}")
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
                itemHolder.binding.location.rlLocation.visibility = View.VISIBLE
                itemHolder.binding.location.tvTime.text = getTimeStamp(item.unix_time.toLong())
                if (item.sender_id == userId.toInt()) {
                    itemHolder.binding.location.rlLocation.gravity = Gravity.END
                } else {
                    itemHolder.binding.location.rlLocation.gravity = Gravity.START
                }
                itemHolder.binding.location.ivImage.setOnClickListener {
                    startOpenMediaActivity(
                        item.message.toString(), item.chat_id,
                        AppConstants.LOCATION_KEY,
                        itemHolder.binding.location.ivImage
                    )
                }
                applyReadStatus(
                    userId.toInt(), item.sender_id,
                    itemHolder.binding.location.tvMessageStatus, item.is_read
                )
                checkSelected(item, itemHolder.binding.location.rlLocation)
            }
            else -> {
                itemHolder.binding.audioPlayer.rlAudioItem.visibility = View.GONE
                itemHolder.binding.itemChat.rlMessage.visibility = View.GONE
                itemHolder.binding.imageHolder.rlImageItem.visibility = View.GONE
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
                itemHolder.binding.groupHolder.rlMultiImageItem.visibility = View.GONE
                itemHolder.binding.location.rlLocation.visibility = View.GONE
            }
        }
    }

    private fun checkSelected(item: ChatData, view: RelativeLayout) {
        if (item.is_selected) {
            view.setBackgroundResource(R.color.shared_gray)
        } else {
            view.setBackgroundResource(0)
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val rlMessage: RelativeLayout = binding.itemChat.rlMessage
        private val rlAudio: RelativeLayout = binding.audioPlayer.rlAudioItem
        private val rlImage: RelativeLayout = binding.imageHolder.rlImageItem
        private val rlVideo: RelativeLayout = binding.videoHolder.rlVideoItem
        private val rlLocation: RelativeLayout = binding.location.rlLocation

        init {
            rlMessage.setOnClickListener { view ->
                selectItemListener.itemListener(view, adapterPosition, false)
            }
            rlMessage.setOnLongClickListener {
                selectItemListener.itemListener(it, adapterPosition, true)
                return@setOnLongClickListener true
            }
            rlAudio.setOnClickListener { view ->
                selectItemListener.itemListener(view, adapterPosition, false)
            }
            rlAudio.setOnLongClickListener {
                selectItemListener.itemListener(it, adapterPosition, true)
                return@setOnLongClickListener true
            }
            rlImage.setOnClickListener { view ->
                selectItemListener.itemListener(view, adapterPosition, false)
            }
            rlImage.setOnLongClickListener {
                selectItemListener.itemListener(it, adapterPosition, true)
                return@setOnLongClickListener true
            }
            rlVideo.setOnClickListener { view ->
                selectItemListener.itemListener(view, adapterPosition, false)
            }
            rlVideo.setOnLongClickListener {
                selectItemListener.itemListener(it, adapterPosition, true)
                return@setOnLongClickListener true
            }
            rlLocation.setOnClickListener { view ->
                selectItemListener.itemListener(view, adapterPosition, false)
            }
            rlLocation.setOnLongClickListener {
                selectItemListener.itemListener(it, adapterPosition, true)
                return@setOnLongClickListener true
            }
        }
    }

    private fun startOpenMediaActivity(file: String, chatId: Int, type: String, view: View) {
        val intent = Intent(context, OpenMediaActivity::class.java)
        intent.putExtra(AppConstants.CHAT_FILE, file)
        intent.putExtra(AppConstants.CHAT_TYPE, type)
        intent.putExtra(AppConstants.USER_NAME, name)
        intent.putExtra(AppConstants.PROFILE_IMAGE_KEY, profile)
        intent.putExtra("location_chat_id", chatId)

        val transitionName = context.getString(R.string.trans_key)
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                context as Activity,
                view,
                transitionName
            )
        ActivityCompat.startActivity(context, intent, options.toBundle())
    }

    private fun applyReadStatus(userId: Int, senderId: Int?, view: TextView, isRead: Int) {
        if (userId == senderId) {
            view.visibility = View.VISIBLE
            view.setTextColor(Global.setColor(context, R.color.darkGrey))
            when (isRead) {
                0 -> {
                    view.text = "Sent"
                }
                1 -> {
                    view.text = "Delivered"
                }
                2 -> {
                    view.text = "Seen"
                }
            }
        } else {
            view.text = ""
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
}