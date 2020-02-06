package com.example.kalam_android.callbacks

import com.example.kalam_android.repository.model.ContactsData

interface OnClickNewGroupContact {
    fun onMyClick(position: Int, list: ArrayList<ContactsData>?)
}