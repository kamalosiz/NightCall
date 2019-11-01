package com.example.kalam_android.wrapper

import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.NewMessageListener
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.JsonObject
import org.json.JSONObject


object SocketIO {
    private val TAG = this.javaClass.simpleName
    var socket: Socket? = null
    private var newMessageListener: NewMessageListener? = null
    private var messageTypingResponse: MessageTypingListener? = null

    fun connectSocket(token: String?) {
        val opts = IO.Options()
        opts.query = "token=$token"
        socket = IO.socket(Urls.BASE_URL, opts)
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Debugger.e(TAG, "==============================CONNECTED")
        }?.on(Socket.EVENT_DISCONNECT) {
            Debugger.e(TAG, "==============================OFF")

        }?.on(AppConstants.NEW_MESSAGE) {

            val jsonObject = it[0] as JSONObject
//            if (newMessageListener != null)
                newMessageListener?.socketResponse(jsonObject)

        }?.on(AppConstants.MESSAGE_TYPING) {

            val jsonObject = it[0] as JSONObject
            messageTypingResponse?.typingResponse(jsonObject, true)

        }?.on(AppConstants.MESSAGE_STOPS_TYPING) {

            val jsonObject = it[0] as JSONObject
            messageTypingResponse?.typingResponse(jsonObject, false)

        }
    }

    fun disconnectSocket() {
        socket?.disconnect()
    }

    fun typingEvent(action: String, userId: String, chatId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("chat_id", chatId)
        socket?.emit(action, jsonObject)
    }

    fun setListener(newMessageListener: NewMessageListener) {
        this.newMessageListener = newMessageListener
    }

    fun setTypingListener(messageTypingResponse: MessageTypingListener) {
        this.messageTypingResponse = messageTypingResponse
    }
}