package com.example.kalam_android.callbacks

import android.view.View
import android.widget.ImageView
import com.example.kalam_android.databinding.ItemForNewGroupContactListBinding
import com.example.kalam_android.repository.model.ContactsData

interface OnClickNewGroupContact {
    fun addToList(contact: ContactsData)
    fun removeToList(contact: ContactsData)
    fun groupName(groupTitle: String)
}