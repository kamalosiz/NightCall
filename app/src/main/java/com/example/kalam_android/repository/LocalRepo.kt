package com.example.kalam_android.repository

import android.util.Log
import com.example.kalam_android.localdb.ContactsDao
import com.example.kalam_android.localdb.ContactsEntityClass
import com.example.kalam_android.repository.model.ContactsData
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepo @Inject constructor(private val dao: ContactsDao) {

    fun insertIntoDB(contactsData: ArrayList<ContactsEntityClass>) {
        contactsData.let {
            Log.i("testingLocal", "insertIntoDB: $it")
            dao.insert(it)
        }
    }

    fun getContactsFromLocal(): Single<List<ContactsEntityClass>> {
        return dao.getAllContacts()
    }

    fun removeContacts() {
        return dao.deleteAll()
    }
}