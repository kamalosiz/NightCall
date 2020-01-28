package com.example.kalam_android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kalam_android.localdb.LocalRepo
import com.example.kalam_android.repository.Repository
import com.example.kalam_android.repository.model.BasicResponse
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.util.Debugger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LogoutViewModel @Inject constructor(
    val repository: Repository,
    private val localRepo: LocalRepo
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse<BasicResponse>>()

    fun logOutResponse(): MutableLiveData<ApiResponse<BasicResponse>> {
        return responseLiveData
    }

    fun hitLogOutApi(authorization: String?) {
        disposables.add(repository.logout(authorization)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.setValue(ApiResponse.success(it)) },
                { responseLiveData.setValue(ApiResponse.error(it)) }
            ))
    }

    fun deleteAllChats() {
        disposables.add(
            Completable.fromAction {
                localRepo.removeChats()
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    Debugger.i("testingLocal", "All Items Deleted")
                }, {
                    Debugger.i("testingLocal", "Exception while Data insertion: ${it.message}")
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}