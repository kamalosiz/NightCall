package com.example.kalam_android.util

import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import com.example.kalam_android.repository.model.MediaList
import java.util.*

open class ImageFetcher(private val context: Context) : AsyncTask<Cursor, Void, ArrayList<MediaList>>() {

    override fun doInBackground(vararg cursors: Cursor): ArrayList<MediaList> {
        val listOfAllImages = ArrayList<MediaList>()
        listOfAllImages.addAll(getAllShownImagesPath(context))
        listOfAllImages.addAll(getAllShownVideosPath(context))
        return listOfAllImages
    }
}