package com.example.kalam_android.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ItemForContactListBinding
import com.example.kalam_android.repository.model.ContactInfo

class AdapterForContacts : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var contactInfo: MutableList<ContactInfo>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemForContactListBinding>(
            LayoutInflater.from(parent.context), R.layout.item_for_contact_list, parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return contactInfo?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = contactInfo?.get(position)
        val itemHolder = holder as ContactViewHolder
        itemHolder.binding.tvContactName.text = item?.name
        itemHolder.binding.tvContactPhone.text = item?.number
    }

    fun updateList(listOfContact: MutableList<ContactInfo>) {
        contactInfo?.clear()
        contactInfo = listOfContact
        notifyDataSetChanged()
    }
}

class ContactViewHolder(val binding: ItemForContactListBinding) :
    RecyclerView.ViewHolder(binding.root)
