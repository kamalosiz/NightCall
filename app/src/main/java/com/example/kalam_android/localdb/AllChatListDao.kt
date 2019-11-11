package com.example.kalam_android.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kalam_android.util.AppConstants
import io.reactivex.Single

@Dao
interface AllChatListDao {

    @Insert
    fun insert(contactsData: Iterable<AllChatEntityClass>)

    @Query("SELECT * FROM ${AppConstants.ALL_CHAT_ENTITY} ORDER BY unix_time DESC")
    fun getAllContacts(): Single<List<AllChatEntityClass>>

    @Query("DELETE FROM ${AppConstants.ALL_CHAT_ENTITY}")
    fun deleteAll()

    @Query("UPDATE ${AppConstants.ALL_CHAT_ENTITY} SET unix_time = :unix_time , message = :message  WHERE id = :uid")
    fun updateUnixTime(unix_time: String, message: String, uid: Int)
}