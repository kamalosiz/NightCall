package com.example.kalam_android.webrtc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.callbacks.WebSocketCallback
import com.example.kalam_android.callbacks.WebSocketOfferCallback
import com.example.kalam_android.databinding.CallActivityBinding
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
import kotlinx.android.synthetic.main.secondcall_notify.*
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.net.URI
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class CallActivity : BaseActivity(), View.OnClickListener, WebSocketCallback,
    WebSocketOfferCallback {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var audioConstraints: MediaConstraints? = null
    private lateinit var sdpConstraints: MediaConstraints
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    internal var localPeer: PeerConnection? = null
    private lateinit var rootEglBase: EglBase
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private val TAG = this.javaClass.simpleName
    private var webSocketClient: CustomWebSocketClient? = null
    var callerID: Int = -1
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var audioManager: AudioManager
    private var videoCapture: CameraVideoCapturer? = null
    private var calleeName: String? = null
    private var myName: String? = null
    private var profileImage: String? = null

    private var uri: Uri? = null
    private var ringtune: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var handler = Handler()
    lateinit var callTimeRunnable: Runnable
    private var isVideo = false
    lateinit var binding: CallActivityBinding
    private var isSpeakerOn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.call_activity)
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

    private fun initialization() {
        isVideo = intent.getBooleanExtra(AppConstants.IS_VIDEO_CALL, false)
        rootEglBase = EglBase.create()
        initViews(isVideo)
        binding.ibHangUp.setOnClickListener(this)
        binding.ibAnswer.setOnClickListener(this)
        webSocketClient =
            CustomWebSocketClient.getInstance(sharedPrefsHelper, URI(Urls.WEB_SOCKET_URL))
        CustomWebSocketClient.getInstance(sharedPrefsHelper, URI(Urls.WEB_SOCKET_URL))
            .setSocketCallback(this)
        webSocketClient?.setOfferListener(this, false)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun initViews(isVideo: Boolean) {
        if (isVideo) {
            binding.rlCameras.visibility = View.VISIBLE
            binding.tvCall.text = "Video Call"
            binding.ivUserAudio.visibility = View.GONE
            binding.ivUserVideo.visibility = View.VISIBLE
            binding.rlCameras.setOnClickListener(this)
            binding.localVideoView.init(rootEglBase.eglBaseContext, null)
            binding.remoteVideoView.init(rootEglBase.eglBaseContext, null)
            binding.localVideoView.setZOrderMediaOverlay(true)
            binding.remoteVideoView.setZOrderMediaOverlay(true)
        } else {
            binding.rlCameras.visibility = View.GONE
            binding.tvCall.text = "Audio Call"
            binding.ivUserAudio.visibility = View.VISIBLE
            binding.ivUserVideo.visibility = View.GONE
            binding.ibSpeaker.setOnClickListener(this)
        }
    }

    private fun initVideoCapturer(isVideo: Boolean) {
        if (isVideo) {
            val videoCapturerAndroid: VideoCapturer? =
                createCameraCapturer(Camera1Enumerator(false))
            if (videoCapturerAndroid != null) {
                val surfaceTextureHelper =
                    SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
                videoSource =
                    peerConnectionFactory?.createVideoSource(videoCapturerAndroid.isScreencast)
                videoCapturerAndroid.initialize(
                    surfaceTextureHelper,
                    applicationContext,
                    videoSource?.capturerObserver
                )
            }
            localVideoTrack = peerConnectionFactory?.createVideoTrack("100", videoSource)
            videoCapturerAndroid?.startCapture(640, 480, 30)
            binding.localVideoView.visibility = View.VISIBLE
            localVideoTrack?.addSink(binding.localVideoView)
            binding.localVideoView.setMirror(true)
            binding.remoteVideoView.setMirror(true)
        }
    }

    fun startWebrtc() {
        initialization()
        //        getIceServers();

        val stunIceServer = PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
        peerIceServers.add(stunIceServer)
        val turnIceServer = PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
            .setUsername("webrtc@live.com")
            .setPassword("muazkh")
            .createIceServer()
        peerIceServers.add(turnIceServer)



        logE("peerIceServers :$peerIceServers")
/*        val turnIceServer = PeerConnection.IceServer.builder("stun:52.53.151.191:3478")
            .setUsername("softech")
            .setPassword("Kalaam2020")
            .createIceServer()
        peerIceServers.add(turnIceServer)
        val turnIceServer1 =
            PeerConnection.IceServer.builder("turn:52.53.151.191:3478?transport=udp")
                .setUsername("softech")
                .setPassword("Kalaam2020")
                .createIceServer()
        peerIceServers.add(turnIceServer1)
        val turnIceServer2 =
            PeerConnection.IceServer.builder("turn:52.53.151.191:3478?transport=tcp")
                .setUsername("softech")
                .setPassword("Kalaam2020")
                .createIceServer()
        peerIceServers.add(turnIceServer2)*/
/*        val turnIceServer2 =
            PeerConnection.IceServer.builder("turn:global.turn.twilio.com:3478?transport=tcp")
                .setUsername("82a800ce5dadb28b08a40ad3ab4841cf495e9b08404b8347d268e0c74f197250")
                .setPassword("iNVTHqAjtr3X/TNXsOOerZ4kta9yCKoTZnFbj8kQ+30=")
                .createIceServer()
        peerIceServers.add(turnIceServer2)

        val turnIceServer1 =
            PeerConnection.IceServer.builder("turn:global.turn.twilio.com:3478?transport=udp")
                .setUsername("82a800ce5dadb28b08a40ad3ab4841cf495e9b08404b8347d268e0c74f197250")
                .setPassword("iNVTHqAjtr3X/TNXsOOerZ4kta9yCKoTZnFbj8kQ+30=")
                .createIceServer()
        peerIceServers.add(turnIceServer1)

        val turnIceServer3 =
            PeerConnection.IceServer.builder("turn:global.turn.twilio.com:443?transport=tcp")
                .setUsername("82a800ce5dadb28b08a40ad3ab4841cf495e9b08404b8347d268e0c74f197250")
                .setPassword("iNVTHqAjtr3X/TNXsOOerZ4kta9yCKoTZnFbj8kQ+30=")
                .createIceServer()
        peerIceServers.add(turnIceServer3)
        val stunIceServer =
            PeerConnection.IceServer.builder("stun:global.stun.twilio.com:3478?transport=udp")
                .createIceServer()
        peerIceServers.add(stunIceServer)

        val stunIceServer2 =
            PeerConnection.IceServer.builder("stun:global.stun.twilio.com:3478?transport=tcp")
                .createIceServer()
        peerIceServers.add(stunIceServer2)*/

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
        initVideoCapturer(isVideo)

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("101", audioSource)

        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtune = RingtoneManager.getRingtone(this, uri)

        callTimeRunnable = Runnable {
            logE("callTimeRunnable called")
            hangup()
        }
        handler.postDelayed(callTimeRunnable, 90 * 1000)

        val initiator = intent.getBooleanExtra(AppConstants.INITIATOR, false)
        if (initiator) {
            callerID = intent.getIntExtra(AppConstants.CALLER_USER_ID, 0)
            myName = intent.getStringExtra(AppConstants.GET_MY_NICKNAME)
            calleeName = intent.getStringExtra(AppConstants.CHAT_USER_NAME)
            profileImage = intent.getStringExtra(AppConstants.CHAT_USER_PICTURE)
            binding.ibAnswer.visibility = View.GONE
            binding.ibSpeaker.visibility = View.VISIBLE
            binding.tvCallStatus.text = "Calling..."
            webSocketClient?.onNewCall(callerID.toString(), myName)

        } else {
            logE("is initiator False")
            val json = intent.getStringExtra(AppConstants.JSON)
            ringtune?.play()
            val data = JSONObject(json)
            newCallReceived(data)
            binding.tvCallStatus.text = "Incoming..."
        }
        if (isVideo) {
            setCallerProfile(true)
        } else {
            setCallerProfile(false)
        }
    }

    private fun newCallReceived(data: JSONObject) {
        logE("newCallReceived")
        val offer = JSONObject(data.getString("offer"))
        callerID = data.getString(AppConstants.CONNECTED_USER_ID).toInt()
        logE("callerID :$callerID")
        createPeerConnection()
        localPeer?.setRemoteDescription(
            CustomSdpObserver("localSetRemote"),
            SessionDescription(SessionDescription.Type.OFFER, offer.getString("sdp"))
        )
        profileImage = data.getString("photoUrl")
        calleeName = data.getString("name")
    }

    private fun setCallerProfile(isVideo: Boolean) {
        if (isVideo) {
            GlideDownloader.load(
                this,
                binding.ivUserVideo,
                profileImage,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
        } else {
            GlideDownloader.load(
                this,
                binding.ivUserAudio,
                profileImage,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
        }
        binding.tvName.text = calleeName.toString()
    }

    private fun startChronometer() {
        binding.tvCallStatus.visibility = View.GONE
        binding.chronometer.visibility = View.VISIBLE
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.start()
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
                    webSocketClient?.onIceCandidateReceived(json, callerID.toString())
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    if (isVideo)
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
                            PeerConnection.IceConnectionState.CHECKING -> {
                                logE("Peer Connection CHECKING")
                                binding.tvCallStatus.text = "Connecting..."
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
        val videoTrack = stream.videoTracks[0]
        runOnUiThread {
            try {
                binding.remoteVideoView.visibility = View.VISIBLE
                videoTrack.addSink(binding.remoteVideoView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun turnOnSpeakers() {
        try {
            logE("TURNING ON SPEAKERS")
            if (!audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = true
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOFFSpeakers() {
        try {
            logE("TURNING OFF SPEAKERS")
            if (audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addStreamToLocalPeer() {
        val stream = peerConnectionFactory?.createLocalMediaStream("102")
        stream?.addTrack(localAudioTrack)
        if (isVideo)
            stream?.addTrack(localVideoTrack)
        localPeer?.addStream(stream)
    }

    private fun doCall() {
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        if (isVideo) {
            sdpConstraints.mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
        }
        localPeer?.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocalDesc"),
                    sessionDescription
                )
                webSocketClient?.createOffer(sessionDescription, callerID.toString(), isVideo)

            }
        }, sdpConstraints)
    }

    private fun answerCall() {
        logE("onOfferReceived")
        if (ringtune?.isPlaying == true) {
            ringtune?.stop()
        }
        binding.ibAnswer.visibility = View.GONE
        doAnswer()
        if (isVideo) {
            updateVideoViews()
            turnOnSpeakers()
        } else {
            binding.ibSpeaker.visibility = View.VISIBLE
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
                webSocketClient?.doAnswer(sessionDescription, callerID.toString())
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
        val params = binding.localVideoView.layoutParams
        params.height = dpToPx()
        params.width = dpToPx()
        binding.localVideoView.layoutParams = params
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ibHangUp -> {
                hangup()
                webSocketClient?.onHangout(callerID.toString())
            }
            R.id.ibAnswer -> {
                answerCall()
            }
            R.id.rlCameras -> {
                if (isVideo) {
                    if (binding.header.visibility == View.VISIBLE) {
                        binding.rlBottomCalls.visibility = View.GONE
                        binding.header.visibility = View.GONE
                    } else {
                        binding.header.visibility = View.VISIBLE
                        binding.rlBottomCalls.visibility = View.VISIBLE
                    }
                }
            }
            R.id.ibSpeaker -> {
                if (!isSpeakerOn) {
                    binding.ibSpeaker.setBackgroundResource(R.drawable.speaker_off)
                    isSpeakerOn = true
                    turnOnSpeakers()
                } else {
                    binding.ibSpeaker.setBackgroundResource(R.drawable.speaker_on)
                    isSpeakerOn = false
                    turnOFFSpeakers()
                }
            }
        }
    }

    private fun hangup() {
        try {

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
            if (isVideo) {
//                turnOFFSpeakers()
                if (videoCapture != null) {
                    videoCapture?.stopCapture()
                    videoCapture?.dispose()
                    videoCapture = null
                }
                if (binding.localVideoView != null || binding.remoteVideoView != null) {
                    binding.localVideoView.release()
                    binding.remoteVideoView.release()
                }
            }
            turnOFFSpeakers()
            webSocketClient?.reAssignOfferListenerToMain()
            setResult(Activity.RESULT_OK)
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

    override fun onBackPressed() {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("End Call")
        builder1.setMessage("Do you really want to end this call?")
        builder1.setCancelable(true)
        builder1.setPositiveButton("Yes") { dialog, id ->
            dialog.cancel()
            hangup()
            webSocketClient?.onHangout(callerID.toString())
        }
        builder1.setNegativeButton("No") { dialog, id ->
            dialog.cancel()
        }
        builder1.create().show()
    }

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }

    override fun webSocketCallback(jsonObject: JSONObject) {
        runOnUiThread {
            when (jsonObject.getString(AppConstants.TYPE)) {
                AppConstants.CANDIDATE -> {
                    logE("iceCandidates Received :$jsonObject")
                    saveIceCandidates(jsonObject)
                }
                AppConstants.ANSWER -> {
                    logE("answer Received :$jsonObject")
                    onAnswerSave(jsonObject)
                }
                AppConstants.REJECT -> {
                    hangup()
                }
                AppConstants.READY_FOR_CALL -> {
                    createPeerConnection()
                    doCall()
                    binding.tvCallStatus.text = "Ringing..."
                    if (isVideo) {
                        turnOnSpeakers()
                    }
                }
            }
        }
    }

    private fun secondCallDialogue(jsonObject: JSONObject) {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.secondcall_notify)
        dialog.btnCancelCall.setOnClickListener {
            dialog.dismiss()
            vibrator?.cancel()
            webSocketClient?.onHangout(jsonObject.getString(AppConstants.CONNECTED_USER_ID))
        }
        dialog.btnAcceptReject.setOnClickListener {
            dialog.dismiss()
            vibrator?.cancel()
            webSocketClient?.onHangout(callerID.toString())
            val isVideo = jsonObject.getBoolean("isVideo")
            if (this.isVideo != isVideo) {
                this.isVideo = isVideo
                initViews(isVideo)
                if (isVideo) initVideoCapturer(isVideo)
                else turnOFFSpeakers()
            }
            newCallReceived(jsonObject)
            setCallerProfile(this.isVideo)
//            doAnswer()
            answerCall()
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
