package com.example.kalam_android.callbacks

import android.view.View
import com.example.kalam_android.localdb.entities.ContactsData

interface MyClickListener {
    fun myOnClick(view: View, position: Int)
}

interface SelectItemListener {
    fun itemListener(view: View, position: Int, isLongClick: Boolean)
}

interface OnClickNewGroupContact {
    fun onMyClick(position: Int, list: ArrayList<ContactsData>?)
}