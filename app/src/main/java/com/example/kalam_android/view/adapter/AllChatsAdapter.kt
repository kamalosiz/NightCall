package com.example.kalam_android.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemChatAdapterBinding
import com.example.kalam_android.repository.model.ChatListData
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.wrapper.GlideDownloder
import java.lang.StringBuilder

class AllChatsAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chatList: ArrayList<ChatListData>? = null
    private val onClickListener: View.OnClickListener

    init {
        onClickListener = AdapterClickListener()
    }

    fun updateList(chatList: ArrayList<ChatListData>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

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
            binding.rlItem.setOnClickListener(onClickListener)
        }
    }

    private inner class AdapterClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.rlItem -> {
                    val feedIndex: Int = Integer.parseInt(v.tag.toString())
                    val item = chatList?.get(feedIndex)
                    val intent = Intent(context, ChatDetailActivity::class.java)
                    intent.putExtra(AppConstants.CHAT_ID, item?.chat_id)
                    intent.putExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, true)
                    context.startActivity(intent)
                }
            }
        }
    }
}