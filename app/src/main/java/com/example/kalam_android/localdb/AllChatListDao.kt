package com.example.kalam_android.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kalam_android.util.AppConstants
import io.reactivex.Single

@Dao
interface AllChatListDao {
    @Insert
    fun insert(contactsData: Iterable<ContactsEntityClass>)

    @Query("SELECT * FROM ${AppConstants.CONTACTS_TABLE}")
    fun getAllContacts(): Single<List<ContactsEntityClass>>

    @Query("DELETE FROM ${AppConstants.CONTACTS_TABLE}")
    fun deleteAll()
}