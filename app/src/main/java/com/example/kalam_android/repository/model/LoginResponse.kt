package com.example.kalam_android.repository.model

data class LoginResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    var data: UserData?,
    var message: String?
)
