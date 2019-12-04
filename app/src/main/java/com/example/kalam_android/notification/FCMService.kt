package com.example.kalam_android.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kalam_android.R
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.view.activities.ChatDetailActivity
import com.example.kalam_android.view.activities.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FCMService : FirebaseMessagingService() {
    val TAG = "FirebaseMessaging"
    override fun onMessageReceived(remoteMSG: RemoteMessage) {
        Debugger.e(TAG, "Notification Received: ${remoteMSG.data}")
        Debugger.e(TAG, "Notification Received: ${remoteMSG.notification}")
//        showNotification(remoteMSG)
    }

    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {

        logE("chatId: " + remoteMessage.data[AppConstants.FIREBASE_CHAT_ID])
        logE("notificationType: " + remoteMessage.data[AppConstants.NOTIFICATION_TYPE])
        logE("content_type: " + remoteMessage.data[AppConstants.CONTENT_TYPE])
        var notifyId = -1
        try {
            notifyId = Integer.valueOf(remoteMessage.data[AppConstants.FIREBASE_CHAT_ID].toString())
        } catch (e: NumberFormatException) {

        }

        var body: String? = ""
        var title: String? = ""
        try {
            body = remoteMessage.notification?.body
//            body = remoteMessage.data[AppConstants.NOTIFICATION_BODY]
            logE("notification body $body")
        } catch (e: Exception) {

        }
        try {
            title = remoteMessage.notification?.title
//            title = remoteMessage.data[AppConstants.SENDER_NAME]
            logE("notification body $title")
        } catch (e: Exception) {

        }
        val intent = Intent(this, ChatDetailActivity::class.java)
        intent.putExtra(AppConstants.CHAT_ID, notifyId)
        intent.putExtra(AppConstants.IS_FROM_CHAT_FRAGMENT, true)
        intent.putExtra(AppConstants.IS_FROM_CHAT_OUTSIDE, true)
        intent.putExtra(
            AppConstants.CHAT_USER_NAME,
            remoteMessage.data[AppConstants.SENDER_NAME]
        )
//        intent.putExtra(AppConstants.CHAT_USER_PICTURE, item.profile_image)
//        val notificationType = remoteMessage.data[AppConstants.NOTIFICATION_TYPE]
        val notificationType = "Kalam Messages"

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                notifyId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(
                if (!isAppRunning(this, packageName)) pendingIntent else null
            )
            .setSound(defaultSoundUri)
            .setGroup(title)
            .setGroupSummary(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        mNotificationManager.notify(notifyId, mBuilder.build())
    }

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.drawable.ic_notification else R.drawable.app_icon
    }

    fun isAppRunning(context: Context, packageName: String): Boolean {
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
            NotificationManager.IMPORTANCE_HIGH
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