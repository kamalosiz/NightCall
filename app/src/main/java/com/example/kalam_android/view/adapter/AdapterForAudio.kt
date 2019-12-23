package com.example.kalam_android.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.callbacks.SelectAudioCallBack
import com.example.kalam_android.databinding.ItemForAudioBinding
import com.example.kalam_android.repository.model.AudioModel

class AdapterForAudio(val context: Context, val list: ArrayList<AudioModel>) :
    RecyclerView.Adapter<AdapterForAudio.AudioViewHolder>() {
    private var selectAudioCallBack:SelectAudioCallBack?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return AudioViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_for_audio,
                parent,
                false
            )
        )
    }
    fun setSelectAudioCallBack(selectAudioCallBack:SelectAudioCallBack){
        this.selectAudioCallBack = selectAudioCallBack
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.binding.tvAudioTitle.text = list[position].aName
        holder.binding.tvAudioDurationSize.text =
            list[position].duration + " . " + list[position].audioLength
        holder.binding.rlAudio.setOnClickListener {

            selectAudioCallBack?.selectAudio(holder.itemView,list[position],position)
        }
    }

    inner class AudioViewHolder(val binding: ItemForAudioBinding) :
        RecyclerView.ViewHolder(binding.root)
}