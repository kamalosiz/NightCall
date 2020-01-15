package com.example.kalam_android.callbacks

import com.example.kalam_android.repository.model.MediaList

interface RemoveItemCallBack {
    fun onRemoveItem(mediaList: MediaList)
}