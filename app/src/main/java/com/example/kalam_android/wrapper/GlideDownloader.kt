package com.example.kalam_android.wrapper

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


object GlideDownloader {

    /*fun load(context: Context, view: ImageView, resource: Int) {
        Glide.with(context)
            .load(resource)
            .into(view)
    }*/

    fun load(context: Context?, view: ImageView?, resource: String?, error: Int, placeholder: Int) {
        context?.let {
            view?.let { it1 ->
                Glide.with(it)
                    .load(resource) // Uri of the picture
                    .error(error)
                    .placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(it1)
            }
        }
    }

    /*fun load(context: Context?, view: ImageView, resource: String, options: RequestOptions) {
        if (context != null) {
            Glide.with(context)
                .load(resource)
                .thumbnail(0.5f)
                .apply(options)
                .into(view)
        }
    }*/
}
