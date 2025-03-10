package com.example.kalam_android.repository.net

class Urls {
    companion object {

        const val WEB_SOCKET_URL = "wss://voip.worldnoordev.com:9090/"
//        const val WEB_SOCKET_URL = "ws://192.168.0.40:9090/"

        //Local IP
//        const val BASE_URL = "http://192.168.0.54:5000/"
        const val BASE_URL = "http://192.168.0.125:5000/"
//        const val BASE_URL = "http://192.168.0.73:5000/"

        //Stage IP
//        const val BASE_URL = "http://184.169.185.200:5000/"
//        const val BASE_URL = "http://184.169.185.200:6000/"

        //Live Ip
//        const val BASE_URL = "http://54.183.109.117:5000/"

        const val VERIFY_PHONE = "api/user/verify_phone"
        const val VERIFY_PHONE_CODE = "api/user/verify_phone_code"
        const val CREATE_PROFILE = "api/user/signup"
        const val SIGN_IN = "api/user/signin"
        const val LOGOUT = "api/user/logout"
        const val VERIFY_CONTACTS = "api/user/sync_contacts"
        const val ALL_CHATS_LIST = "/api/chat/get_all_conversations"
        const val SEARCH_MSG = "/api/chat/search_messages"
        const val ALL_CHATS_MESSAGES = "/api/chat/get_chat_messages"
        const val FIND_FRIENDS = "/api/user/search"
        const val UPDATE_FCM_TOKEN = "/api/user/update_fcm_token"
        const val UPLOAD_AUDIO = "/api/chat/upload_file"
        const val GET_PROFILE = "/api/user/profile"
        const val UPDATE_PROFILE = "/api/user/update_profile"
        //        const val USER_NAME_UPDATE = "/api/user/update_contact_info"
        const val FORGET_PASSWORD = "/api/site/forgot_password"
        const val CREATE_GROUP = "/api/chat/create_group"
    }
}