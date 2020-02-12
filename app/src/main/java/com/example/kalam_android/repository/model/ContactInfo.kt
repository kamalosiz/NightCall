package com.example.kalam_android.repository.model

import com.example.kalam_android.localdb.entities.ContactsData

data class ContactInfo(val name: String, val number: String)

data class Contacts(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ContactList
)

data class ContactList(
    val contacts_list: ArrayList<ContactsData>
)


