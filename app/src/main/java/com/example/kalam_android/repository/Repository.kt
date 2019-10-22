package com.example.kalam_android.repository

import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.model.VerifyCodeResponse
import com.example.kalam_android.repository.net.ApiCallInterface
import io.reactivex.Observable

class Repository(private val apiCallInterface: ApiCallInterface) {

    fun executeSignup(parameters: Map<String, String>): Observable<SignUpResponse> {
        return apiCallInterface.signUp(parameters)
    }

    fun executeVerifyCode(parameters: Map<String, String>): Observable<VerifyCodeResponse> {
        return apiCallInterface.verifyCode(parameters)
    }
}