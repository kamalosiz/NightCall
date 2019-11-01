package com.example.kalam_android.callbacks

import org.json.JSONObject

interface MessageTypingListener {
    fun typingResponse(jsonObject: JSONObject, isTyping: Boolean)
}