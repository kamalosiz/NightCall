package com.example.kalam_android.repository.model

data class CreateProfileResponse(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: UserData,
    val message: String
)

data class UserData(
    val firstname: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val username: String,
    val updated_at: String,
    val created_at: String,
    val id: Int,
    val token: String,
    val verification_code: Int
)