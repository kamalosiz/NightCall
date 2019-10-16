package com.example.kalam_android.base

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.example.kalam_android.di.component.AppComponents
import com.example.kalam_android.di.component.DaggerAppComponents
import com.example.kalam_android.di.modules.AppModule

class MyApplication : MultiDexApplication() {
    lateinit var component: AppComponents

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponents.builder().appModule(AppModule(this)).build()
    }

    companion object {

        fun getAppComponent(context: Context): AppComponents {
            return (context.applicationContext as MyApplication).component
        }
    }
}