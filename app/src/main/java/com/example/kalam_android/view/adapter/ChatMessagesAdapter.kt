package com.example.kalam_android.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
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
import com.example.kalam_android.wrapper.GlideDownloder
import androidx.core.content.ContextCompat.startActivity
import com.example.kalam_android.view.activities.OpenMediaActivity


class ChatMessagesAdapter(
    val context: Context,
    private val userId: String,
    val name: String,
    val profile: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = this.javaClass.simpleName
    private var chatList: ArrayList<ChatData>? = ArrayList()


    fun updateList(list: ArrayList<ChatData>) {
        chatList?.addAll(list)
        notifyDataSetChanged()
    }

    fun updateIdentifier(identifier: String) {
        chatList?.let {
            for (x in it.indices) {
                if (chatList?.get(x)?.identifier == identifier) {
                    chatList?.get(x)?.identifier = ""
                    notifyDataSetChanged()
//                    notifyItemChanged(x)
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
                    itemHolder.binding.itemChat.tvTime.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_send_background)
                    itemHolder.binding.itemChat.view.setBackgroundResource(R.color.white)
                    itemHolder.binding.itemChat.tvOriginal.setTextColor(
                        Global.setColor(context, R.color.white)
                    )
                } else {
                    itemHolder.binding.itemChat.rlMessage.gravity = Gravity.START
                    itemHolder.binding.itemChat.tvMessage.setTextColor(
                        Global.setColor(context, R.color.black)
                    )
                    itemHolder.binding.itemChat.ivMessage.setBackgroundResource(R.drawable.text_receive_background)
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
                itemHolder.binding.videoHolder.rlVideoItem.visibility = View.GONE
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
                    itemHolder.binding.imageHolder.rlImage.setBackgroundResource(R.drawable.sender_image_video_bg)
                } else {
                    itemHolder.binding.imageHolder.rlImageItem.gravity = Gravity.START
                    itemHolder.binding.imageHolder.rlImage.setBackgroundResource(R.drawable.receiver_image_video_bg)
                }
                itemHolder.binding.imageHolder.rlImage.setOnClickListener {
                    val intent = Intent(context, OpenMediaActivity::class.java)
                    intent.putExtra(AppConstants.CHAT_FILE, item.audio_url.toString())
                    intent.putExtra(AppConstants.CHAT_TYPE, AppConstants.IMAGE_MESSAGE)
                    intent.putExtra(AppConstants.USER_NAME, name)
                    intent.putExtra(AppConstants.PROFILE_IMAGE_KEY, profile)

                    val transitionName = context.getString(R.string.image_trans)
                    val options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            context as Activity,
                            itemHolder.binding.imageHolder.ivImage, // Starting view
                            transitionName    // The String
                        )
                    ActivityCompat.startActivity(context, intent, options.toBundle())
                }
            }
            AppConstants.VIDEO_MESSAGE -> {
                itemHolder.binding.audioPlayer.cvPlayer.visibility = View.GONE
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
                    itemHolder.binding.videoHolder.rlVideo.setBackgroundResource(R.drawable.sender_image_video_bg)
                } else {
                    itemHolder.binding.videoHolder.rlVideoItem.gravity = Gravity.START
                    itemHolder.binding.videoHolder.rlVideo.setBackgroundResource(R.drawable.receiver_image_video_bg)
                }
                GlideDownloder.load(
                    context,
                    itemHolder.binding.videoHolder.ivImage,
                    item.audio_url.toString(),
                    R.color.grey,
                    R.color.grey
                )
            }
        }
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}