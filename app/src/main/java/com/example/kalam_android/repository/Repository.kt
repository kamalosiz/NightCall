package com.example.kalam_android.repository

import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.net.ApiCallInterface
import io.reactivex.Observable

class Repository(private val apiCallInterface: ApiCallInterface) {
    fun executeLogin(parameters: Map<String, String>): Observable<LoginResponse> {
        return apiCallInterface.login(parameters)
    }
}