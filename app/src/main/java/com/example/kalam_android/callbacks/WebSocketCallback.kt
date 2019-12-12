package com.example.kalam_android.callbacks

import org.json.JSONObject

interface WebSocketCallback {
    fun webSocketCallback(jsonObject: JSONObject)
}