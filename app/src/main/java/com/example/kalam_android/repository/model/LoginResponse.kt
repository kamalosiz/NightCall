package com.example.kalam_android.repository.model

data class LoginResponse(
    val action: String,
    val meta: Meta,
    val data: LoginData?
)

data class LoginData(
    val id: Int,
    var email_verified_at: String,
    var profile_image: String,
    var cover_image: String,
    var dob: String,
    var city: String,
    var about_me: String,
    var gender: String,
    var created_at: String,
    var updated_at: String,
    var api_token: String,
    var firstname: String,
    var lastname: String,
    var email: String,
    var phone: String,
    var country_id: String,
    var state_id: String,
    var language_id: String,
    var fr_accepted_notifications_count: Int,
    var website: String,
    var is_online: Int,
    var username: String
)

data class Meta(
    val code: Int,
    val message: String
)