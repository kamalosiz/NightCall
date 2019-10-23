package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.model.VerifyCodeResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCallInterface {

    @POST(Urls.VERIFY_PHONE)
    fun signUp(@Body parameters: Map<String, String>): Observable<SignUpResponse>

    @POST(Urls.VERIFY_PHONE_CODE)
    fun verifyCode(@Body parameters: Map<String, String>): Observable<VerifyCodeResponse>

    @POST(Urls.CREATE_PROFILE)
    fun createProfile(@Body parameters: Map<String, String>): Observable<CreateProfileResponse>
}