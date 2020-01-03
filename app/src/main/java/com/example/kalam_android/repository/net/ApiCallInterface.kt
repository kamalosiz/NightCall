package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.*
import com.example.kalam_android.services.WorkManagerMedia
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiCallInterface {

    @POST(Urls.VERIFY_PHONE)
    fun signUp(@Body parameters: Map<String, String>): Observable<SignUpResponse>

    @POST(Urls.VERIFY_PHONE_CODE)
    fun verifyCode(@Body parameters: Map<String, String>): Observable<BasicResponse>

    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@Body parameters: Map<String, String>): Observable<CreateProfileResponse>

    @Multipart
    @POST(Urls.CREATE_PROFILE)
    fun createProfile(
        @PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePic: MultipartBody.Part
    ): Observable<CreateProfileResponse>

    @POST(Urls.SIGN_IN)
    fun login(@Body parameters: Map<String, String>): Observable<LoginResponse>


    @POST(Urls.LOGOUT)
    fun logout(@Header("Authorization") authorization: String?): Observable<BasicResponse>


    @POST(Urls.VERIFY_CONTACTS)
    fun getContacts(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<Contacts>

    @POST(Urls.ALL_CHATS_LIST)
    fun getAllChats(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<AllChatListResponse>

    @POST(Urls.SEARCH_MSG)
    fun getSearchMessage(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<AllChatListResponse>


    @POST(Urls.ALL_CHATS_MESSAGES)
    fun getAllMessages(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<ChatMessagesResponse>

    @POST(Urls.FIND_FRIENDS)
    @FormUrlEncoded
    fun findFriends(@Header("Authorization") authorization: String?, @FieldMap parameters: Map<String, String>): Observable<FindFriends>

    @Multipart
    @POST(Urls.UPLOAD_AUDIO)
    fun uploadMedia(
        @Header("Authorization") authorization: String?,
        @PartMap params: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part media: MultipartBody.Part
    ): Observable<MediaResponse>

    /*@Multipart
    @POST(Urls.UPLOAD_AUDIO)
    fun uploadMedia(
        @Header("Authorization") authorization: String?,
        @PartMap params: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part list: ArrayList<MultipartBody.Part>
    ): Observable<MediaResponse>*/

    @POST(Urls.UPDATE_FCM_TOKEN)
    fun updateFcm(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<BasicResponse>

    @POST(Urls.GET_PROFILE)
    fun getProfileData(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<UserProfile>

}
