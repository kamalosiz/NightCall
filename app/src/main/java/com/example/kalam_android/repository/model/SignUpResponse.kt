package com.example.kalam_android.repository.model

data class SignUpResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: Data?,
    val message: String
)

data class Data(
    val verification_code: Int
)