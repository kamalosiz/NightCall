package com.example.kalam_android.wrapper

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target


object GlideDownloader {

    /*fun load(context: Context, view: ImageView, resource: Int) {
        Glide.with(context)
            .load(resource)
            .into(view)
    }*/

/*    fun load(
        context: Context?,
        view: ImageView?,
        resource: String?,
        error: Int,
        placeholder: Int,
        progressBar: ProgressBar
    ) {
        context?.let {
            view?.let { it1 ->
                Glide.with(it)
                    .load(resource) // Uri of the picture
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                    })
                    .error(error)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(it1)
            }
        }
    }*/

    fun load(
        context: Context?,
        view: ImageView?,
        resource: String?,
        error: Int,
        placeholder: Int
    ) {
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
