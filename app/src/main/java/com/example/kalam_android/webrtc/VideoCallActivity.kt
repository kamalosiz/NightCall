package com.example.kalam_android.webrtc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.wrapper.GlideDownloder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_audio_call.*
import kotlinx.android.synthetic.main.call_activity.*
import kotlinx.android.synthetic.main.call_activity.chronometer
import kotlinx.android.synthetic.main.call_activity.header
import kotlinx.android.synthetic.main.call_activity.ibAnswer
import kotlinx.android.synthetic.main.call_activity.ibHangUp
import kotlinx.android.synthetic.main.call_activity.ivUser
import kotlinx.android.synthetic.main.call_activity.rlBottomCalls
import kotlinx.android.synthetic.main.call_activity.tvCallStatus
import kotlinx.android.synthetic.main.call_activity.tvName
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class VideoCallActivity : BaseActivity(), View.OnClickListener, WebSocketCallback {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var audioConstraints: MediaConstraints? = null
    private var videoConstraints: MediaConstraints? = null
    private lateinit var sdpConstraints: MediaConstraints
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    internal var localPeer: PeerConnection? = null
    private lateinit var rootEglBase: EglBase
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private val TAG = this.javaClass.simpleName
    private var webSocketListener: CustomWebSocketListener? = null
    var callerID: Long = -1
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var videoCapture: CameraVideoCapturer? = null
    private var calleeName: String? = null
    private var profileImage: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.call_activity)
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
        rlCameras.setOnClickListener(this)
        rootEglBase = EglBase.create()
        localVideoView.init(rootEglBase.eglBaseContext, null)
        remoteVideoView.init(rootEglBase.eglBaseContext, null)
        localVideoView.setZOrderMediaOverlay(true)
        remoteVideoView.setZOrderMediaOverlay(true)
        webSocketListener = CustomWebSocketListener.getInstance(sharedPrefsHelper)
        webSocketListener?.setSocketCallback(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer.create(this, R.raw.incoming)
    }

    fun startWebrtc() {
        initViews()
        //        getIceServers();
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

        val videoCapturerAndroid: VideoCapturer? = createCameraCapturer(Camera1Enumerator(false))
        audioConstraints = MediaConstraints()
        videoConstraints = MediaConstraints()
        if (videoCapturerAndroid != null) {
            videoSource = peerConnectionFactory?.createVideoSource(videoCapturerAndroid)
        }
        localVideoTrack = peerConnectionFactory?.createVideoTrack("100", videoSource)
        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("101", audioSource)
//        videoCapturerAndroid?.startCapture(1024, 720, 30)
        videoCapturerAndroid?.startCapture(640, 480, 30)
        localVideoView.visibility = View.VISIBLE
        localVideoTrack?.addSink(localVideoView)
        localVideoView.setMirror(true)
        remoteVideoView.setMirror(true)
        val initiator = intent.getBooleanExtra(AppConstants.INITIATOR, false)
        if (initiator) {
            callerID = intent.getLongExtra(AppConstants.CALLER_USER_ID, 0)
            calleeName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
            profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
            createPeerConnection()
            doCall()
            ibAnswer.visibility = View.GONE
            turnOnSpeakers()
            tvCallStatus.text = "Outgoing"
        } else {
            val json = intent.getStringExtra(AppConstants.JSON)
            mediaPlayer?.start()
            mediaPlayer?.isLooping = true
            val data = JSONObject(json)
            val offer = JSONObject(data.getString("offer"))
            callerID = data.getString(AppConstants.CONNECTED_USER_ID).toLong()
            createPeerConnection()
            localPeer?.setRemoteDescription(
                CustomSdpObserver("localSetRemote"),
                SessionDescription(SessionDescription.Type.OFFER, offer.getString("sdp"))
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

    private fun startChronometer() {
        tvCallStatus.visibility = View.GONE
        chronometer.visibility = View.VISIBLE
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
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
                    webSocketListener?.onIceCandidateReceived(json, callerID.toString())
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
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                logE("Peer Connection DISCONNECTED")
                            }
                            PeerConnection.IceConnectionState.FAILED -> {
                                logE("Peer Connection FAILED")
                            }
                            PeerConnection.IceConnectionState.CHECKING -> {
                                logE("Peer Connection CHECKING")
                                tvCallStatus.text = "Connecting"
                            }
                        }
                    }
                }
            })
        addStreamToLocalPeer()
    }

    private fun gotRemoteStream(stream: MediaStream) {
        val videoTrack = stream.videoTracks[0]
        runOnUiThread {
            try {
                remoteVideoView.visibility = View.VISIBLE
                videoTrack.addSink(remoteVideoView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun turnOnSpeakers() {
        try {
            if (!audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = true
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOFFSpeakers() {
        try {
            if (audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun addStreamToLocalPeer() {
        val stream = peerConnectionFactory?.createLocalMediaStream("102")
        stream?.addTrack(localAudioTrack)
        stream?.addTrack(localVideoTrack)
        localPeer?.addStream(stream)
    }

    private fun doCall() {
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        localPeer?.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocalDesc"),
                    sessionDescription
                )
                webSocketListener?.createOffer(sessionDescription, callerID.toString(), true)

            }
        }, sdpConstraints)
    }

    private fun answerCall() {
        logE("onOfferReceived")
        try {
            mediaPlayer?.pause()
            ibAnswer.visibility = View.GONE
            doAnswer()
            updateVideoViews()
            turnOnSpeakers()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
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
                webSocketListener?.doAnswer(sessionDescription, callerID.toString())
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

    private fun onAnswerSave(data: JSONObject) {
        try {
            logE("onAnswerReceived : $data")
            val answer = JSONObject(data.getString("offer"))
            localPeer?.setRemoteDescription(
                CustomSdpObserver("localSetRemote"),
                SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()),
                    answer.getString("sdp")
                )
            )
            updateVideoViews()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun updateVideoViews() {
        runOnUiThread {
            val params = localVideoView.layoutParams
            params.height = dpToPx()
            params.width = dpToPx()
            localVideoView.layoutParams = params
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ibHangUp -> {
                hangup()
                webSocketListener?.onHangout(callerID.toString())
            }
            R.id.ibAnswer -> {
                answerCall()
            }
            R.id.rlCameras -> {
                if (header.visibility == View.VISIBLE) {
                    rlBottomCalls.visibility = View.GONE
                    header.visibility = View.GONE
                } else {
                    header.visibility = View.VISIBLE
                    rlBottomCalls.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun hangup() {
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            turnOFFSpeakers()
            if (peerConnectionFactory != null) {
                peerConnectionFactory?.stopAecDump()
            }
            if (localPeer != null) {
                localPeer?.close()
                localPeer = null
            }
            if (videoCapture != null) {
                videoCapture?.stopCapture()
                videoCapture?.dispose()
                videoCapture = null
            }
            if (localVideoView != null || remoteVideoView != null) {
                localVideoView.release()
                remoteVideoView.release()
            }
            if (audioSource != null && videoSource != null) {
                audioSource?.dispose()
                videoSource?.dispose()
            }
            /*if (peerConnectionFactory != null) {
                peerConnectionFactory?.dispose()
                peerConnectionFactory = null
            }*/
            finish()
            overridePendingTransition(R.anim.anim_nothing, R.anim.bottom_down)
        } catch (e: Exception) {
            logE("Exception Occurred ${e.message}")
        }

    }

    private fun dpToPx(): Int {
        val displayMetrics = resources.displayMetrics
        return (resources.getDimension(R.dimen._50sdp) * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        logE("Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                videoCapture = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }
        logE("Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                videoCapture = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }

        return null
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    override fun webSocketCallback(jsonObject: JSONObject) {
        runOnUiThread {
            when (jsonObject.getString(AppConstants.TYPE)) {
                AppConstants.CANDIDATE -> {
                    saveIceCandidates(jsonObject)
                }
                AppConstants.ANSWER -> {
                    onAnswerSave(jsonObject)
                }
                AppConstants.REJECT -> {
                    hangup()
                }
            }
        }
    }
}
