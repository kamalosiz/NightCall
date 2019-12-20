package com.example.kalam_android.base

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.example.kalam_android.BuildConfig
import com.example.kalam_android.R
import com.example.kalam_android.di.component.AppComponents
import com.example.kalam_android.di.component.DaggerAppComponents
import com.example.kalam_android.di.modules.AppModule
import com.example.kalam_android.util.Debugger
import org.acra.ACRA
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes


@ReportsCrashes(
    formUri = "http://suavesol.net/evolve_crash_log/",
    mailTo = "waqarmustafa18@gmail.com",
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.crash_report
)
class MyApplication : MultiDexApplication() {
    lateinit var component: AppComponents

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponents.builder().appModule(AppModule(this)).build()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            Debugger.e("acra", "ACRA init")
            ACRA.init(this)
            Debugger.e("acra", "ACRA Configured")
        }
    }

    companion object {

        fun getAppComponent(context: Context): AppComponents {
            return (context.applicationContext as MyApplication).component
        }
    }
}