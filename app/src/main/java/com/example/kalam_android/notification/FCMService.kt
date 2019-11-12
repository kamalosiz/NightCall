package com.example.kalam_android.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
 val TAG = "FirebaseMessagingService"

 override fun onMessageReceived(p0: RemoteMessage?) {
  super.onMessageReceived(p0)
 }
}