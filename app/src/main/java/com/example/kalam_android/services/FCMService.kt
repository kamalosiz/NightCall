package com.example.kalam_android.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.activities.SplashActivity
import com.example.kalam_android.webrtc.CustomWebSocketClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.URI
import javax.inject.Inject

class FCMService : FirebaseMessagingService() {
    val TAG = this.javaClass.simpleName
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onMessageReceived(remoteMSG: RemoteMessage) {

        logE("onMessageReceived ${remoteMSG.data}")
        if (remoteMSG.data["nType"] == "call") {
//            showNotification("Call Notification", "Call is received")
            (this.application as MyApplication).component.doInjection(this)
            try {
                val customWebSocketClient =
                    CustomWebSocketClient.getInstance(
                        sharedPrefsHelper, this,
                        URI(Urls.WEB_SOCKET_URL)
                    )
                customWebSocketClient.setConnectTimeout(10000)
                customWebSocketClient.setReadTimeout(60000)
                customWebSocketClient.enableAutomaticReconnection(5000)
                customWebSocketClient.connect()
                customWebSocketClient.setPushData(
                    remoteMSG.data["connectedUserId"].toString(),
                    true
                )
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        } else {
            if (Integer.valueOf(remoteMSG.data[AppConstants.FIREBASE_CHAT_ID].toString()) != Global.currentChatID) {
                showNotification(remoteMSG)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logE("onNewToken $token")
        (this.application as MyApplication).component.doInjection(this)
        sharedPrefsHelper.saveFCMToken(token)
        sharedPrefsHelper.saveIsNewFcmToken(true)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val body: String? = remoteMessage.notification?.body
        val title: String? = remoteMessage.notification?.title
        val chatID: Int =
            Integer.valueOf(remoteMessage.data[AppConstants.FIREBASE_CHAT_ID].toString())

        logE("chatId: $chatID")
        var intent = Intent(this, SplashActivity::class.java)
        if (isAppRunning(this, packageName)) {
            val msgId = remoteMessage.data["id"].toString()
            val callerID = remoteMessage.data["sender_id"].toString()
            val image = remoteMessage.data["profile_image"].toString()

            logE("FCM data: sender_id$callerID masgId:$msgId profile_image:$image")
            intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra(AppConstants.IS_FROM_OUTSIDE, false)
            intent.putExtra(AppConstants.CHAT_ID, chatID)
            intent.putExtra(
                AppConstants.CHAT_USER_NAME,
                remoteMessage.data[AppConstants.SENDER_NAME]
            )
            intent.putExtra(AppConstants.IS_CHATID_AVAILABLE, true)
//            intent.putExtra(AppConstants.MSG_ID, 0)
            intent.putExtra(AppConstants.CALLER_USER_ID, callerID.toInt())
            intent.putExtra(AppConstants.CHAT_USER_PICTURE, image)

        }
        val notificationType = "Kalam Messages"

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

        val mBuilder: NotificationCompat.Builder

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannels(mNotificationManager, notificationType)
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder = NotificationCompat.Builder(this, notificationType)
            .setContentTitle(title)
            .setSmallIcon(getNotificationIcon())
            .setContentText(body)
            .setColor(Color.parseColor("#179a63"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
//            .setGroup(title)
//            .setGroupSummary(true)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        mNotificationManager.notify(chatID, mBuilder.build())
    }

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.drawable.ic_notification else R.drawable.app_icon
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels(notification: NotificationManager, type: String) {
        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)
        val adminChannel = NotificationChannel(
            type,
            type,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notification.createNotificationChannel(adminChannel)
    }

    private fun showNotification(title: String, task: String) {
        logE("Showing Notification")
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Call Notification",
                "Call Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "Call Notification")
                .setContentTitle(title)
                .setSmallIcon(getNotificationIcon())
                .setContentText(task)
                .setColor(Color.parseColor("#179a63"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, mBuilder.build())
        logE("Showing Notification done")
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

}
