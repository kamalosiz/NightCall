package com.example.kalam_android.webrtc

import android.util.Log
import com.example.kalam_android.util.Debugger

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

internal open class CustomSdpObserver(logTag: String) : SdpObserver {


    private var tag: String? = null

    init {
        tag = this.javaClass.canonicalName
        this.tag = this.tag + " " + logTag
    }


    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Debugger.e(
            tag.toString(),
            "onCreateSuccess() called with: sessionDescription = [$sessionDescription]"
        )
    }

    override fun onSetSuccess() {
        Debugger.e(tag.toString(), "onSetSuccess() called")
    }

    override fun onCreateFailure(s: String) {
        Debugger.e(tag.toString(), "onCreateFailure() called with: s = [$s]")
    }

    override fun onSetFailure(s: String) {
        Debugger.e(tag.toString(), "onSetFailure() called with: s = [$s]")
    }

}
