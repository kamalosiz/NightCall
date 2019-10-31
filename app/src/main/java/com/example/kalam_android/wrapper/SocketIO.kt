package com.example.kalam_android.wrapper

import com.example.kalam_android.callbacks.NewMessageListener
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.JsonObject


object SocketIO {
    private val TAG = this.javaClass.simpleName
    var socket: Socket? = null

    fun connectSocket(token: String?) {
        val opts = IO.Options()
        opts.query = "token=$token"
        socket = IO.socket(Urls.BASE_URL, opts)
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Debugger.e(TAG, "==============================CONNECTED")
        }?.on(Socket.EVENT_DISCONNECT) {
            Debugger.e(TAG, "==============================OFF")
        }?.on(AppConstants.MESSAGE_TYPING) {
            val jsonObject = it[0] as JsonObject
            Debugger.e(TAG, "Some one is typing : $it")
            Debugger.e(TAG, "Some one is typing : $jsonObject")
        }
    }

    fun disconnectSocket() {
        socket?.disconnect()
    }

    fun checkSomeoneTyping(action: String, userId: String, chatId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("chat_id", chatId)
        socket?.emit(action, jsonObject)
    }

    fun checkNewMessage(newMessageListener: NewMessageListener) {
        socket?.on(AppConstants.NEW_MESSAGE) {
            val jsonObject = it[0] as JsonObject
            newMessageListener.newMessage(jsonObject)
        }
    }
}