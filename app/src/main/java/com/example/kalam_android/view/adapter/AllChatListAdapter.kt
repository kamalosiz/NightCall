package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.databinding.ItemChatAdapterBinding
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.getTimeStamp
import com.example.kalam_android.wrapper.GlideDownloader


class AllChatListAdapter(
    val context: Context,
    val myClickListener: MyClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatList: ArrayList<ChatListData> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chat_adapter,
                parent,
                false
            )
        )
    }

    fun updateList(chatList: ArrayList<ChatListData>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    fun updateItem(chatList: ArrayList<ChatListData>, position: Int) {
        this.chatList = chatList
        notifyItemChanged(position)
    }


    fun newChatInserted(chatList: ArrayList<ChatListData>) {
        this.chatList = chatList
        Debugger.e("AllChatAdapter", "newChatInserted : ${this.chatList}")
        notifyItemInserted(0)
    }

    fun updateOnlineStatus(position: Int, status: Int) {
        chatList[position].is_Online = status
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderItem = holder as MyHolder
        val item = chatList[position]
        if (item.nickname.isNullOrEmpty()) {
            holderItem.binding.tvName.text =
                StringBuilder(item.firstname).append(" ").append(item.lastname)
        } else {
            holderItem.binding.tvName.text = item.nickname.toString()
        }
        holderItem.binding.tvLastMessage.text = item.message.toString()
        applyReadStatus(item.user_id, item.sender_id, holderItem.binding.tvStatus, item.is_read)
        if (item.un_read_count == 0) {
            holderItem.binding.llUnread.visibility = View.GONE
            holderItem.binding.tvAgo.setTextColor(
                Global.setColor(context, R.color.black)
            )
        } else {
            holderItem.binding.llUnread.visibility = View.VISIBLE
            holder.binding.tvUnread.text = item.un_read_count.toString()
            holderItem.binding.tvAgo.setTextColor(
                Global.setColor(context, R.color.light_green)
            )
        }

        holderItem.binding.tvAgo.text = item.unix_time?.let { getTimeStamp(it.toLong()) }
        GlideDownloader.load(
            context,
            holderItem.binding.ivImage,
            item.profile_image,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        holderItem.binding.rlItem.tag = position
        if (item.is_Online == 1) {
            holderItem.binding.ivOnline.visibility = View.VISIBLE
        } else {
            holderItem.binding.ivOnline.visibility = View.GONE
        }
    }

    inner class MyHolder(val binding: ItemChatAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rlItem.setOnClickListener {
                myClickListener.myOnClick(it, adapterPosition)
            }
        }
    }

    private fun applyReadStatus(userId: Int, senderId: Int?, view: TextView, isRead: Int?) {
        if (userId != senderId) {
            view.visibility = View.VISIBLE
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
                3 -> {
                    view.visibility = View.GONE
                }
            }
        } else {
            view.text = ""
        }
    }
}