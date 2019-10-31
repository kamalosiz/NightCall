package com.example.kalam_android.callbacks

import com.google.gson.JsonObject

interface NewMessageListener {
    fun newMessage(jsonObject: JsonObject)
}