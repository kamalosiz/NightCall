package com.example.kalam_android.viewmodel

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SignUpViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<LoginResponse>>()
    var email: String = "labeebahmad205@gmail.com"
    var password: String = "password"

    fun loginResponse(): MutableLiveData<ApiResponse<LoginResponse>> {
        return responseLiveData
    }

    fun hitLoginApi(parameters: Map<String, String>) {
        disposables.add(repository.executeLogin(parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(

                { result ->
                    Log.e("testinglogin", "Success")
                    responseLiveData.setValue(ApiResponse.success(result))
                },
                { throwable ->
                    Log.e("testinglogin", "Error")
                    responseLiveData.setValue(ApiResponse.error(throwable))
                }
            ))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}