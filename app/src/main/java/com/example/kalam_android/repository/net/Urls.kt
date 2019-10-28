package com.example.kalam_android.repository.net

class Urls {
    companion object {
//        Live Base URL: http://184.169.185.200:5000/
        const val BASE_URL = "http://184.169.185.200:5000/"
//        const val BASE_URL = "http://192.168.0.71:5000/"
        const val VERIFY_PHONE = "api/user/verify_phone"
        const val VERIFY_PHONE_CODE = "api/user/verify_phone_code"
        const val CREATE_PROFILE = "api/user/signup"
        const val SIGN_IN = "api/user/signin"
        const val VERIFY_CONTACTS = "api/user/sync_contacts"
    }
}