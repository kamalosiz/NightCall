package com.example.kalam_android.webrtc

import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


class CustomWebSocketListener(val sharedPrefsHelper: SharedPrefsHelper) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private var webSocketCallback: WebSocketCallback? = null
    private var webSocketOfferCallback: WebSocketOfferCallback? = null
    private val TAG = this.javaClass.simpleName

    companion object {
        private var instance: CustomWebSocketListener? = null
        @Synchronized
        fun getInstance(sharedPrefsHelper: SharedPrefsHelper): CustomWebSocketListener {
            if (instance == null) {
                instance = CustomWebSocketListener(sharedPrefsHelper)
            }
            return instance as CustomWebSocketListener
        }
    }

    fun setSocketCallback(webSocketCallback: WebSocketCallback?) {
        this.webSocketCallback = webSocketCallback
    }

    fun setOfferListener(webSocketOfferCallback: WebSocketOfferCallback) {
        this.webSocketOfferCallback = webSocketOfferCallback
    }

    fun setWebSocket(webSocket: WebSocket) {
        this.webSocket = webSocket
    }


    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        logE("onOpen $response")
        login()
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        logE("onMessage string" + text!!)
        try {
            val json = JSONObject(text)
            if (json.getString("type") == "offer") {
                webSocketOfferCallback?.offerCallback(json)
            } else {
                webSocketCallback?.webSocketCallback(json)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        logE("onMessage bytes " + bytes!!)
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        logE("onClosing " + reason!!)
    }

    override fun onFailure(webSocket: WebSocket?, t: Throwable, response: Response?) {
        println("Error : " + t.message)
        logE("onFailure " + t.message)
    }

    fun login() {
        val json = JSONObject()
        try {
            json.put("type", "login")
            json.put(
                "name",
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                )
            )
            json.put("userId", sharedPrefsHelper.getUser()?.id)
            json.put("phone", "")
            json.put("photoUrl", sharedPrefsHelper.getUser()?.profile_image.toString())
            webSocket?.send(json.toString())
            logE("login successfully")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun onIceCandidateReceived(iceCandidate: IceCandidate, callerID: Long) {
        logE("onIceCandidateReceived normal :$iceCandidate")
        val obj = JSONObject()
        try {
            obj.put("type", "candidate")
            obj.put("label", iceCandidate.sdpMLineIndex)
            obj.put("id", iceCandidate.sdpMid)
            obj.put("candidate", iceCandidate.sdp)
            obj.put("connectedUserId", callerID)
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun createOffer(sessionDescription: SessionDescription, callerID: Long) {
        try {
            logE("Emit Description [ $sessionDescription]")
            val obj = JSONObject()
            obj.put("type", sessionDescription.type.canonicalForm())
            obj.put("sdp", sessionDescription.description)
            obj.put("candidate", "")
            obj.put("connectedUserId", callerID)
            webSocket?.send(obj.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun doAnswer(sessionDescription: SessionDescription, callerID: Long) {
        try {
            val obj = JSONObject()
            obj.put("type", sessionDescription.type.canonicalForm())
            obj.put("sdp", sessionDescription.description)
            obj.put("candidate", "")
            obj.put("connectedUserId", callerID)
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}