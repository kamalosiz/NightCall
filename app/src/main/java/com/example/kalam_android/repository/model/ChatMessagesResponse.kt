package com.example.kalam_android.repository.model

data class ChatMessagesResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<ChatData>?,
    val message: String
)

data class ChatData(
    val original_audio_text: String?,
    val audio_url: String?,
    val original_audio_url: String?,
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
    val duration: Long,
    val original_message: String?,
    var identifier: String?
)

data class MediaResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: DataResponse?,
    val message: String
)

data class DataResponse(
    val file_url: String,
    val duration: Double,
    val message: String?,
    val type: String?,
    val identifier: String,
    val file_id: Long
)

data class MediaList(
    val file: String,
    val type: Int
)