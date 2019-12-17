package com.example.kalam_android.webrtc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.util.toast
import com.example.kalam_android.wrapper.GlideDownloder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_audio_call.*
import kotlinx.android.synthetic.main.call_activity.ibAnswer
import kotlinx.android.synthetic.main.call_activity.ibHangUp
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.util.*
import javax.inject.Inject

class AudioCallActivity : BaseActivity(), View.OnClickListener, WebSocketCallback {
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var audioConstraints: MediaConstraints? = null
    private lateinit var sdpConstraints: MediaConstraints
    private lateinit var audioSource: AudioSource
    private lateinit var localAudioTrack: AudioTrack
    internal var localPeer: PeerConnection? = null
    private lateinit var rootEglBase: EglBase
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private val TAG = this.javaClass.simpleName
    private var webSocketListener: CustomWebSocketListener? = null
    var callerID: Long = -1
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var mediaPlayer: MediaPlayer? = null
    lateinit var audioManager: AudioManager
    private var calleeName: String? = null
    private var profileImage: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
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
                if (report!!.areAllPermissionsGranted()) {
                    startWebrtc()
                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
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
        webSocketListener = CustomWebSocketListener.getInstance(sharedPrefsHelper)
        webSocketListener?.setSocketCallback(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun startWebrtc() {
        initViews()
        val stunIceServer = PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
        peerIceServers.add(stunIceServer)
        val turnIceServer = PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
            .setUsername("webrtc@live.com")
            .setPassword("muazkh")
            .createIceServer()
        peerIceServers.add(turnIceServer)
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .setEnableVideoHwAcceleration(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        peerConnectionFactory =
            PeerConnectionFactory(options, defaultVideoEncoderFactory, defaultVideoDecoderFactory)

        audioConstraints = MediaConstraints()

        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)
        mediaPlayer = MediaPlayer.create(this, R.raw.incoming)
        val initiator = intent.getBooleanExtra(AppConstants.INITIATOR, false)
        if (initiator) {
            callerID = intent.getLongExtra(AppConstants.CALLER_USER_ID, 0)
            calleeName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
            profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
            createPeerConnection()
            doCall()
            ibAnswer.visibility = View.GONE
            tvCallStatus.text = "Outgoing"
        } else {
            val json = intent.getStringExtra(AppConstants.JSON)
            mediaPlayer?.start()
            mediaPlayer?.isLooping = true
            val data = JSONObject(json)
            callerID = data.getLong(AppConstants.CONNECTED_USER_ID)
            createPeerConnection()
            localPeer?.setRemoteDescription(
                CustomSdpObserver("localSetRemote"),
                SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp"))
            )
            profileImage = data.getString("photoUrl")
            calleeName = data.getString("name")
            tvCallStatus.text = "Incoming"
        }
        GlideDownloder.load(
            this,
            ivUser,
            profileImage,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        tvName.text = calleeName.toString()
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        localPeer = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver("localPeerCreation") {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    webSocketListener?.onIceCandidateReceived(iceCandidate, callerID)
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    gotRemoteStream(mediaStream)
                }
            })
        addStreamToLocalPeer()
    }

    private fun gotRemoteStream(stream: MediaStream) {
        val audioTrack = stream.audioTracks[0]
        runOnUiThread {
            try {
                toast("gotRemoteStream")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*private fun openSpeakerOn() {
        try {
            if (!audioManager.isSpeakerphoneOn) audioManager.isSpeakerphoneOn = true
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }*/

    private fun addStreamToLocalPeer() {
        val stream = peerConnectionFactory.createLocalMediaStream("102")
        stream.addTrack(localAudioTrack)
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
                webSocketListener?.createOffer(sessionDescription, callerID, false)

            }
        }, sdpConstraints)
    }

    private fun answerCall() {
        logE("onOfferReceived")
        try {
            doAnswer()
            startChronometer()
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
                webSocketListener?.doAnswer(sessionDescription, callerID)
            }
        }, MediaConstraints())
    }

    private fun saveIceCandidates(data: JSONObject) {
        try {
            logE("onIceCandidateReceived : $data")
            localPeer?.addIceCandidate(
                IceCandidate(
                    data.getString("id"),
                    data.getInt("label"),
                    data.getString("candidate")
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun onAnswerReceived(data: JSONObject) {
        try {

            logE("onAnswerReceived : $data")
            localPeer?.setRemoteDescription(
                CustomSdpObserver("localSetRemote"),
                SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()),
                    data.getString("sdp")
                )
            )
            startChronometer()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ibHangUp -> {
                hangup()
                webSocketListener?.onHangout(callerID)
            }
            R.id.ibAnswer -> {
                mediaPlayer?.stop()
                answerCall()
                ibAnswer.visibility = View.GONE
            }
        }
    }

    private fun hangup() {
        try {
            mediaPlayer?.stop()
            peerConnectionFactory.stopAecDump()
            localPeer?.close()
            localPeer = null
            if (audioConstraints != null) {
                audioConstraints = null
            }
            audioSource.dispose()
            localAudioTrack.dispose()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    override fun webSocketCallback(jsonObject: JSONObject) {
        runOnUiThread {
            when (jsonObject.getString(AppConstants.TYPE)) {
                AppConstants.CANDIDATE -> {
                    logE("webSocketCallback candidate")
                    saveIceCandidates(jsonObject)
                }
                AppConstants.ANSWER -> {
                    logE("webSocketCallback answer")
                    onAnswerReceived(jsonObject)
                }
                AppConstants.REJECT -> {
                    hangup()
                }
            }
        }
    }
}
