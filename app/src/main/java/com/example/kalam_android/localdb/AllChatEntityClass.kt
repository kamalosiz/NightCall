package com.example.kalam_android.localdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalam_android.util.AppConstants

@Entity(tableName = AppConstants.ALL_CHAT_ENTITY)
data class AllChatEntityClass(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "chat_id") var chatID: Int,
    @ColumnInfo(name = "updated_at") var updated_at: String,
    @ColumnInfo(name = "unix_time") var unix_time: Long,
    @ColumnInfo(name = "firstname") var firstname: String,
    @ColumnInfo(name = "lastname") var lastname: String,
    @ColumnInfo(name = "profile_image") var profile_image: String,
    @ColumnInfo(name = "message") var message: String?,
    @ColumnInfo(name = "un_read_count") var un_read_count: Int
)