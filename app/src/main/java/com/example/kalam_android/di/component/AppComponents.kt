package com.example.kalam_android.di.component

import com.example.kalam_android.di.modules.AppModule
import com.example.kalam_android.di.modules.UtilsModule
import com.example.kalam_android.di.modules.ViewModelsModule
import com.example.kalam_android.notification.FCMService
import com.example.kalam_android.view.activities.*
import com.example.kalam_android.view.fragments.ChatsFragment
import com.example.kalam_android.view.fragments.MoreFragment
import com.example.kalam_android.view.fragments.ProfileFragment
import com.example.kalam_android.webrtc.CallActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [UtilsModule::class, ViewModelsModule::class, AppModule::class])
interface AppComponents {

    fun doInjection(activity: SplashActivity)
    fun doInjection(activity: LoginActivity)
    fun doInjection(activity: SignUpActivity)
    fun doInjection(activity: VerifyCodeActivity)
    fun doInjection(activity: CreateProfileActivity)
    fun doInjection(activity: ContactListActivity)
    fun doInjection(activity: WelcomeActivity)
    fun doInjection(activity: MainActivity)
    fun doInjection(activity: ChatDetailActivity)
    fun doInjection(fragment: ChatsFragment)
    fun doInjection(activity: FindsFriendsActivity)
    fun doInjection(fragment: MoreFragment)
    fun doInjection(activity: SettingActivity)
    fun doInjection(fragment: ProfileFragment)
    fun doInjection(fcm: FCMService)
    fun doInjection(activity: CallActivity)
}