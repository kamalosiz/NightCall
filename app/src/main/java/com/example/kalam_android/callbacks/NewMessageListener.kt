package com.example.kalam_android.callbacks

import org.json.JSONObject

interface NewMessageListener {
    fun socketResponse(jsonObject: JSONObject)
}