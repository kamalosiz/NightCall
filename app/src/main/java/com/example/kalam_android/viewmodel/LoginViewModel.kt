package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<LoginResponse>>()

    fun loginResponse(): MutableLiveData<ApiResponse<LoginResponse>> {
        return responseLiveData
    }

    fun hitLogin(parameters: Map<String, String>) {
        disposables.add(repository.login(parameters)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                        { responseLiveData.setValue(ApiResponse.success(it)) },
                        { responseLiveData.setValue(ApiResponse.error(it)) }
                ))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}