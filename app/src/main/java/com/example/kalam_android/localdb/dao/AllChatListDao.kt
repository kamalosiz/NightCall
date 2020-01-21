package com.example.kalam_android.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.util.AppConstants
import io.reactivex.Single

@Dao
interface AllChatListDao {

    @Insert
    fun insertChat(chatListItem: Iterable<ChatListData>)

    @Insert
    fun insertChat(chatListItem: ChatListData)

    @Query("SELECT * FROM ${AppConstants.ALL_CHAT_ENTITY} ORDER BY unix_time DESC")
    fun getAllContacts(): Single<List<ChatListData>>

    @Query("DELETE FROM ${AppConstants.ALL_CHAT_ENTITY}")
    fun deleteAll()

    @Query("UPDATE ${AppConstants.ALL_CHAT_ENTITY} SET unix_time = :unix_time , message = :message , un_read_count = :unReadcount , sender_id = :sender_id, is_read = :isRead WHERE chat_id = :chatId")
    fun updateItem(
        unix_time: String,
        message: String,
        chatId: Int,
        unReadcount: Int,
        sender_id: Int?,
        isRead: Int?
    )
}