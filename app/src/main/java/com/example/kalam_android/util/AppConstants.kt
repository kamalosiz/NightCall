package com.example.kalam_android.util

object AppConstants {
    const val VERIFICATION_CODE = "key_phone_verfiy"
    const val PROFILE_IMAGE_KEY = "profileImage_kalam"
    const val USER_NAME = "user_name_kalam"
    const val IS_CHATID_AVAILABLE = "is_chat_boolean"
    const val CALLER_USER_ID = "caller_user_id"
    const val INITIATOR = "caller_initiator"
    const val IS_FROM_OUTSIDE = "is_outside_boolean"
    const val IS_FROM_PUSH = "is_call_boolean"
    const val CHAT_USER_NAME = "chat_user_name"
    const val CHAT_USER_PICTURE = "chat_user_picture"
    const val CHAT_ID = "kalam_chat_id"
    const val DUMMY_DATA = -1
    const val DUMMY_STRING = ""
    const val TEXT_MESSAGE = "text"
    const val AUDIO_MESSAGE = "audio"
    const val IMAGE_MESSAGE = "image"
    const val VIDEO_MESSAGE = "video"
    const val LOCATION_MESSAGE = "location"
    const val IS_FROM_CONTACTS = "inFromContactsAdapter"
    const val FCM_TOKEN = "fcm_token"
    const val CHAT_FILE = "chatfile"
    const val CHAT_TYPE = "chattype"
    const val SELECTED_IMAGES_VIDEOS = "selected_images_videos"
    const val KEY_USER_OBJECT = "kalam_user"
    const val CONTACTS_SYNCED = "key_contacts_synced"
    const val KEY_IS_LOGIN = "is_logged_in_kalam"
    const val PHONE = "key_phoneno"
    const val CONNECTED_USER_ID = "connectedUserId"
    const val FIREBASE_CHAT_ID = "chat_id"
    const val MSG_ID = "msgidto_send"
    const val FROM_SEARCH = "is_from_search"
    const val SENDER_NAME = "sender_name"
    const val USER_DATA = "user_data"
    const val KALAM_CONTACT_LIST = "kalam_contact_list"
    const val IS_NULL = "response_null_kalam"
    const val JSON = "webrtc_json"
    const val IS_VIDEO_CALL = "isVideoCall"
    const val LOCATION_KEY = "key_location_kalam"
    const val IS_FORWARD_MESSAGE = "key_is_forward_message"
    const val SELECTED_MSGS_IDS = "key_selected_messages_list"

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
    const val CODE_FROM_PUSH = 1010
    const val GROUP_IMAGE_CODE = 1011
    const val FORWARD_REQUEST_CODE = 1032

    //Local DB
    const val DB_NAME = "kalam_local_db"
    const val CONTACTS_TABLE = "contacts"
    const val ALL_CHAT_ENTITY = "allchat"
    const val CHAT_MESSAGES = "chat_messages"

    //SocketIO Actions
    const val START_CAHT = "startChat"
    const val SEND_MESSAGE = "send_message"
    const val START_TYPING = "start_typing"
    const val STOP_TYPING = "stop_typing"
    const val UPDATE_SETTINGS = "update_settings"
    const val READ_ALL_MESSAGES = "read_all_messages"
    const val MESSAGE_SEEN = "message_seen"
    const val SEEN_MESSAGE = "seen_message"
    const val USER_STATUS = "user_status"
    const val GET_MY_NICKNAME = "get_my_nickname"
    const val CHECK_USER_STATUS = "check_user_status"
    const val GET_ALL_USER_STATUS = "get_all_user_status"
    const val DELETE_MESSAGE = "delete_message"
    const val MESSAGE_TYPING = "message_typing"
    const val NEW_MESSAGE = "new_message"
    const val MESSAGE_STOPS_TYPING = "message_stop_typing"
    const val ALL_MESSAGES_READ = "all_messages_read"
    const val MESSAGE_DELIVERED = "messages_delivered"
    const val MESSAGE_DELETED = "message_deleted"
    const val EDIT_MESSAGE = "edit_message"
    const val SEND_LOCATION = "sendlocation"
    const val FORWARD_MESSAGE = "forward_message"

    //WebRtc WebSockets
    const val CANDIDATE = "candidate"
    const val ANSWER = "answer"
    const val OFFER = "offer"
    const val LOGIN = "login"
    const val TYPE = "type"
    const val REJECT = "reject"
    const val READY_FOR_CALL = "readyforcall"

    const val NEW_CALL = "newcall"
}
