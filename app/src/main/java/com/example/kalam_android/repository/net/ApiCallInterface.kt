package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.model.VerifyCodeResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiCallInterface {

    @POST(Urls.VERIFY_PHONE)
    fun signUp(@Body parameters: Map<String, String>): Observable<SignUpResponse>

    @POST(Urls.VERIFY_PHONE_CODE)
    fun verifyCode(@Body parameters: Map<String, String>): Observable<VerifyCodeResponse>

    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@QueryMap parameters: Map<String, String>): Observable<CreateProfileResponse>

    @Multipart
    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@QueryMap parameters: Map<String, String>, @Part profilePic: MultipartBody.Part): Observable<CreateProfileResponse>

    @POST(Urls.SIGN_IN)
    fun login(@Body parameters: Map<String, String>): Observable<LoginResponse>
}
