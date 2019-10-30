package com.example.kalam_android.repository.model

data class AllChatListResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<ChatListData>,
    val message: String
)

data class ChatListData(
    val chat_id: Int,
    val firstname: String,
    val lastname: String,
    val profile_image: String,
    val message: String?
)