package com.example.kalam_android.webrtc

import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
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
            if (json.getString(AppConstants.TYPE) == AppConstants.OFFER) {
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

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logE("onClosed $reason")
    }

    override fun onFailure(webSocket: WebSocket?, t: Throwable, response: Response?) {
        println("Error : " + t.message)
        logE("onFailure " + t.message)
    }

    fun login() {
        val json = JSONObject()
        try {
            json.put("type", AppConstants.LOGIN)
            json.put(
                "name",
                StringBuilder(sharedPrefsHelper.getUser()?.firstname.toString()).append(" ").append(
                    sharedPrefsHelper.getUser()?.lastname.toString()
                )
            )
            json.put("userId", sharedPrefsHelper.getUser()?.id.toString())
            json.put("phone", "")
            json.put("photoUrl", sharedPrefsHelper.getUser()?.profile_image.toString())
            json.put("deviceType", "android")
            json.put("token", sharedPrefsHelper.getFCMToken().toString())
            webSocket?.send(json.toString())
            logE("login successfully")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun onIceCandidateReceived(iceCandidate: JSONObject, callerID: String) {
        logE("onIceCandidateReceived normal :$iceCandidate")
        val obj = JSONObject()
        try {
            obj.put("type", AppConstants.CANDIDATE)
            obj.put("candidate", iceCandidate)
            obj.put("connectedUserId", callerID)
            logE("ICE Candidates $obj")
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onIceCandidateReceived JSONException ${e.message}")
            e.printStackTrace()
        }
    }

    fun createOffer(sessionDescription: SessionDescription, callerID: String, isVideo: Boolean) {
        try {
            logE("Emit Description [ $sessionDescription]")
            val obj = JSONObject()
            obj.put("type", sessionDescription.type.canonicalForm())
            obj.put("offer", JSONObject().put("sdp", sessionDescription.description))
            obj.put("connectedUserId", callerID)
            obj.put("isVideo", isVideo)
            logE("createOffer $obj")
            webSocket?.send(obj.toString())

        } catch (e: JSONException) {
            logE("createOffer JSONException ${e.message}")
            e.printStackTrace()
        }
    }

    fun doAnswer(sessionDescription: SessionDescription, callerID: String) {
        try {
            val obj = JSONObject()
            obj.put("type", sessionDescription.type.canonicalForm())
            obj.put("offer", JSONObject().put("sdp", sessionDescription.description))
            obj.put("connectedUserId", callerID)
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    fun onHangout(id: String) {
        try {
            val obj = JSONObject()
            obj.put("type", "reject")
            obj.put("connectedUserId", id)
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    fun onAvailable(id: String) {
        try {
            val obj = JSONObject()
            obj.put("type", "available")
            obj.put("connectedUserId", id)
            webSocket?.send(obj.toString())
            logE("onAvailable sent $obj")
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    /*fun disconnectSocket() {
        webSocket?.close(1,"")
    }*/

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}