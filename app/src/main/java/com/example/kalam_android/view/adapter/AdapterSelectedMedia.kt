package com.example.kalam_android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.*
import com.example.kalam_android.databinding.ItemForSelectedMediaBinding
import com.example.kalam_android.repository.model.MediaList
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import java.util.*

class AdapterSelectedMedia(val context: Context, val list: ArrayList<MediaList>) :
    RecyclerView.Adapter<AdapterSelectedMedia.SelectedMediaHolder>(),
    ItemTouchHelperAdapter {
    private var myClickListener: MyClickListener? = null
    private var selectedItemCallBack: SelectedItemCallBack? = null
    private var onStartDragListener: OnStartDragListener? = null
    private val TAG = this.javaClass.simpleName
    private var removeItemCallBack: RemoveItemCallBack? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaHolder {
        return SelectedMediaHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_for_selected_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setMyClickListener(myClickListener: MyClickListener) {
        this.myClickListener = myClickListener
    }

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener) {
        this.onStartDragListener = onStartDragListener
    }

    fun setSelectedItemCallBack(selectedItemCallBack: SelectedItemCallBack) {

        this.selectedItemCallBack = selectedItemCallBack
    }

    override fun onBindViewHolder(holder: SelectedMediaHolder, position: Int) {

        when (list[position].type) {

            AppConstants.IMAGE_GALLERY -> {
                holder.binding.selectedVideoHolder.rlVideo.visibility = View.GONE
                holder.binding.selectedAudioHolder.rlAudio.visibility = View.GONE
                holder.binding.selectedImagesHolder.llSelectedImage.visibility = View.VISIBLE
                holder.binding.selectedImagesHolder.ivSelectedImages.let {
                    Glide.with(context)
                        .load(list[position].file)
                        .placeholder(R.drawable.dummy_placeholder_1)
                        .into(it)
                }

                holder.binding.selectedImagesHolder.ivCancel.setOnClickListener {
                    removeItemCallBack?.onRemoveItem(list[position])
                }
                holder.binding.selectedImagesHolder.ivSelectedImages.setOnClickListener {

                    myClickListener?.myOnClick(it, position)
                }

            }
            AppConstants.POST_VIDEO -> {
                holder.binding.selectedVideoHolder.rlVideo.visibility = View.VISIBLE
                holder.binding.selectedAudioHolder.rlAudio.visibility = View.GONE
                holder.binding.selectedImagesHolder.llSelectedImage.visibility = View.GONE
                holder.binding.selectedVideoHolder.ivImage.let {
                    Glide.with(context)
                        .load(list[position].file)
                        .placeholder(R.drawable.dummy_placeholder_1)
                        .into(it)
                }
                holder.binding.selectedVideoHolder.ivImage.setOnClickListener {

                    myClickListener?.myOnClick(it, position)
                }
                holder.binding.selectedVideoHolder.ivCancel.setOnClickListener {
                    removeItemCallBack?.onRemoveItem(list[position])
                }

            }
            AppConstants.AUDIO_GALLERY->{
                holder.binding.selectedVideoHolder.rlVideo.visibility = View.GONE
                holder.binding.selectedAudioHolder.rlAudio.visibility = View.VISIBLE
                holder.binding.selectedImagesHolder.llSelectedImage.visibility = View.GONE
                holder.binding.selectedAudioHolder.ivImage.setOnClickListener {

                    myClickListener?.myOnClick(it, position)
                }
                holder.binding.selectedAudioHolder.ivCancel.setOnClickListener {
                    removeItemCallBack?.onRemoveItem(list[position])
                }
            }
        }



        holder.binding.selectedImagesHolder.llSelectedImage.setOnLongClickListener {
            onStartDragListener?.onStartDrag(holder)
            return@setOnLongClickListener true
        }
    }

    fun setRemoveItemCallBack(removeItemCallBack: RemoveItemCallBack) {
        this.removeItemCallBack = removeItemCallBack
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < list.size && toPosition < list.size) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(list, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(list, i, i - 1)
                }
            }
        }

        logE("Move Item : $list")
        notifyItemMoved(fromPosition, toPosition)
        selectedItemCallBack?.selectedItem(list, fromPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        logE("dismiss : $position")
    }

    inner class SelectedMediaHolder(val binding: ItemForSelectedMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun logE(msg: String) {
        Debugger.e(TAG, msg)
    }
}