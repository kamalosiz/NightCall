package com.example.kalam_android.callbacks

import org.json.JSONObject

interface SocketCallback {
    fun socketResponse(jsonObject: JSONObject, type: String)
}