package com.example.kalam_android.webrtc

import android.content.Context
import android.content.Intent
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.MainActivity
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.SessionDescription
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI

class CustomWebSocketClient private constructor(
    val sharedPrefsHelper: SharedPrefsHelper,
    uri: URI
) :
    WebSocketClient(uri) {


    private val TAG = this.javaClass.simpleName
    private var webSocketCallback: WebSocketCallback? = null
    private var webSocketOfferCallback: WebSocketOfferCallback? = null
    private var dummyWebSocketOfferCallback: WebSocketOfferCallback? = null
    private var isFromPush = false
    private var connectedUserId = ""

    companion object {
        private var instance: CustomWebSocketClient? = null
        var context: Context? = null
        @Synchronized
        fun getInstance(sharedPrefsHelper: SharedPrefsHelper, uri: URI): CustomWebSocketClient {
            if (instance == null) {
                instance = CustomWebSocketClient(sharedPrefsHelper, uri)
            }
            return instance as CustomWebSocketClient
        }

        @Synchronized
        fun getInstance(
            sharedPrefsHelper: SharedPrefsHelper,
            context: Context, uri: URI
        ): CustomWebSocketClient {
            if (instance == null) {
                this.context = context
                instance = CustomWebSocketClient(sharedPrefsHelper, uri)
            }
            return instance as CustomWebSocketClient
        }
    }

    fun setSocketCallback(webSocketCallback: WebSocketCallback?) {
        this.webSocketCallback = webSocketCallback
    }

    fun setOfferListener(
        webSocketOfferCallback: WebSocketOfferCallback,
        isMainOfferCallback: Boolean
    ) {
        if (isMainOfferCallback) {
            this.webSocketOfferCallback = webSocketOfferCallback
        } else {
            dummyWebSocketOfferCallback = this.webSocketOfferCallback
            this.webSocketOfferCallback = webSocketOfferCallback
        }
    }

    fun reAssignOfferListenerToMain() {
        this.webSocketOfferCallback = dummyWebSocketOfferCallback
    }


    fun setPushData(connectedUserId: String, isFromPush: Boolean) {
        this.connectedUserId = connectedUserId
        this.isFromPush = isFromPush
    }


    override fun onOpen() {
        logE("Yahoo websocket is open now")
        login()
        if (isFromPush) {
            onReadyForCall(connectedUserId)
        }
    }

    override fun onTextReceived(text: String?) {
        logE("onMessage string" + text!!)
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
    }

    override fun onPongReceived(data: ByteArray?) {
        logE("onPongReceived $data")
    }

    override fun onException(e: Exception?) {
        logE("onException ${e?.message}")
    }

    override fun onCloseReceived() {
        logE("onCloseReceived")
    }

    override fun onBinaryReceived(data: ByteArray?) {
        logE("onBinaryReceived $data")
    }

    override fun onPingReceived(data: ByteArray?) {
        logE("onPingReceived $data")
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
            send(json.toString())
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
            send(obj.toString())
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
            send(obj.toString())
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
            send(obj.toString())

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
            send(obj.toString())
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
            send(obj.toString())
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
            send(obj.toString())
            logE("onNewCall sent $obj")
        } catch (e: JSONException) {
            logE("onSetFailure" + e.message)
            e.printStackTrace()
        }
    }

    fun disconnectSocket() {
        close()
        instance = null
    }


    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

}
