package com.example.kalam_android.repository.model

data class UserProfile(val action: String, val code: Int, val status: String, val data: ArrayList<ProfileData>?, val message: String)

data class ProfileData(
        val nickname: String,
        val id: Int,
        val firstname: String,
        val lastname: String,
        val email: String,
        val country: String,
        val country_code: String,
        val phone: String,
        val profile_image: String
)