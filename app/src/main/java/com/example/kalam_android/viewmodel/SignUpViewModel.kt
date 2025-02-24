package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.SignUpResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SignUpViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<SignUpResponse>>()

    fun signupResponse(): MutableLiveData<ApiResponse<SignUpResponse>> {
        return responseLiveData
    }

    fun hitSignUpApi(parameters: Map<String, String>) {
        disposables.add(repository.executeSignup(parameters)
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