package com.example.kalam_android.localdb

import android.util.Log
import com.example.kalam_android.localdb.dao.AllChatListDao
import com.example.kalam_android.localdb.dao.ChatDataDao
import com.example.kalam_android.localdb.dao.ContactsDao
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.localdb.entities.ContactsEntityClass
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepo @Inject constructor(
    private val dao: ContactsDao,
    private val daoChats: AllChatListDao,
    private val chatDataDao: ChatDataDao
) {

    fun insertContactsToIntoDB(contactsData: ArrayList<ContactsEntityClass>) {
        contactsData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            dao.insert(it)
        }
    }

    //Testing
    /* fun insertListIntoDB(data: ArrayList<*>, type: String) {
         when (type) {
             "chats" -> {
                 data.let {
                     dao.insert(it as ArrayList<ContactsEntityClass>)
                 }
             }
             "contacts" -> {

             }
         }
     }*/
    //

    fun getContactsFromLocal(): Single<List<ContactsEntityClass>> {
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

    fun updateItemToDB(
        unix_time: String,
        message: String,
        uid: Int,
        unReadcount: Int,
        sender_id: Int?
    ) {
        daoChats.updateItem(unix_time, message, uid, unReadcount, sender_id)
    }

    fun updateReadCountDB(uid: Int, unReadcount: Int) {
        daoChats.updateReadCountDB(uid, unReadcount)
    }

    fun getAllChatListFromDB(): Single<List<ChatListData>> {
        return daoChats.getAllContacts()
    }

    fun removeChats() {
        return daoChats.deleteAll()
    }

    //ChatsData

    fun inserChatsIntoDB(chatData: ArrayList<ChatData>) {
        chatData.let {
            Log.i("testingLocal", "insertContactsToIntoDB: $it")
            return chatDataDao.insert(it)
        }
    }

    fun inserChatsIntoDB(chatData: ChatData) {
        chatDataDao.insert(chatData)
    }
}