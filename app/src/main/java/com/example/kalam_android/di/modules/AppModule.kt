package com.example.kalam_android.di.modules

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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

}