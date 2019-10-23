package com.example.kalam_android.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.repository.model.ContactInfo

class AdapterForContacts : RecyclerView.Adapter<ContactViewHolder>() {

    var contactInfo: MutableList<ContactInfo>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {

        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_for_contact_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contactInfo!!.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
    }

    fun updateList(listOfContact: MutableList<ContactInfo>) {

        contactInfo = listOfContact
    }
}

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


}
