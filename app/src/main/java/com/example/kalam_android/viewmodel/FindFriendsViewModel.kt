package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.FindFriends
import com.example.kalam_android.repository.model.LoginResponse
import com.example.kalam_android.repository.net.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FindFriendsViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<FindFriends>>()

    fun searchResponse(): MutableLiveData<ApiResponse<FindFriends>> {
        return responseLiveData
    }

    fun hitSearchFriends(auth:String,parameters: Map<String, String>) {
        disposables.add(repository.findFriends(auth,parameters)
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