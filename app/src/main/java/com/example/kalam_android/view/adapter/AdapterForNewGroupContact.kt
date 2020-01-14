package com.example.kalam_android.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForAddGroupNameBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactBinding
import com.example.kalam_android.databinding.ItemForNewGroupContactListBinding

class AdapterForNewGroupContact : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_HEADER = 0
    private val VIEW_ITEM = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_HEADER) {
            NewGroupNameViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_for_add_group_name, parent, false))
        } else {
            NewGroupContactViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_for_new_group_contact_list, parent, false))
        }
    }

    override fun getItemCount(): Int {
//        return (contactList?.size?.plus(1) ?: 0)
        return 10
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(holder.adapterPosition)) {
            VIEW_HEADER -> {
            }
            VIEW_ITEM -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_HEADER
        } else {
            VIEW_ITEM
        }
    }

    inner class NewGroupNameViewHolder(binding: ItemForAddGroupNameBinding) : RecyclerView.ViewHolder(binding.root)

    inner class NewGroupContactViewHolder(binding: ItemForNewGroupContactListBinding) : RecyclerView.ViewHolder(binding.root)
}