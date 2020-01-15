package com.example.kalam_android.webrtc

import android.content.Context
import android.content.Intent
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.MainActivity
import okhttp3.*
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.SessionDescription


class CustomWebSocketListener private constructor(val sharedPrefsHelper: SharedPrefsHelper) :
    WebSocketListener() {
    private var webSocket: WebSocket? = null
    private var webSocketCallback: WebSocketCallback? = null
    private var webSocketOfferCallback: WebSocketOfferCallback? = null
    private var isFromPush = false
    private var connectedUserId = ""
    private val TAG = this.javaClass.simpleName

    companion object {
        private var instance: CustomWebSocketListener? = null
        var context: Context? = null
        @Synchronized
        fun getInstance(sharedPrefsHelper: SharedPrefsHelper): CustomWebSocketListener {
            if (instance == null) {
                instance = CustomWebSocketListener(sharedPrefsHelper)
            }
            return instance as CustomWebSocketListener
        }

        @Synchronized
        fun getInstance(
            sharedPrefsHelper: SharedPrefsHelper,
            context: Context
        ): CustomWebSocketListener {
            if (instance == null) {
                this.context = context
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

    fun setPushData(connectedUserId: String, isFromPush: Boolean) {
        this.connectedUserId = connectedUserId
        this.isFromPush = isFromPush
    }


    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        logE("onOpen $response")
        login()
        if (isFromPush) {
//            isFromPush = false
            onReadyForCall(connectedUserId)
        }
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        logE("onMessage string" + text!!)
        try {
            val json = JSONObject(text)
            when {
                json.getString(AppConstants.TYPE) == AppConstants.NEW_CALL -> {
                    onReadyForCall(json.getString(AppConstants.CONNECTED_USER_ID))
                }
                json.getString(AppConstants.TYPE) == AppConstants.OFFER -> {
                    logE("offer received $text")
                    if (isFromPush) {
                        isFromPush = false
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra(AppConstants.JSON, json.toString())
                        intent.putExtra(AppConstants.IS_FROM_PUSH, true)
                        context?.startActivity(intent)
                    } else {
                        webSocketOfferCallback?.offerCallback(json)
                    }
                }
                else -> {
                    webSocketCallback?.webSocketCallback(json)
                }
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

    override fun onFailure(wSocket: WebSocket?, t: Throwable, response: Response?) {
        logE("onFailure " + t.message)
        val request = Request.Builder().url(Urls.WEB_SOCKET_URL).build()
        val okHttpClientBuilder = OkHttpClient.Builder()
        val webSocket1 = okHttpClientBuilder.build()
        webSocket = webSocket1.newWebSocket(request, this)
        this.setWebSocket(webSocket!!)
        webSocket1.dispatcher().executorService().shutdown()
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
            json.put("phone", sharedPrefsHelper.getUser()?.phone.toString())
            json.put("photoUrl", sharedPrefsHelper.getUser()?.profile_image.toString())
            json.put("deviceType", "android")
            json.put("token", sharedPrefsHelper.getFCMToken().toString())
            json.put("platform", "kalamtime")
            webSocket?.send(json.toString())
            logE("login successfully")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun onReadyForCall(connectedUserId: String) {
        try {
            val obj = JSONObject()
            obj.put("type", AppConstants.READY_FOR_CALL)
            obj.put("connectedUserId", connectedUserId)
            logE("readyForCall sent $obj")
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onReadyForCall JSONException ${e.message}")
            e.printStackTrace()
        }
    }


    fun onIceCandidateReceived(iceCandidate: JSONObject, callerID: String) {
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
            obj.put("type", AppConstants.REJECT)
            obj.put("connectedUserId", id)
            webSocket?.send(obj.toString())
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    fun onNewCall(id: String) {
        try {
            val obj = JSONObject()
            obj.put("type", AppConstants.NEW_CALL)
            obj.put("connectedUserId", id)
            webSocket?.send(obj.toString())
            logE("onNewCall sent $obj")
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}