package com.example.kalam_android.callbacks

import android.view.View
import com.example.kalam_android.repository.model.MediaList

interface OnGalleryItemClickedListener {
    fun onGalleryItemClicked(list: ArrayList<MediaList>, view: View, position: Int)
}

interface RemoveItemCallBack {
    fun onRemoveItem(mediaList: MediaList)
}