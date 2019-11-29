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
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.calculateLocalDate
import com.example.kalam_android.wrapper.GlideDownloder


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

    fun updateReadCount(chatList: ArrayList<ChatListData>, position: Int) {
        this.chatList = chatList
        notifyItemChanged(position)
    }

    fun updateList(chatList: ArrayList<ChatListData>) {
        this.chatList = chatList
        notifyDataSetChanged()
    }

    /*fun updateNewList(newList: ArrayList<ChatListData>) {
//        Debugger.e("ChatsFragment","Old Chats : $chatList")
//        Debugger.e("ChatsFragment","new Chats : $newList")
        val diffCallback = DiffUtilClass(this.chatList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.chatList.clear()
        this.chatList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }*/

    fun newChatInserted(chatList: ArrayList<ChatListData>) {
        this.chatList = chatList
        Debugger.e("AllChatAdapter", "newChatInserted : ${this.chatList}")
        notifyItemInserted(0)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderItem = holder as MyHolder
        val item = chatList[position]
        holderItem.binding.tvName.text =
            StringBuilder(item.firstname).append(" ").append(item.lastname)
        holderItem.binding.tvLastMessage.text = item.message.toString()
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
        holderItem.binding.tvAgo.text = item.unix_time?.let { calculateLocalDate(it) }
        GlideDownloder.load(
            context,
            holderItem.binding.ivImage,
            item.profile_image,
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