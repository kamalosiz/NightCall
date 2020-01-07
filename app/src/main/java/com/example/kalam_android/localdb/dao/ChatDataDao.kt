package com.example.kalam_android.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.kalam_android.localdb.entities.ChatData

@Dao
interface ChatDataDao {
    @Insert
    fun insertChat(chatListItem: Iterable<ChatData>)

    @Insert
    fun insertChat(chatListItem: ChatData)
}