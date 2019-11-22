package com.example.kalam_android.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.OnGalleryItemClickedListener
import com.example.kalam_android.repository.model.MediaList


class GalleryImageAdapter(
    val context: Context,
    val list: ArrayList<MediaList>,
    val listener: OnGalleryItemClickedListener
) : RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(context)
                .inflate(
                    R.layout.gallery_image_item,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position].type == 1) {
            holder.ivVideoImage.visibility = View.VISIBLE
        } else {
            holder.ivVideoImage.visibility = View.GONE

        }
        holder.ivGalleryImage.let {
            Glide.with(context)
                .load(list[position].file)
                .centerCrop()
                .into(it)
        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            Log.i("onGalleryItemClicked", "clicked at position $position")
            listener.onGalleryItemClicked(list, holder.itemView, position)
        })
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGalleryImage = itemView.findViewById<ImageView>(R.id.ivGalleryImage)
        val ivVideoImage = itemView.findViewById<ImageView>(R.id.ivVideo)

    }
}