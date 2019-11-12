package com.example.kalam_android.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kalam_android.repository.model.ContactsData
import com.example.kalam_android.util.AppConstants
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.DELETE
import java.util.*

@Dao
interface ContactsDao {

    @Insert
    fun insert(contactsData: Iterable<ContactsEntityClass>)

    @Query("SELECT * FROM ${AppConstants.CONTACTS_TABLE}")
    fun getAllContacts(): Single<List<ContactsEntityClass>>

    @Query("DELETE FROM ${AppConstants.CONTACTS_TABLE}")
    fun deleteAll()


}