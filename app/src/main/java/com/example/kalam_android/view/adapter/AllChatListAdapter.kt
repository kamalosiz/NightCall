package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.MyClickListener
import com.example.kalam_android.databinding.ItemChatAdapterBinding
import com.example.kalam_android.repository.model.ChatListData
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.calculateLocalDate
import com.example.kalam_android.wrapper.GlideDownloder

class AllChatListAdapter(
    val context: Context,
    val myClickListener: MyClickListener,
    private val chatList: ArrayList<ChatListData>?

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    override fun getItemCount(): Int {
        return chatList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderItem = holder as MyHolder
        val item = chatList?.get(position)
        holderItem.binding.tvName.text =
            StringBuilder(item?.firstname.toString()).append(" ").append(item?.lastname.toString())
        holderItem.binding.tvLastMessage.text = item?.message.toString()
        if (item?.un_read_count == 0) {
            holderItem.binding.llUnread.visibility = View.GONE
        } else {
            holderItem.binding.llUnread.visibility = View.VISIBLE
            holder.binding.tvUnread.text = item?.un_read_count.toString()
            holderItem.binding.tvAgo.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_green
                )
            )
        }

        holderItem.binding.tvAgo.text = item?.unix_time?.let { calculateLocalDate(it) }
        GlideDownloder.load(
            context,
            holderItem.binding.ivImage,
            item?.profile_image.toString(),
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        holderItem.binding.rlItem.tag = position
    }

    inner class MyHolder(val binding: ItemChatAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rlItem.setOnClickListener {
                myClickListener.myOnClick(it, adapterPosition)
            }
        }
    }
}