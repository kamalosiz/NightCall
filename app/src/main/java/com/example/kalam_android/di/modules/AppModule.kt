package com.example.kalam_android.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import androidx.room.Room
import com.example.kalam_android.localdb.ContactsDao
import com.example.kalam_android.localdb.RoomDB
import com.example.kalam_android.repository.LocalRepo
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

    @Provides
    @Singleton
    fun getLocalRepo(contactsDao: ContactsDao): LocalRepo {
        return LocalRepo(contactsDao)
    }

}