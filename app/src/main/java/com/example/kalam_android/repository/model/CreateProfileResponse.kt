package com.example.kalam_android.repository.model

data class CreateProfileResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: Any,
    val message: String
)
