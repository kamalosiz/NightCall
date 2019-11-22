package com.example.kalam_android.callbacks

import com.example.kalam_android.repository.model.MediaList

interface MediaListCallBack {
    fun mediaListResponse(list: ArrayList<MediaList>?)
}