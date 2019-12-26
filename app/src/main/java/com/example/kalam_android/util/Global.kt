package com.example.kalam_android.util

import android.content.Context
import android.graphics.Typeface
import android.media.AudioManager
import androidx.core.content.ContextCompat
import java.lang.reflect.Type


object Global {
    var currentChatID = -1
    fun setColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(
            context,
            color
        )
    }

    fun changeText(context: Context, font: Int): Typeface {
        var typeface: Typeface = Typeface.DEFAULT
        if (font == 0) {
            typeface = Typeface.createFromAsset(context.assets, "fonts/amiri_regular.ttf")
        } else if (font == 1) {
            typeface = Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf")
        }
        return typeface
    }

    fun turnOFFSpeakers(audioManager: AudioManager) {
        try {
            if (audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.mode = AudioManager.STREAM_MUSIC
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun turnOnSpeakers(audioManager: AudioManager) {
        try {
            if (!audioManager.isSpeakerphoneOn) {
                audioManager.isSpeakerphoneOn = true
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}