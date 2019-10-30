package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.AllChatListResponse
import com.example.kalam_android.repository.model.CreateProfileResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import javax.inject.Inject

class AllChatListViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val responsiveLiveData = MutableLiveData<ApiResponse<AllChatListResponse>>()

    fun allChatResponse(): MutableLiveData<ApiResponse<AllChatListResponse>> {
        return responsiveLiveData
    }

    fun hitAllChatApi(authorization: String?, @Body parameters: Map<String, String>) {
        disposable.add(repository.getAllChat(authorization, parameters)
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
}