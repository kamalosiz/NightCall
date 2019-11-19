package com.example.kalam_android.repository.model

import com.example.kalam_android.localdb.entities.ChatListData

data class AllChatListResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<ChatListData>?,
    val message: String
)
