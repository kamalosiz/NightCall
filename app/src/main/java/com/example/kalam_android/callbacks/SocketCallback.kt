package com.example.kalam_android.callbacks

import org.json.JSONArray
import org.json.JSONObject

interface SocketCallback {
    fun socketResponse(jsonObject: JSONObject, type: String)
}

interface MessageTypingListener {
    fun typingResponse(jsonObject: JSONObject, isTyping: Boolean)
}

interface StatusCallback {
    fun onStatusCallback(array: JSONArray)
}

interface LocationsCallback {
    fun onLocationCallback(jsonObject: JSONObject)
}