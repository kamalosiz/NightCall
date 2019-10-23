package com.example.kalam_android.util

import android.content.SharedPreferences
import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.model.UserData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsHelper @Inject
constructor(private val mSharedPreferences: SharedPreferences) {
    val PHONE = "key_phoneno"
    private val KEY_USER_OBJECT = "kalam_user"
    private val KEY_IS_LOGIN = "is_logged_in_kalam"

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

    fun savePhoneNo(phone: String) {
        put(PHONE, phone)
    }

    fun getPhoneNo(): String? {
        return get(PHONE, "")
    }

    fun isLoggedIn(): Boolean {
        return get(KEY_IS_LOGIN, false) ?: false
    }

    fun setUser(user: UserData?) {
        put(KEY_IS_LOGIN, true)
        val json = Gson().toJson(user)
        put(KEY_USER_OBJECT, json)
    }

    fun getUser(): UserData? {
        return try {
            val json = get(KEY_USER_OBJECT, "")
            Gson().fromJson(json, UserData::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}
