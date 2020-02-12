package com.example.kalam_android.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kalam_android.localdb.entities.ContactsData
import com.example.kalam_android.util.AppConstants
import io.reactivex.Single

@Dao
interface ContactsDao {
    @Insert
    fun insert(contactsData: Iterable<ContactsData>)

    @Query("SELECT * FROM ${AppConstants.CONTACTS_TABLE}")
    fun getAllContacts(): Single<List<ContactsData>>

    @Query("DELETE FROM ${AppConstants.CONTACTS_TABLE}")
    fun deleteAll()
}