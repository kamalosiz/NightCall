package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VerifyCodeViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val disposable = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<BasicResponse>>()

    fun verificationResponse(): MutableLiveData<ApiResponse<BasicResponse>> {
        return responseLiveData
    }

    fun hitVerificationApi(parameter: Map<String, String>) {
        disposable.add(repository.executeVerifyCode(parameter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.value = ApiResponse.loading() }
            .subscribe({
                responseLiveData.value = ApiResponse.success(it)
            }, {
                responseLiveData.value = ApiResponse.error(it)
            })
        )

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}