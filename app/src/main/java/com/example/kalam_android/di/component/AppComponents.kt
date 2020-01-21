package com.example.kalam_android.di.component

import com.example.kalam_android.di.modules.AppModule
import com.example.kalam_android.di.modules.UtilsModule
import com.example.kalam_android.di.modules.ViewModelsModule
import com.example.kalam_android.services.FCMService
import com.example.kalam_android.services.RxMediaWorker
import com.example.kalam_android.view.activities.*
import com.example.kalam_android.view.fragments.ChatsFragment
import com.example.kalam_android.view.fragments.MoreFragment
import com.example.kalam_android.view.fragments.ProfileFragment
import com.example.kalam_android.webrtc.AudioCallActivity
import com.example.kalam_android.webrtc.CustomWebSocketListener
import com.example.kalam_android.webrtc.VideoCallActivity
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
    fun doInjection(activityVideo: VideoCallActivity)
    fun doInjection(activityAudio: AudioCallActivity)
    fun doInjection(activityProfile: UserProfileActivity)
    fun doInjection(thread: RxMediaWorker)
    fun doInjection(activity: EditUserProfileActivity)
    fun doInjection(activity: ResetPasswordActivity)
}