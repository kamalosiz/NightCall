package com.example.kalam_android.util

import android.content.SharedPreferences
import com.example.kalam_android.repository.model.PhoneModel
import com.example.kalam_android.repository.model.UserData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsHelper @Inject
constructor(private val mSharedPreferences: SharedPreferences) {

    private val KEY_PHONE = "key_phone_kalam"
    private val IMAGE_INDEX = "key_image_index_kalam"
    private val ALL_CHAT_ITEM_SYNCED = "key_all_chat_item_synced"
    private val LANGUAGE = "kalam_user_language"
    private val SELECT_AUTO = "kalam_translate_data"
    private val KEY_IS_NEW_FCM = "key_kalam_is_new_FCM"

    fun put(key: String, value: String) {
        mSharedPreferences.edit().putString(key, value).apply()
    }

    fun put(key: String, value: Int) {
        mSharedPreferences.edit().putInt(key, value).apply()
    }

    fun put(key: String, value: Float) {
        mSharedPreferences.edit().putFloat(key, value).apply()
    }

    fun put(key: String, value: Boolean) {
        mSharedPreferences.edit().putBoolean(key, value).apply()
    }

    operator fun get(key: String, defaultValue: String): String? {
        return mSharedPreferences.getString(key, defaultValue)
    }

    operator fun get(key: String, defaultValue: Int): Int? {
        return mSharedPreferences.getInt(key, defaultValue)
    }

    operator fun get(key: String, defaultValue: Float): Float? {
        return mSharedPreferences.getFloat(key, defaultValue)
    }

    operator fun get(key: String, defaultValue: Boolean): Boolean? {
        return mSharedPreferences.getBoolean(key, defaultValue)
    }

    fun saveIsNewFcmToken(isNewToken: Boolean) {
        put(KEY_IS_NEW_FCM, isNewToken)
    }

    fun isNewFcmToken(): Boolean? {
        return get(KEY_IS_NEW_FCM, false)
    }

    fun saveFCMToken(fcmToken: String) {
        put(AppConstants.FCM_TOKEN, fcmToken)
    }

    fun getFCMToken(): String? {
        return get(AppConstants.FCM_TOKEN, "")
    }

    fun setNumber(phone: String) {
        put(AppConstants.PHONE, phone)
    }

    fun setImageIndex(index: Int) {
        put(IMAGE_INDEX, index)
    }

    fun getNumber(): String? {
        return get(AppConstants.PHONE, "")
    }

    fun getImageIndex(): Int? {
        return get(IMAGE_INDEX, 0)
    }

    fun isLoggedIn(): Boolean {
        return get(AppConstants.KEY_IS_LOGIN, false) ?: false
    }

    fun setUser(user: UserData?) {
        put(AppConstants.KEY_IS_LOGIN, true)
        val json = Gson().toJson(user)
        put(AppConstants.KEY_USER_OBJECT, json)
    }

    fun getUser(): UserData? {
        return try {
            val json = get(AppConstants.KEY_USER_OBJECT, "")
            Gson().fromJson(json, UserData::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun setPhone(phoneModel: PhoneModel?) {
        val json = Gson().toJson(phoneModel)
        put(KEY_PHONE, json)
    }

    fun getPhone(): PhoneModel? {
        return try {
            val json = get(KEY_PHONE, "")
            Gson().fromJson(json, PhoneModel::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun contactsSynced() {
        put(AppConstants.CONTACTS_SYNCED, true)
    }

    fun isContactsSynced(): Boolean {
        return get(AppConstants.CONTACTS_SYNCED, false) ?: false
    }

    fun allChatItemSynced() {
        put(ALL_CHAT_ITEM_SYNCED, true)
    }

    fun isAllChatsItemsSynced(): Boolean {
        return get(ALL_CHAT_ITEM_SYNCED, false) ?: false
    }

    fun saveLanguage(language: String) {
        put(LANGUAGE, language)
    }

    fun getLanguage(): String? {
        return get(LANGUAGE, "")
    }

    fun saveTranslateState(translate: Int) {
        put(SELECT_AUTO, translate)
    }

    fun getTransState(): Int? {
        return get(SELECT_AUTO, 0)
    }
}
