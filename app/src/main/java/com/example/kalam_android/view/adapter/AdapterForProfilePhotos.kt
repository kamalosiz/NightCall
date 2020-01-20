package com.example.kalam_android.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemUserProfileImageBinding

class AdapterForProfilePhotos : RecyclerView.Adapter<ProfilePhotosHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePhotosHolder {
        return ProfilePhotosHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_profile_image,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(holder: ProfilePhotosHolder, position: Int) {
    }
}

class ProfilePhotosHolder(val binding: ItemUserProfileImageBinding) :
    RecyclerView.ViewHolder(binding.root)
