package com.example.kalam_android.repository.model

data class LoginResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    var data: ArrayList<UserData>,
    var message: String
)

data class UserData(
    val id: Int,
    var firstname: String,
    var lastname: String,
    var email: String,
    var profile_image: String,
    val token: String
)
