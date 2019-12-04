package com.example.kalam_android.repository.net

class Urls {
    companion object {
        const val BASE_URL = "http://192.168.0.71:5000/"
        //        const val BASE_URL = "http://184.169.185.200:5000/"
        const val VERIFY_PHONE = "api/user/verify_phone"
        const val VERIFY_PHONE_CODE = "api/user/verify_phone_code"
        const val CREATE_PROFILE = "api/user/signup"
        const val SIGN_IN = "api/user/signin"
        const val VERIFY_CONTACTS = "api/user/sync_contacts"
        const val ALL_CHATS_LIST = "/api/chat/get_all_conversations"
        const val ALL_CHATS_MESSAGES = "/api/chat/get_chat_messages"
        const val FIND_FRIENDS = "/api/user/search"
        const val UPLOAD_AUDIO = "/api/chat/upload_file"
    }
}