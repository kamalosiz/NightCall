package com.example.kalam_android.di.component

import com.example.kalam_android.di.modules.AppModule
import com.example.kalam_android.di.modules.UtilsModule
import com.example.kalam_android.di.modules.ViewModelsModule
import com.example.kalam_android.view.LoginActivity
import com.example.kalam_android.view.SignUpActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UtilsModule::class, ViewModelsModule::class, AppModule::class])
interface AppComponents {

    fun doInjection(activity: LoginActivity)
    fun doInjection(activity: SignUpActivity)
}