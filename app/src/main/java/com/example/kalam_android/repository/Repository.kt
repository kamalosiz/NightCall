package com.example.kalam_android.repository

import com.example.kalam_android.repository.model.*
import com.example.kalam_android.repository.net.ApiCallInterface
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import retrofit2.http.PartMap

class Repository(private val apiCallInterface: ApiCallInterface) {

    fun executeSignup(parameters: Map<String, String>): Observable<SignUpResponse> {
        return apiCallInterface.signUp(parameters)
    }

    fun executeVerifyCode(parameters: Map<String, String>): Observable<VerifyCodeResponse> {
        return apiCallInterface.verifyCode(parameters)
    }

    fun createProfile(@PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>): Observable<CreateProfileResponse> {
        return apiCallInterface.createProfile(parameters)
    }

    fun createProfile(@PartMap parameters: Map<String, @JvmSuppressWildcards RequestBody>, @Part profilePic: MultipartBody.Part): Observable<CreateProfileResponse> {
        return apiCallInterface.createProfile(parameters, profilePic)
    }

    fun login(parameters: Map<String, String>): Observable<LoginResponse> {
        return apiCallInterface.login(parameters)
    }

    fun getContacts(parameters: Map<String, String>): Observable<Contacts> {
        return apiCallInterface.getContacts(parameters)
    }
}