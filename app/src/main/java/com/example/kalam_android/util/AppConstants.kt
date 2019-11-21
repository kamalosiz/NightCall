package com.example.kalam_android.util

object AppConstants {
    const val VERIFICATION_CODE = "key_phone_verfiy"
    const val PROFILE_IMAGE_KEY = "profileImage_kalam"
    const val USER_NAME = "user_name_kalam"
    const val RECEIVER_ID = "user_receiver_id"
    const val IS_FROM_CHAT_FRAGMENT = "is_chat_boolean"
    const val CHAT_USER_NAME = "chat_user_name"
    const val CHAT_USER_PICTURE = "chat_user_picture"
    const val CHAT_ID = "kalam_chat_id"
    const val DUMMY_DATA = -1
    const val DUMMY_STRING = ""
    const val TEXT_MESSAGE = "text"
    const val AUDIO_MESSAGE = "audio"
    const val IMAGE_MESSAGE = "image"
    const val VIDEO_MESSAGE = "video"
    const val IS_FROM_CONTACTS = "inFromContactsAdapter"
    const val FCM_TOKEN = "fcm_token"
    const val LAST_MESSAGE = "lastmessage"
    const val LAST_MESSAGE_TIME = "lastmsgtime"
    const val IsSEEN = "isseen"
    const val CHAT_FILE = "chatfile"
    const val CHAT_TYPE = "chattype"


    //Request Code
    const val PROFILE_IMAGE_CODE = 111
    const val CHAT_FRAGMENT_CODE = 1000
    const val LOGOUT_CODE = 1001
    const val CHAT_IMAGE_CODE = 1002

    //Local DB=
    const val DB_NAME = "kalam_local_db"
    const val CONTACTS_TABLE = "contacts"
    const val ALL_CHAT_ENTITY = "allchat"

    //Socket Actions
    const val START_CAHT = "startChat"
    const val SEND_MESSAGE = "send_message"
    const val START_TYPING = "start_typing"
    const val STOP_TYPING = "stop_typing"
    const val UPDATE_SETTINGS = "update_settings"

    //Listeners
    const val MESSAGE_TYPING = "message_typing"
    const val NEW_MESSAGE = "new_message"
    const val MESSAGE_STOPS_TYPING = "message_stop_typing"

}