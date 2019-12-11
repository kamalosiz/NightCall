package com.example.kalam_android.notification

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
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.Global
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.activities.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FCMService : FirebaseMessagingService() {
    val TAG = "FirebaseMessaging"
    override fun onMessageReceived(remoteMSG: RemoteMessage) {
//        logE("remoteMSG data: ${remoteMSG.data}")
//        logE("remoteMSG notification: ${remoteMSG.notification}")
        if (Integer.valueOf(remoteMSG.data[AppConstants.FIREBASE_CHAT_ID].toString()) != Global.currentChatID) {
            showNotification(remoteMSG)
        }
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        var body: String? = ""
        var title: String? = ""
        var chatID = -1
        try {
            body = remoteMessage.notification?.body
            title = remoteMessage.notification?.title
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            chatID =
                Integer.valueOf(remoteMessage.data[AppConstants.FIREBASE_CHAT_ID].toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logE("chatId: $chatID")
        var intent = Intent(this, SplashActivity::class.java)
        if (isAppRunning(this, packageName)) {
            intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra(AppConstants.IS_FROM_OUTSIDE, false)
            intent.putExtra(AppConstants.CHAT_ID, chatID)
            intent.putExtra(
                AppConstants.CHAT_USER_NAME,
                remoteMessage.data[AppConstants.SENDER_NAME]
            )
            intent.putExtra(AppConstants.IS_CHATID_AVAILABLE, true)
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
            .setGroup(title)
            .setGroupSummary(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

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
        val adminChannelName = getString(R.string.notifications_admin_channel_name)
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
}


/* .setStyle(
     NotificationCompat.BigPictureStyle()
//                    .bigPicture(bigBitmap)
         .setSummaryText(body)
         .setBigContentTitle(title)
 )*/