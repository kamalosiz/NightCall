package com.example.kalam_android.repository.model

data class Logout(
    val action: String,
    val code: Int,
    val status: Boolean,
    val data: String,
    val message: String
)