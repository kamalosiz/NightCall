package com.example.kalam_android.callbacks

import android.view.View
import com.example.kalam_android.repository.model.MediaList

interface ResultVoiceToText {

    fun onResultVoiceToText(list: ArrayList<String>)
}