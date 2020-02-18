package com.example.kalam_android.wrapper

import com.example.kalam_android.callbacks.LocationsCallback
import com.example.kalam_android.callbacks.MessageTypingListener
import com.example.kalam_android.callbacks.SocketCallback
import com.example.kalam_android.callbacks.StatusCallback
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.github.nkzawa.socketio.client.Ack
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

class SocketIO private constructor() {
    private val TAG = this.javaClass.simpleName
    var socketCallback: SocketCallback? = null
    var locationCallback: LocationsCallback? = null
    private var statusCallback: StatusCallback? = null
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
            Debugger.e("SocketIO", "SEEN_MESSAGE :$json")
            socketCallback?.socketResponse(json, AppConstants.SEEN_MESSAGE)

        }?.on(AppConstants.USER_STATUS) {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.USER_STATUS)
        }?.on(AppConstants.MESSAGE_DELETED) {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.MESSAGE_DELETED)
        }?.on(AppConstants.EDIT_MESSAGE) {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.EDIT_MESSAGE)
        }?.on(AppConstants.SEND_LOCATION) {
            val json = it[0] as JSONObject
            locationCallback?.onLocationCallback(json)
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

    fun setLocationsCallback(locationsCallback: LocationsCallback) {
        this.locationCallback = locationsCallback
    }

    fun setStatusCallbackListener(statusCallback: StatusCallback) {
        this.statusCallback = statusCallback
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
        jsonObject.addProperty("is_group", 0)
        Debugger.e("emitNewMessage", "jsonObject :$jsonObject")
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
        Debugger.e("SocketIO", "Message seen and emmited to socket:$jsonObject")
        socket?.emit(AppConstants.MESSAGE_SEEN, jsonObject)
    }

    fun emitGetNickName(userId: String, contactId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("contact_id", contactId)
        socket?.emit(AppConstants.GET_MY_NICKNAME, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.GET_MY_NICKNAME)
        })
    }

    fun checkUserStatus(userId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", userId)
        socket?.emit(AppConstants.CHECK_USER_STATUS, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.CHECK_USER_STATUS)
        })
    }

    fun getUserStatuses(json: JsonArray) {
        socket?.emit(AppConstants.GET_ALL_USER_STATUS, json, Ack {
            val list = it[0] as JSONArray
            statusCallback?.onStatusCallback(list)
        })
    }

    fun emitDeleteMsg(chatID: String, msgId: String, receiver_id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("chat_id", chatID)
        jsonObject.addProperty("msg_id", msgId)
        jsonObject.addProperty("receiver_id", receiver_id)
        Debugger.e("testing", "emitDeleteMsg:$jsonObject")
        socket?.emit(AppConstants.DELETE_MESSAGE, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.DELETE_MESSAGE)
        })
    }

    fun emitEditMsg(message_id: String, chatId: String, message: String, receiver_id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("message_id", message_id)
        jsonObject.addProperty("chatId", chatId)
        jsonObject.addProperty("isGroupChat", false)
        jsonObject.addProperty("message", message)
        jsonObject.addProperty("receiver_id", receiver_id)
        socket?.emit(AppConstants.EDIT_MESSAGE, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.EDIT_MESSAGE)
        })
    }

    fun emitEditLocation(
        message_id: Int,
        chatId: Int,
        receiver_id: Int,
        lat: Double,
        long: Double
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("message_id", message_id)
        jsonObject.addProperty("chatId", chatId)
        jsonObject.addProperty("receiver_id", receiver_id)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("long", long)
        socket?.emit(AppConstants.SEND_LOCATION, jsonObject)
    }

    fun emitForwardMessage(message_id: String, receiver_id: String, sender_id: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("message_id", message_id)
        jsonObject.addProperty("receiver_id", receiver_id)
        jsonObject.addProperty("sender_id", sender_id)
        socket?.emit(AppConstants.FORWARD_MESSAGE, jsonObject, Ack {
            val json = it[0] as JSONObject
            socketCallback?.socketResponse(json, AppConstants.FORWARD_MESSAGE)
        })
    }
}