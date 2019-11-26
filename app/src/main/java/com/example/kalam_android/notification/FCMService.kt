package com.example.kalam_android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kalam_android.R
import com.example.kalam_android.repository.model.ChatData
import com.example.kalam_android.repository.model.NotificationResponse
import com.example.kalam_android.util.Debugger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*

class FCMService : FirebaseMessagingService() {
    val TAG = "FirebaseMessaging"
    private val ADMIN_CHANNEL_ID = "com.example.kala_android"
    private lateinit var notificationManager: NotificationManager

    override fun onMessageReceived(remoteMSG: RemoteMessage) {
        Debugger.e(TAG, "Notification Received: ${remoteMSG.data}")
        /*val gson = Gson()
        val jsonObject = p0[0] as JSONObject
        val data = gson.fromJson(jsonObject.toString(), NotificationResponse::class.java)
        *//* if (p0.notification != null) {
             showNotification(p0.notification?.title, p0.notification?.body)
         }*//*
        if (data != null) {*/
        val name = remoteMSG.data["sender_name"]
        val message = remoteMSG.data["message"]
        showNotification(name, message)
//        }
    }


    private fun showNotification(title: String?, body: String?) {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannels(notificationManager)
        }
        val notificationId = Random().nextInt(60000)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)

//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels(notification: NotificationManager) {
        val adminChannelName = getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notification.createNotificationChannel(adminChannel)
    }
}