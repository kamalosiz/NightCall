package com.example.kalam_android.util

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import com.example.kalam_android.repository.model.MediaList
import java.util.*

open class ImageFetcher(private val context: Context) :
    AsyncTask<Cursor, Void, ArrayList<MediaList>>() {

    override fun doInBackground(vararg cursors: Cursor): ArrayList<MediaList> {
        val listOfAllImages = ArrayList<MediaList>()
        (context as Activity).runOnUiThread {
            listOfAllImages.addAll(getGalleryImagesVideos(context))
        }
        return listOfAllImages
    }
}