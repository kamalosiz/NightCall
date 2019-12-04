package com.example.kalam_android.util

object AppConstants {
    const val VERIFICATION_CODE = "key_phone_verfiy"
    const val PROFILE_IMAGE_KEY = "profileImage_kalam"
    const val USER_NAME = "user_name_kalam"
    const val RECEIVER_ID = "user_receiver_id"
    const val IS_FROM_CHAT_FRAGMENT = "is_chat_boolean"
    const val IS_FROM_CHAT_OUTSIDE = "is_outside_boolean"
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
    const val POST_AUDIO = "post_audio"
    const val SELECTED_IMAGES_VIDEOS = "selected_images_videos"
    const val KEY_USER_OBJECT = "kalam_user"
    const val CONTACTS_SYNCED = "key_contacts_synced"
    const val KEY_IS_LOGIN = "is_logged_in_kalam"
    const val PHONE = "key_phoneno"

    val NOTIFICATION_TYPE = "notification_type"
    val CONTENT_TYPE = "content_type"
    val FIREBASE_CHAT_ID = "chat_id"
    val NOTIFICATION_TYPE_POST = "1"
    val GROUP_KALAM = "com.kalam.group"
    val NOTIFICATION_BODY = "message"
    val SENDER_NAME = "sender_name"


    //Request Code
    const val IMAGE_GALLERY = 0
    const val POST_VIDEO = 1
    const val PROFILE_IMAGE_CODE = 111
    const val CHAT_FRAGMENT_CODE = 1000
    const val LOGOUT_CODE = 1001
    const val CHAT_IMAGE_CODE = 1002
    const val STATUS_IMAGE_CODE = 112
    const val SELECTED_IMAGES = 12
    val REQ_CODE_SPEECH_INPUT=222


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
    const val READ_ALL_MESSAGES = "read_all_messages"

    //Listeners
    const val MESSAGE_TYPING = "message_typing"
    const val NEW_MESSAGE = "new_message"
    const val MESSAGE_STOPS_TYPING = "message_stop_typing"
    const val ALL_MESSAGES_READ = "all_messages_read"
    const val MESSAGE_DELIVERED = "messages_delivered"

}