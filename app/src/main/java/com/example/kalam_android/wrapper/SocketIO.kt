package com.example.kalam_android.wrapper

import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.github.nkzawa.socketio.client.Ack
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.JsonObject
import org.json.JSONObject

class SocketIO private constructor() {
    private val TAG = this.javaClass.simpleName
    private var socketCallback: SocketCallback? = null
    private var messageTypingResponse: MessageTypingListener? = null
    var socket: Socket? = null

    companion object {
        private var instance: SocketIO? = null

        @Synchronized
        fun getInstance(): SocketIO {
            if (instance == null) {
                instance = SocketIO()
            }
            return instance as SocketIO
        }
    }

    fun connectSocket(token: String?) {
        val opts = IO.Options()
        opts.forceNew = true
        opts.query = "token=$token"
        socket = IO.socket(Urls.BASE_URL, opts)
        socket?.connect()
    }

    fun connectListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Debugger.e(TAG, "==============CONNECTED==============")
        }?.on(Socket.EVENT_DISCONNECT) {
            Debugger.e(TAG, "===============OFF===============")

        }?.on(AppConstants.NEW_MESSAGE) {

            val jsonObject = it[0] as JSONObject
            socketCallback?.socketResponse(jsonObject, AppConstants.NEW_MESSAGE)

        }?.on(AppConstants.MESSAGE_TYPING) {

            val jsonObject = it[0] as JSONObject
            Debugger.e("SocketIO", "MESSAGE_TYPING :$jsonObject")
            messageTypingResponse?.typingResponse(jsonObject, true)

        }?.on(AppConstants.MESSAGE_STOPS_TYPING) {

            val jsonObject = it[0] as JSONObject
            messageTypingResponse?.typingResponse(jsonObject, false)

        }?.on(AppConstants.ALL_MESSAGES_READ) {

            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.ALL_MESSAGES_READ)

        }?.on(AppConstants.MESSAGE_DELIVERED) {

            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.MESSAGE_DELIVERED)

        }?.on(AppConstants.SEEN_MESSAGE) {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.SEEN_MESSAGE)
        }
    }

    fun disconnectSocket() {
        socket?.disconnect()
        socket?.off(AppConstants.NEW_MESSAGE)
        socket?.off(AppConstants.MESSAGE_TYPING)
        socket?.off(AppConstants.MESSAGE_STOPS_TYPING)
        socket?.off(AppConstants.ALL_MESSAGES_READ)
        socket?.off(AppConstants.MESSAGE_DELIVERED)
        socket?.off(AppConstants.SEEN_MESSAGE)
        socket?.close()
        instance = null
        Debugger.e("SocketIO", "Socket disconnectSocket")
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

    fun setSocketCallbackListener(socketCallback: SocketCallback) {
        this.socketCallback = socketCallback
    }

    fun setTypingListeners(messageTypingResponse: MessageTypingListener) {
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
        thumbnail: String,
        identifier: String,
        language: String,
        groupID: String,
        profileImage: String
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
        jsonObject.addProperty("identifier", identifier)
        jsonObject.addProperty("language", language)
        jsonObject.addProperty("group_id", groupID)
        jsonObject.addProperty("profile_image", profileImage)
        socket?.emit(AppConstants.SEND_MESSAGE, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.SEND_MESSAGE)
        })
    }

    fun emitReadAllMessages(chatID: String, userId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("chat_id", chatID)
        jsonObject.addProperty("user_id", userId)
        socket?.emit(AppConstants.READ_ALL_MESSAGES, jsonObject)
    }

    fun emitMessageSeen(chatId: String, msgId: String, userId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("chat_id", chatId)
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("message_id", msgId)
        socket?.emit(AppConstants.MESSAGE_SEEN, jsonObject)
    }
}