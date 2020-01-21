package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.localdb.entities.ChatData
import com.example.kalam_android.localdb.entities.ChatListData
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.model.ChatMessagesResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import javax.inject.Inject

class ForgetPasswordViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<BasicResponse>>()

    fun forgetPasswordResponse(): MutableLiveData<ApiResponse<BasicResponse>> {
        return responsiveLiveData
    }

    fun hitForgetPassword(@Body parameters: Map<String, String>) {
        disposable.add(repository.forgetPassword(parameters)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { responsiveLiveData.value = ApiResponse.loading() }
                .subscribe({
                    responsiveLiveData.value = ApiResponse.success(it)
                }, {
                    responsiveLiveData.value = ApiResponse.error(it)
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}