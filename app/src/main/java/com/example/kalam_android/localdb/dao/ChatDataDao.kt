package com.example.kalam_android.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.kalam_android.localdb.entities.ChatData
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ChatDataDao {
    @Insert
    fun insert(chatListItem: Iterable<ChatData>)

    @Insert
    fun insert(chatListItem: ChatData)
}