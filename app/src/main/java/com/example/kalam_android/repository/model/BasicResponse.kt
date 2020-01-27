package com.example.kalam_android.repository.model

data class BasicResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: String,
    val message: String
)

data class StatusResponse(
    val user_id: Int,
    val status: Int
)