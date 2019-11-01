package com.example.kalam_android.repository.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

data class FindFriends(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: ArrayList<FindFriendList>?,
    val message: String
)


data class FindFriendList(
    val id: Int,
    val firstname: String,
    val lastname: String,
    val profile_image: String,
    val country: String
)