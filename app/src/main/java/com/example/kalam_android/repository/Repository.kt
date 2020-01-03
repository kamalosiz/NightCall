package com.example.kalam_android.repository

import com.example.kalam_android.repository.model.*
import com.example.kalam_android.repository.net.ApiCallInterface
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.PartMap

class Repository(private val apiCallInterface: ApiCallInterface) {

    fun executeSignup(parameters: Map<String, String>): Observable<SignUpResponse> {
        return apiCallInterface.signUp(parameters)
    }

    fun executeVerifyCode(parameters: Map<String, String>): Observable<BasicResponse> {
        return apiCallInterface.verifyCode(parameters)
    }

    fun createProfile(@Body parameters: Map<String, String>): Observable<CreateProfileResponse> {
        return apiCallInterface.createProfile(parameters)
    }

    fun createProfile(@PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part profilePic: MultipartBody.Part): Observable<CreateProfileResponse> {
        return apiCallInterface.createProfile(parameters, profilePic)
    }

    fun login(parameters: Map<String, String>): Observable<LoginResponse> {
        return apiCallInterface.login(parameters)
    }

    fun logout(authorization: String?): Observable<BasicResponse> {
        return apiCallInterface.logout(authorization)
    }

    fun getContacts(authorization: String?, parameters: Map<String, String>): Observable<Contacts> {
        return apiCallInterface.getContacts(authorization, parameters)
    }

    fun getAllChat(
        authorization: String?,
        parameters: Map<String, String>
    ): Observable<AllChatListResponse> {
        return apiCallInterface.getAllChats(authorization, parameters)
    }

    fun getSearchMessage(
        authorization: String?,
        parameters: Map<String, String>
    ): Observable<AllChatListResponse> {
        return apiCallInterface.getSearchMessage(authorization, parameters)
    }

    fun getAllMessages(
        authorization: String?,
        parameters: Map<String, String>
    ): Observable<ChatMessagesResponse> {
        return apiCallInterface.getAllMessages(authorization, parameters)
    }

    fun findFriends(
        authorization: String?,
        parameters: Map<String, String>
    ): Observable<FindFriends> {
        return apiCallInterface.findFriends(authorization, parameters)
    }

    fun uploadMedia(
        authorization: String?,
        @PartMap params: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part media: MultipartBody.Part
    ): Observable<MediaResponse> {
        return apiCallInterface.uploadMedia(authorization, params, media)
    }

   /* fun uploadMedia(
        authorization: String?,
        @PartMap params: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part list: ArrayList<MultipartBody.Part>
    ): Observable<MediaResponse> {
        return apiCallInterface.uploadAudio(authorization, params, list)
    }*/

    fun updateFcm(
        authorization: String?,
        parameters: Map<String, String>
    ): Observable<BasicResponse> {
        return apiCallInterface.updateFcm(authorization, parameters)
    }
    fun getProfileData(
            authorization: String?,
            parameters: Map<String, String>
    ): Observable<UserProfile> {
        return apiCallInterface.getProfileData(authorization, parameters)
    }
}