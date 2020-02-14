package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.*
import io.reactivex.Observable
import io.reactivex.Single
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
    fun getAllMessages(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<ChatDetailResponse>

    @POST(Urls.FIND_FRIENDS)
    @FormUrlEncoded
    fun findFriends(@Header("Authorization") authorization: String?, @FieldMap parameters: Map<String, String>): Observable<FindFriends>

    @Multipart
    @POST(Urls.UPLOAD_AUDIO)
    fun uploadMedia(
        @Header("Authorization") authorization: String?,
        @PartMap params: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part media: MultipartBody.Part
    ): Single<MediaResponse>

    @POST(Urls.UPDATE_FCM_TOKEN)
    fun updateFcm(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<BasicResponse>

    @POST(Urls.GET_PROFILE)
    fun getProfileData(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<UserProfile>

    @POST(Urls.UPDATE_PROFILE)
    fun updateProfile(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<UpdateUserProfile>

    @Multipart
    @POST(Urls.UPDATE_PROFILE)
    fun updateProfile(@Header("Authorization") authorization: String, @PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part image: MultipartBody.Part): Observable<UpdateUserProfile>

    @Multipart
    @POST(Urls.UPDATE_PROFILE)
    fun updateProfile(@Header("Authorization") authorization: String, @PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part profile: MultipartBody.Part, @Part wallImage: MultipartBody.Part): Observable<UpdateUserProfile>

//    @POST(Urls.USER_NAME_UPDATE)
//    fun updateUserName(@Header("Authorization") authorization: String, @Body parameters: Map<String, String>): Observable<BasicResponse>

    @POST(Urls.FORGET_PASSWORD)
    fun forgetPassword(@Body parameters: Map<String, String>): Observable<ForgotResponse>

    @POST(Urls.CREATE_GROUP)
    fun createGroup(@Header("Authorization") authorization: String?, @Body parameters: Map<String, String>): Observable<BasicResponse>

    @POST(Urls.CREATE_GROUP)
    fun createGroup(@Header("Authorization") authorization: String?, @PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part profile: MultipartBody.Part): Observable<BasicResponse>
}
