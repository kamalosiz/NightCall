package com.example.kalam_android.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideContext
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForFindFriendsBinding
import com.example.kalam_android.repository.model.FindFriendList
import com.example.kalam_android.repository.model.FindFriends
import com.example.kalam_android.wrapper.GlideDownloder

class AdapterForFindFriends(val context: Context, var friendList: MutableList<FindFriendList>) :
    RecyclerView.Adapter<AdapterForFindFriends.FindFriendsVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendsVH {

        return FindFriendsVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_for_find_friends,
                parent,
                false
            )
        )
    }

    fun updateList(list: MutableList<FindFriendList>) {
        friendList.clear()
        friendList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {

        return friendList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FindFriendsVH, position: Int) {

        val item = friendList.get(position)
        holder.binding.tvName.text = item.firstname + " " + item.lastname
        holder.binding.tvLocation.text = item.country
        GlideDownloder.load(
            context,
            holder.binding.ivImage,
            item.profile_image,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
    }

    inner class FindFriendsVH(val binding: ItemForFindFriendsBinding) :
        RecyclerView.ViewHolder(binding.root)
}