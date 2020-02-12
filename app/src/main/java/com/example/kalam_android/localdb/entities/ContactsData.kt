package com.example.kalam_android.localdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kalam_android.util.AppConstants
import java.io.Serializable

@Entity(tableName = AppConstants.CONTACTS_TABLE)
data class ContactsData(
    @PrimaryKey(autoGenerate = true)
    var primaryId: Int,
    @ColumnInfo(name = "number") var number: String,
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "profile_image") var profile_image: String?,
    @ColumnInfo(name = "kalam_number") var kalam_number: String?,
    @ColumnInfo(name = "kalam_name") var kalam_name: String?,
    @ColumnInfo(name = "is_selected") var is_selected: Boolean = false
) : Serializable