package com.example.kalam_android.webrtc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.repository.net.Urls
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.wrapper.GlideDownloader
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_audio_call.*
import kotlinx.android.synthetic.main.call_activity.ibAnswer
import kotlinx.android.synthetic.main.call_activity.ibHangUp
import kotlinx.android.synthetic.main.secondcall_notify.*
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.net.URI
import java.util.*
import javax.inject.Inject

class AudioCallActivity : BaseActivity(), View.OnClickListener, WebSocketCallback,
    WebSocketOfferCallback {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var audioConstraints: MediaConstraints? = null
    private lateinit var sdpConstraints: MediaConstraints
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localPeer: PeerConnection? = null
    private lateinit var rootEglBase: EglBase
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private val TAG = this.javaClass.simpleName
    private var webSocketClientListener: CustomWebSocketClient? = null
    var callerID: Long = -1
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var calleeName: String? = null
    private var profileImage: String? = null
    private var uri: Uri? = null
    private var ringtune: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var handler = Handler()
    lateinit var callTimeRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)
        MyApplication.getAppComponent(this).doInjection(this)
        initPermissions()
    }

    @SuppressLint("CheckResult")
    fun initPermissions() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    startWebrtc()
                } else {
                    Debugger.e("initPermissions", "onPermissionDenied")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).check()
    }

    private fun initViews() {
        ibHangUp.setOnClickListener(this)
        ibAnswer.setOnClickListener(this)
        rootEglBase = EglBase.create()
        webSocketClientListener =
            CustomWebSocketClient.getInstance(sharedPrefsHelper, URI(Urls.WEB_SOCKET_URL))
        webSocketClientListener?.setSocketCallback(this)
        webSocketClientListener?.setOfferListener(this, false)
    }

    fun startWebrtc() {
        initViews()
        /* val stunIceServer = PeerConnection.IceServer
             .builder("stun:stun.l.google.com:19302")
             .createIceServer()
         peerIceServers.add(stunIceServer)*/
        val turnIceServer = PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
            .setUsername("webrtc@live.com")
            .setPassword("muazkh")
            .createIceServer()
        peerIceServers.add(turnIceServer)
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        audioConstraints = MediaConstraints()

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("101", audioSource)
        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtune = RingtoneManager.getRingtone(this, uri)

        callTimeRunnable = Runnable {
            logE("callTimeRunnable called")
            hangup()
        }
        handler.postDelayed(callTimeRunnable, 60 * 1000)

        val initiator = intent.getBooleanExtra(AppConstants.INITIATOR, false)
        if (initiator) {
            callerID = intent.getLongExtra(AppConstants.CALLER_USER_ID, 0)
            calleeName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
            profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
            ibAnswer.visibility = View.GONE
            tvCallStatus.text = "Calling"
            webSocketClientListener?.onNewCall(callerID.toString())

        } else {
            val json = intent.getStringExtra(AppConstants.JSON)
            ringtune?.play()
            val data = JSONObject(json)
            newCallReceived(data)
            tvCallStatus.text = "Incoming"
        }
        setCallerProfile()
    }

    private fun setCallerProfile() {
        GlideDownloader.load(
            this,
            ivUser,
            profileImage,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        tvName.text = calleeName.toString()
    }

    private fun newCallReceived(data: JSONObject) {
        val offer = JSONObject(data.getString("offer"))
        callerID = data.getString(AppConstants.CONNECTED_USER_ID).toLong()
        createPeerConnection()
        localPeer?.setRemoteDescription(
            CustomSdpObserver("localSetRemote"),
            SessionDescription(SessionDescription.Type.OFFER, offer.getString("sdp"))
        )
        profileImage = data.getString("photoUrl")
        calleeName = data.getString("name")
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        localPeer = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver("localPeerCreation") {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    val json = JSONObject()
                    json.put("sdp", iceCandidate.sdp)
                    json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                    json.put("sdpMid", iceCandidate.sdpMid)
                    webSocketClientListener?.onIceCandidateReceived(json, callerID.toString())
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    gotRemoteStream(mediaStream)
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                    runOnUiThread {
                        when (iceConnectionState) {
                            PeerConnection.IceConnectionState.CONNECTED -> {
                                logE("Peer Connection CONNECTED")
                                startChronometer()
                                handler.removeCallbacks(callTimeRunnable)
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                logE("Peer Connection DISCONNECTED")
                            }
                            PeerConnection.IceConnectionState.FAILED -> {
                                logE("Peer Connection FAILED")
                            }
                            PeerConnection.IceConnectionState.CLOSED -> {
                                logE("Peer Connection CLOSED")
                            }
                            PeerConnection.IceConnectionState.COMPLETED -> {
                                logE("Peer Connection COMPLETED")
                            }
                            PeerConnection.IceConnectionState.NEW -> {
                                logE("Peer Connection NEW")
                            }
                            PeerConnection.IceConnectionState.CHECKING -> {
                                logE("Peer Connection CHECKING")
                                tvCallStatus.text = "Connecting"
                            }
                            else -> {
                            }
                        }
                    }
                }
            })
        addStreamToLocalPeer()
    }

    private fun gotRemoteStream(stream: MediaStream) {
        val audioTrack = stream.audioTracks[0]
        runOnUiThread {
            try {
                logE("gotRemoteStream audioTrack: $audioTrack")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addStreamToLocalPeer() {
        val stream = peerConnectionFactory?.createLocalMediaStream("102")
        stream?.addTrack(localAudioTrack)
        logE("addStreamToLocalPeer : Stream : $stream")
        localPeer?.addStream(stream)
    }

    private fun doCall() {
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        localPeer?.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocalDesc"),
                    sessionDescription
                )
                logE("doCall : onCreateSuccess : SignallingClient emit ")
                webSocketClientListener?.createOffer(sessionDescription, callerID.toString(), false)

            }
        }, sdpConstraints)
    }

    private fun answerCall() {
        logE("onOfferReceived")
        try {
            ringtune?.stop()
            ibAnswer.visibility = View.GONE
            doAnswer()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    private fun startChronometer() {
        tvCallStatus.visibility = View.GONE
        chronometer.visibility = View.VISIBLE
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun doAnswer() {
        localPeer?.createAnswer(object : CustomSdpObserver("localCreateAnswer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                logE("doAnswer : onCreateSuccess $sessionDescription")
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocal"),
                    sessionDescription
                )
                Log.e("testingSocket", "Emit Description [ $sessionDescription]")
                webSocketClientListener?.doAnswer(sessionDescription, callerID.toString())
            }
        }, MediaConstraints())
    }

    private fun saveIceCandidates(data: JSONObject) {
        try {
            logE("onIceCandidateReceived : $data")
            val candidates = JSONObject(data.getString("candidate"))
            localPeer?.addIceCandidate(
                IceCandidate(
                    candidates.getString("sdpMid"),
                    candidates.getInt("sdpMLineIndex"),
                    candidates.getString("sdp")
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onAnswerReceived(data: JSONObject) {
        try {
            logE("onAnswerReceived : $data")
            val answer = JSONObject(data.getString("offer"))
            logE("onAnswerReceived : $answer")
            localPeer?.setRemoteDescription(
                CustomSdpObserver("localSetRemote"),
                SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()),
                    answer.getString("sdp")
                )
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ibHangUp -> {
                webSocketClientListener?.onHangout(callerID.toString())
                hangup()
            }
            R.id.ibAnswer -> {
                answerCall()
            }
        }
    }

    private fun hangup() {
//        try {
        if (ringtune?.isPlaying == true) {
            ringtune?.stop()
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory?.stopAecDump()
        }
        if (localPeer != null) {
            localPeer?.close()
            localPeer = null

        }
        webSocketClientListener?.reAssignOfferListenerToMain()
        setResult(Activity.RESULT_OK)
        finish()
        overridePendingTransition(R.anim.anim_nothing, R.anim.bottom_down)
        /*} catch (e: Exception) {
            logE("Exception Occurred ${e.message}")
        }*/
    }

    override fun webSocketCallback(jsonObject: JSONObject) {
        runOnUiThread {
            when (jsonObject.getString(AppConstants.TYPE)) {
                AppConstants.CANDIDATE -> {
                    logE("did candidates received")
                    saveIceCandidates(jsonObject)
                }
                AppConstants.ANSWER -> {
                    logE("did answer received")
                    onAnswerReceived(jsonObject)
                }
                AppConstants.REJECT -> {
                    if (jsonObject.getString(AppConstants.CONNECTED_USER_ID).toLong() == callerID) {
                        hangup()
                    }
                }
                AppConstants.READY_FOR_CALL -> {
                    logE("did readyForCall received $jsonObject")
                    tvCallStatus.text = "Ringing"
                    createPeerConnection()
                    doCall()
                }
            }
        }
    }

    override fun onBackPressed() {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("End Call")
        builder1.setMessage("Do you really want to end this call?")
        builder1.setCancelable(true)
        builder1.setPositiveButton("Yes") { dialog, id ->
            webSocketClientListener?.onHangout(callerID.toString())
            hangup()
        }
        builder1.setNegativeButton("No") { dialog, id ->
            dialog.cancel()
        }
        builder1.create().show()
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    private fun secondCallDialogue(jsonObject: JSONObject) {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.secondcall_notify)
        dialog.btnCancelCall.setOnClickListener {
            dialog.dismiss()
            vibrator?.cancel()
            webSocketClientListener?.onHangout(jsonObject.getString(AppConstants.CONNECTED_USER_ID))
        }
        dialog.btnAcceptReject.setOnClickListener {
            dialog.dismiss()
            vibrator?.cancel()
            webSocketClientListener?.onHangout(callerID.toString())
            newCallReceived(jsonObject)
            setCallerProfile()
            doAnswer()
        }
        GlideDownloader.load(
            this,
            dialog.cvImageCaller,
            jsonObject.getString("photoUrl").toString(),
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        dialog.tvTitleCall.text =
            StringBuilder(jsonObject.getString("name").toString()).append(" ")
                .append("is calling....").toString()
        dialog.show()
    }

    private fun vibratePhone() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    1000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator?.vibrate(1000)
        }
    }

    override fun offerCallback(jsonObject: JSONObject) {
        runOnUiThread {
            logE("offer received $jsonObject")
            vibratePhone()
            secondCallDialogue(jsonObject)
        }
    }
}
