package com.example.kalam_android.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kalam_android.localdb.dao.AllChatListDao
import com.example.kalam_android.localdb.dao.ChatDataDao
import com.example.kalam_android.localdb.dao.ContactsDao
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.localdb.entities.ContactsData

@Database(
    entities = [ContactsData::class, ChatListData::class, ChatData::class],
    version = 2
)
abstract class RoomDB : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao
    abstract fun allChatListDao(): AllChatListDao
    abstract fun chatData(): ChatDataDao
}