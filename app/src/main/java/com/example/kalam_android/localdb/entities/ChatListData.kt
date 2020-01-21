package com.example.kalam_android.localdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalam_android.util.AppConstants

@Entity(tableName = AppConstants.ALL_CHAT_ENTITY)
data class ChatListData(
    @PrimaryKey var chat_id: Int,
    @ColumnInfo(name = "updated_at") var updated_at: String,
    @ColumnInfo(name = "unix_time") var unix_time: Double?,
    @ColumnInfo(name = "firstname") var firstname: String,
    @ColumnInfo(name = "lastname") var lastname: String,
    @ColumnInfo(name = "profile_image") var profile_image: String,
    @ColumnInfo(name = "message") var message: String?,
    @ColumnInfo(name = "un_read_count") var un_read_count: Int,
    @ColumnInfo(name = "user_id") var user_id: Int,
    @ColumnInfo(name = "nickname") var nickname: String?,
    @ColumnInfo(name = "last_message_id") var last_message_id: Long,
    @ColumnInfo(name = "is_read") var is_read: Int?,
    @ColumnInfo(name = "sender_id") var sender_id: Int?
)