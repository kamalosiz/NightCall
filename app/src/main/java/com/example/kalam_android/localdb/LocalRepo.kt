package com.example.kalam_android.localdb

import android.util.Log
import com.example.kalam_android.localdb.dao.AllChatListDao
import com.example.kalam_android.localdb.dao.ChatDataDao
import com.example.kalam_android.localdb.dao.ContactsDao
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.localdb.entities.ContactsData
import com.example.kalam_android.util.Debugger
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepo @Inject constructor(
    private val dao: ContactsDao,
    private val daoChats: AllChatListDao,
    private val chatDataDao: ChatDataDao
) {
    fun insertContactsToIntoDB(contactsData: ArrayList<ContactsData>) {
        contactsData.let {
            Debugger.e("insertContactsToIntoDB", "insertContactsToIntoDB: $it")
            dao.insert(it)
        }
    }

    fun getContactsFromLocal(): Single<List<ContactsData>> {
        return dao.getAllContacts()
    }

    fun removeContacts() {
        return dao.deleteAll()
    }

    //All Chat Entity Class

    fun insertAllChatListToIntoDB(chatData: ArrayList<ChatListData>) {
        chatData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            daoChats.insertChat(it)
        }
    }

    fun insertChat(chatListData: ChatListData) {
        daoChats.insertChat(chatListData)
    }

    fun updateChatItemDB(uid: Int, unReadcount: Int, isRead: Int?) {
        daoChats.updateChatItemDB(uid, unReadcount, isRead)
    }

    fun updateChatItemDB(uid: Int, isRead: Int?) {
        daoChats.updateChatItemDB(uid, isRead)
    }

    fun updateItemToDB(
        unix_time: String,
        message: String,
        uid: Int,
        unReadcount: Int,
        sender_id: Int?,
        isRead: Int?
    ) {
        daoChats.updateItem(unix_time, message, uid, unReadcount, sender_id, isRead)
    }

    fun getAllChatListFromDB(): Single<List<ChatListData>> {
        return daoChats.getAllContacts()
    }

    fun removeChats() {
        return daoChats.deleteAll()
    }

    fun insertChatsIntoDB(chatData: ArrayList<ChatData>) {
        chatData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            return chatDataDao.insert(it)
        }
    }

    fun insertChatsIntoDB(chatData: ChatData) {
        chatDataDao.insert(chatData)
    }
}