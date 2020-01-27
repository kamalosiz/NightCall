package com.example.kalam_android.callbacks

import org.json.JSONArray

interface StatusCallback {
    fun onStatusCallback(array: JSONArray)
}