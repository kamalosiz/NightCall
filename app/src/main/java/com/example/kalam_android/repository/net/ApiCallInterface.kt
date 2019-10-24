package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiCallInterface {

    @POST(Urls.VERIFY_PHONE)
    fun signUp(@Body parameters: Map<String, String>): Observable<SignUpResponse>

    @POST(Urls.VERIFY_PHONE_CODE)
    fun verifyCode(@Body parameters: Map<String, String>): Observable<VerifyCodeResponse>

    /*
        @POST(Urls.CREATE_PROFILE)
        fun createProfile(@QueryMap parameters: Map<String, String>): Observable<CreateProfileResponse>*/
    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>): Observable<CreateProfileResponse>

//    @Multipart
//    @POST(Urls.CREATE_PROFILE)
//    fun createProfile(@QueryMap parameters: Map<String, String>, @Part profilePic: MultipartBody.Part): Observable<CreateProfileResponse>

    @Multipart
    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part profilePic: MultipartBody.Part): Observable<CreateProfileResponse>

    @POST(Urls.SIGN_IN)
    fun login(@Body parameters: Map<String, String>): Observable<LoginResponse>

    @POST(Urls.VERIFY_CONTACTS)
    fun getContacts(@Body parameters: Map<String, String>): Observable<Contacts>
}
