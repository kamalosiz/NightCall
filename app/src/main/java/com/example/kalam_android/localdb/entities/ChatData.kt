package com.example.kalam_android.localdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalam_android.util.AppConstants

@Entity(tableName = AppConstants.CHAT_MESSAGES)
data class ChatData(
    @ColumnInfo(name = "original_audio_text") var original_audio_text: String?,
    @ColumnInfo(name = "audio_url") var audio_url: String?,
    @ColumnInfo(name = "original_audio_url") var original_audio_url: String?,
    @ColumnInfo(name = "sender_name") var sender_name: String,
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "chat_id") var chat_id: Int,
    @ColumnInfo(name = "sender_id") var sender_id: Int?,
    @ColumnInfo(name = "receiver_id") var receiver_id: Int,
    @ColumnInfo(name = "message") var message: String?,
    @ColumnInfo(name = "sender_deleted") var sender_deleted: Int,
    @ColumnInfo(name = "receiver_deleted") val receiver_deleted: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "file") val file: String?,
    @ColumnInfo(name = "is_read") var is_read: Int,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "original_message") val original_message: String?,
    @ColumnInfo(name = "identifier") var identifier: String?,
    @ColumnInfo(name = "unix_time") val unix_time: Double,
    @ColumnInfo(name = "language") val language: String?,
    @ColumnInfo(name = "profile_image") val profile_image: String?
)