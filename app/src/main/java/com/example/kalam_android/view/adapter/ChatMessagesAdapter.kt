package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatRightBinding
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.util.Debugger

class ChatMessagesAdapter(val context: Context, private val userId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
    }

    inner class MyHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}