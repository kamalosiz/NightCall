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

class MainViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val updateFcmLiveData = MutableLiveData<ApiResponse<BasicResponse>>()

    fun updateFcmTokenResponse(): MutableLiveData<ApiResponse<BasicResponse>> {
        return updateFcmLiveData
    }

    fun hitUpdateFcmToken(
        authorization: String?, parameters: Map<String, String>
    ) {
        disposables.add(repository.updateFcm(authorization, parameters)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { d -> updateFcmLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { result -> updateFcmLiveData.setValue(ApiResponse.success(result)) },
                { throwable -> updateFcmLiveData.setValue(ApiResponse.error(throwable)) }
            ))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}