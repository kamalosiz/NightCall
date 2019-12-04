package com.example.kalam_android.repository.model

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

data class ContactsData(
    val number: String?,
    val id: Int?,
    val name: String?,
    val profile_image: String?,
    val kalam_number: String?,
    val kalam_name: String?
)