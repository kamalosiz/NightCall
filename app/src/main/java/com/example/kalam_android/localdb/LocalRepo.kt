package com.example.kalam_android.localdb

import android.util.Log
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepo @Inject constructor(
    private val dao: ContactsDao,
    private val daoChats: AllChatListDao
) {

    fun insertContactsToIntoDB(contactsData: ArrayList<ContactsEntityClass>) {
        contactsData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            dao.insert(it)
        }
    }

    fun getContactsFromLocal(): Single<List<ContactsEntityClass>> {
        return dao.getAllContacts()
    }

    fun removeContacts() {
        return dao.deleteAll()
    }

    //All Chat Entity Class

    fun insertAllChatListToIntoDB(contactsData: ArrayList<AllChatEntityClass>) {
        contactsData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            daoChats.insert(it)
        }
    }

    fun updateItemToDB(unix_time: String, message: String, uid: Int) {
        daoChats.updateUnixTime(unix_time, message, uid)
    }

    fun getAllChatListFromDB(): Single<List<AllChatEntityClass>> {
        return daoChats.getAllContacts()
    }
}