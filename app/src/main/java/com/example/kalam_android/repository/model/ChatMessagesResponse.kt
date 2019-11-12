package com.example.kalam_android.repository.model

data class ChatMessagesResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<ChatData>?,
    val message: String
)

data class ChatData(
    val sender_name: String,
    val id: Int,
    val chat_id: Int,
    val sender_id: Int?,
    val receiver_id: Int,
    val message: String?,
    val sender_deleted: Int,
    val receiver_deleted: Int,
    val type: String,
    val file: String?,
    val is_read: Int,
    val duration: Int,
    val original_message: String?,
    val tr_file: String?
)

data class AudioResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: DataResponse?,
    val message: String
)

data class DataResponse(
    val file_url: String
)