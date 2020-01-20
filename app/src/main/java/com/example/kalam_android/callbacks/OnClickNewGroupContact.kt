package com.example.kalam_android.callbacks

import android.view.View
import com.example.kalam_android.databinding.ItemForNewGroupContactListBinding
import com.example.kalam_android.repository.model.ContactsData

interface OnClickNewGroupContact {
    fun onClickGroupContact(binding: ItemForNewGroupContactListBinding, groupTitle:String, contact:ContactsData)
}