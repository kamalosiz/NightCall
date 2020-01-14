package com.example.kalam_android.util

object AppConstants {
    const val VERIFICATION_CODE = "key_phone_verfiy"
    const val PROFILE_IMAGE_KEY = "profileImage_kalam"
    const val USER_NAME = "user_name_kalam"
    const val RECEIVER_ID = "user_receiver_id"
    const val IS_CHATID_AVAILABLE = "is_chat_boolean"
    const val CALLER_USER_ID = "caller_user_id"
    const val INITIATOR = "caller_initiator"
    const val IS_FROM_OUTSIDE = "is_outside_boolean"
    const val CHAT_USER_NAME = "chat_user_name"
    const val CHAT_USER_ID = "chat_user_id"
    const val CHAT_USER_PICTURE = "chat_user_picture"
    const val CHAT_ID = "kalam_chat_id"
    const val DUMMY_DATA = -1
    const val DUMMY_STRING = ""
    const val TEXT_MESSAGE = "text"
    const val AUDIO_MESSAGE = "audio"
    const val IMAGE_MESSAGE = "image"
    const val GROUP = "group"
    const val VIDEO_MESSAGE = "video"
    const val IS_FROM_CONTACTS = "inFromContactsAdapter"
    const val FCM_TOKEN = "fcm_token"
    const val LAST_MESSAGE = "lastmessage"
    const val LAST_MESSAGE_TIME = "lastmsgtime"
    const val IsSEEN = "isseen"
    const val CHAT_FILE = "chatfile"
    const val CHAT_TYPE = "chattype"
    const val SELECTED_IMAGES_VIDEOS = "selected_images_videos"
    const val KEY_USER_OBJECT = "kalam_user"
    const val CONTACTS_SYNCED = "key_contacts_synced"
    const val KEY_IS_LOGIN = "is_logged_in_kalam"
    const val PHONE = "key_phoneno"
    const val CONNECTED_USER_ID = "connectedUserId"
    const val IMAGE_DIRECTORY = "/demonuts"
    const val FIREBASE_CHAT_ID = "chat_id"
    const val MSG_ID = "msgidto_send"
    const val FROM_SEARCH = "is_from_search"
    const val SENDER_NAME = "sender_name"
    const val USER_DATA = "user_data"
    const val KALAM_CONTACT_LIST = "kalam_contact_list"

    //Request Code
    const val IMAGE_GALLERY = 0
    const val POST_VIDEO = 1
    const val AUDIO_GALLERY = 2
    const val PROFILE_IMAGE_CODE = 111
    const val CHAT_FRAGMENT_CODE = 1000
    const val LOGOUT_CODE = 1001
    const val SELECTED_IMAGES = 12
    const val SELECT_IMAGES_VIDEOS = 14
    const val SELECT_AUDIO = 15
    const val UPDATE_PROFILE = 11
    const val CAMERA = 150
    const val GALLERY = 160



    //Local DB=
    const val DB_NAME = "kalam_local_db"
    const val CONTACTS_TABLE = "contacts"
    const val ALL_CHAT_ENTITY = "allchat"

    //SocketIO Actions
    const val START_CAHT = "startChat"
    const val SEND_MESSAGE = "send_message"
    const val START_TYPING = "start_typing"
    const val STOP_TYPING = "stop_typing"
    const val UPDATE_SETTINGS = "update_settings"
    const val READ_ALL_MESSAGES = "read_all_messages"
    const val MESSAGE_SEEN = "message_seen"
    const val SEEN_MESSAGE = "seen_message"

    //WebRtc WebSockets
    const val CANDIDATE = "candidate"
    const val ANSWER = "answer"
    const val OFFER = "offer"
    const val LOGIN = "login"
    const val TYPE = "type"
    const val REJECT = "reject"
    const val AVAILABLE = "available"
    const val JSON = "webrtc_json"

    //Listeners
    const val MESSAGE_TYPING = "message_typing"
    const val NEW_MESSAGE = "new_message"
    const val MESSAGE_STOPS_TYPING = "message_stop_typing"
    const val ALL_MESSAGES_READ = "all_messages_read"
    const val MESSAGE_DELIVERED = "messages_delivered"
}
