package com.example.kalam_android.webrtc

import com.example.kalam_android.util.Debugger
import org.webrtc.*

internal open class CustomPeerConnectionObserver(logTag: String) : PeerConnection.Observer {

    private var logTag: String? = null

    init {
        this.logTag = this.javaClass.canonicalName
        this.logTag = this.logTag + " " + logTag
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Debugger.d(
            logTag.toString(),
            "onSignalingChange() called with: signalingState = [$signalingState]"
        )
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Debugger.d(
            logTag.toString(),
            "onIceConnectionChange() called with: iceConnectionState = [$iceConnectionState]"
        )
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Debugger.d(logTag.toString(), "onIceConnectionReceivingChange() called with: b = [$b]")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Debugger.d(
            logTag.toString(),
            "onIceGatheringChange() called with: iceGatheringState = [$iceGatheringState]"
        )
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Debugger.d(
            logTag.toString(),
            "onIceCandidate() called with: iceCandidate = [$iceCandidate]"
        )
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Debugger.d(
            logTag.toString(),
            "onIceCandidatesRemoved() called with: iceCandidates = [$iceCandidates]"
        )
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Debugger.d(logTag.toString(), "onAddStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Debugger.d(logTag.toString(), "onRemoveStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Debugger.d(logTag.toString(), "onDataChannel() called with: dataChannel = [$dataChannel]")
    }

    override fun onRenegotiationNeeded() {
        Debugger.d(logTag.toString(), "onRenegotiationNeeded() called")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        Debugger.d(
            logTag.toString(),
            "onAddTrack() called with: rtpReceiver = [$rtpReceiver], mediaStreams = [$mediaStreams]"
        )
    }
}
