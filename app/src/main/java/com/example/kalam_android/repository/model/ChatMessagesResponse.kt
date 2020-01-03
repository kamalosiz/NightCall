package com.example.kalam_android.repository.model

import java.io.Serializable

data class ChatMessagesResponse(
        val action: String,
        val code: Int,
        val status: Boolean,
        val data: DataObject,
        val message: String
)

data class DataObject(
        val chats: ArrayList<ChatData>?,
        val is_first_page: Int,
        val is_last_page: Int,
        val swipe_up: Int
)

data class ChatData(
        val original_audio_text: String?,
        var audio_url: String?,
        val original_audio_url: String?,
        val sender_name: String,
        var id: Long,
        val chat_id: Int,
        val sender_id: Int?,
        val receiver_id: Int,
        val message: String?,
        val sender_deleted: Int,
        val receiver_deleted: Int,
        val type: String,
        val file: String?,
        var is_read: Int,
        val duration: Long,
        val original_message: String?,
        var identifier: String?,
        val unix_time: Double,
        val language: String?
)

data class MediaResponse(
        val action: String,
        val code: Int,
        val status: Boolean,
        val data: ArrayList<DataResponse>?,
        val message: String
)

data class DataResponse(
        val file_url: String,
        val duration: Double,
        val message: String?,
        val type: String?,
        val identifier: String,
        val thumbnail: String?,
        val file_id: Long,
        val group_id: String?
)

data class MediaList(val file: String, val type: Int) : Serializable

data class NotificationResponse(
        val type: String,
        val sender_name: String,
        val message: String?,
        val original_message: String?
)