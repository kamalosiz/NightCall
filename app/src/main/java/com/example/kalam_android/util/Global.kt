package com.example.kalam_android.util

import android.content.Context
import android.graphics.Typeface
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
}