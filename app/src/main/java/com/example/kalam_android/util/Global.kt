package com.example.kalam_android.util

import android.content.Context
import android.graphics.Typeface
import android.media.AudioManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.layout_content_of_chat.view.*
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

    fun showKeyBoard(context: Context, editText: EditText) {
        val imm =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        setFocusCursor(editText)
    }

    private fun setFocusCursor(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
    }

    /* fun turnOFFSpeakers(audioManager: AudioManager) {
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
     }*/
}