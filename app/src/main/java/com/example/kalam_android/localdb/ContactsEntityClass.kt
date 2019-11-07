package com.example.kalam_android.localdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalam_android.util.AppConstants

@Entity(tableName = AppConstants.CONTACTS_TABLE)
data class ContactsEntityClass(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "number") var number: String?,
    @ColumnInfo(name = "contact_id") var contact_id: Int?,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "profile_image") var profile_image: String?,
    @ColumnInfo(name = "kalam_number") var kalam_number: String?

)