package com.example.kalam_android.repository.net

import com.example.kalam_android.repository.model.LoginResponse
import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface ApiCallInterface {
    @POST(Urls.LOGIN)
    fun login(@QueryMap parameters: Map<String, String>): Observable<LoginResponse>
}