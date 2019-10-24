package com.example.kalam_android.repository.model

data class ContactInfo(val name: String, val number: String)

data class Contacts(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ContactsData
)

data class ContactsData(
    val contacts_list: ArrayList<ContactsList>
)

data class ContactsList(
    val number: String,
    val id: Int,
    val name: String,
    val profile_image: String
)