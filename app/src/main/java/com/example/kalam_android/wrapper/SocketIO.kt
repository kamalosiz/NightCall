package com.example.kalam_android.wrapper

import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject


object SocketIO {
    private val TAG = this.javaClass.simpleName
    var socket: Socket? = null

    fun connectSocket(token: String?) {
        val opts = IO.Options()
        opts.query = "token=$token"
        socket = IO.socket(Urls.BASE_URL, opts)
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Debugger.e(TAG, "==============================CONNECTED")
        }?.on(Socket.EVENT_DISCONNECT) {
            Debugger.e(TAG, "==============================OFF")
        }
    }

    fun chatInitiated() {
        socket?.on(AppConstants.CHAT_INITIATED) {
            val data = it[0] as JSONObject
            Debugger.e(TAG, "USER ID $data")
        }
    }


}