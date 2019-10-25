package com.example.kalam_android.repository.model

import com.google.gson.annotations.SerializedName

data class CreateProfileResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: UserData?,
    @SerializedName("message")
    val message: String
)
