package com.example.kalam_android.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import androidx.room.Room
import com.example.kalam_android.localdb.dao.AllChatListDao
import com.example.kalam_android.localdb.dao.ContactsDao
import com.example.kalam_android.localdb.RoomDB
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.util.AppConstants


@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    internal fun provideContext(): Context {
        return context
    }

    @Provides
    internal fun provideSharedPrefs(): SharedPreferences {
        return context.getSharedPreferences("kalam-prefs", Context.MODE_PRIVATE)
    }

    @Provides
    internal fun getResources(): Resources {
        return context.resources
    }

    @Singleton
    @Provides
    fun provideRoomDB(context: Context): RoomDB {
        return Room.databaseBuilder(
            context,
            RoomDB::class.java, AppConstants.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideShowDao(roomDb: RoomDB): ContactsDao {
        return roomDb.contactsDao()
    }

    @Singleton
    @Provides
    fun provideAllChatDao(roomDb: RoomDB): AllChatListDao {
        return roomDb.allChatListDao()
    }

    @Provides
    @Singleton
    fun getLocalRepo(contactsDao: ContactsDao, allChatListDao: AllChatListDao): LocalRepo {
        return LocalRepo(contactsDao, allChatListDao)
    }

}