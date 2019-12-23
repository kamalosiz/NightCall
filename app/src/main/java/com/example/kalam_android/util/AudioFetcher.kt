package com.example.kalam_android.util

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import com.example.kalam_android.repository.model.AudioModel
import com.example.kalam_android.repository.model.MediaList
import java.util.ArrayList

open class AudioFetcher(private val context: Context):
    AsyncTask<Cursor, Void, ArrayList<AudioModel>>(){
    override fun doInBackground(vararg params: Cursor?): ArrayList<AudioModel> {
        val audioList = ArrayList<AudioModel>()
        (context as Activity).runOnUiThread {
            audioList.addAll(getAllAudioFromDevice(context)!!)
        }
        return audioList    }

}