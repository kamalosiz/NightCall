package com.example.kalam_android.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactsEntityClass::class, AllChatEntityClass::class], version = 3)
abstract class RoomDB : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao
    abstract fun allChatListDao(): AllChatListDao
}