package com.example.kalam_android.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactsEntityClass::class], version = 2)
abstract class RoomDB : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao
}