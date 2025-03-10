package com.example.kalam_android.repository.model

data class LoginResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    var data: UserData?,
    var message: String?
)

data class UserData(
    var firstname: String,
    var lastname: String,
    var email: String,
    var country: String,
    var country_code: String,
    var phone: String,
    var profile_image: String,
    val token: String,
    val id: Int,
    val language: String?,
    val auto_translate: Int?
)
