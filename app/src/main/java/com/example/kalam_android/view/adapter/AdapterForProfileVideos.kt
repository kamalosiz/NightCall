package com.example.kalam_android.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemUserProfileVideoBinding

class AdapterForProfileVideos : RecyclerView.Adapter<ProfileVideosHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileVideosHolder {
        return ProfileVideosHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_profile_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {

        return 0
    }

    override fun onBindViewHolder(holder: ProfileVideosHolder, position: Int) {
    }
}

class ProfileVideosHolder(val binding: ItemUserProfileVideoBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
