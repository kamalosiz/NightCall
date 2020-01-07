package com.example.kalam_android.callbacks

import android.view.View
import com.example.kalam_android.repository.model.AudioModel

interface SelectAudioCallBack {
    fun selectAudio(view: View, audioModel: AudioModel, position: Int)
}