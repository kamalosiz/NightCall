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
        Debugger.e("testingSocket", "token : ${opts.query}")
        Debugger.e("testingSocket", "token : $token")
        socket = IO.socket(Urls.BASE_URL, opts)
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Debugger.e(TAG, "==============================CONNECTED")
        }?.on(Socket.EVENT_DISCONNECT) {
            Debugger.e(TAG, "==============================OFF")

        }?.on(AppConstants.NEW_MESSAGE) {

            val jsonObject = it[0] as JSONObject
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
        socket?.off(AppConstants.NEW_MESSAGE)
        socket?.off(AppConstants.MESSAGE_TYPING)
        socket?.off(AppConstants.MESSAGE_STOPS_TYPING)
        socket?.disconnect()
    }

    fun typingEvent(action: String, userId: String, chatId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("chat_id", chatId)
        socket?.emit(action, jsonObject)
    }

    fun updateSettings(action: String, autoTranslate: String, language: String, id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("auto_translate", autoTranslate)
        jsonObject.addProperty("language", language)
        jsonObject.addProperty("user_id", id)
        socket?.emit(action, jsonObject)
    }

    fun setListener(newMessageListener: NewMessageListener) {
        this.newMessageListener = newMessageListener
    }

    fun setTypingListener(messageTypingResponse: MessageTypingListener) {
        this.messageTypingResponse = messageTypingResponse
    }

    fun emitNewMessage(
        id: String,
        chatID: String,
        message: String,
        type: String,
        senderName: String,
        fileID: String,
        duration: Long,
        thumbnail: String
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", id)
        jsonObject.addProperty("chat_id", chatID)
        jsonObject.addProperty("message", message)
        jsonObject.addProperty("mType", type)
        jsonObject.addProperty("sender_name", senderName)
        jsonObject.addProperty("file_id", fileID)
        jsonObject.addProperty("duration", duration)
        jsonObject.addProperty("thumbnail", thumbnail)
        socket?.emit(AppConstants.SEND_MESSAGE, jsonObject)
    }
}